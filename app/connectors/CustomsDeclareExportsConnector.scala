/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import com.codahale.metrics.Timer
import com.kenshoo.play.metrics.Metrics
import config.AppConfig
import forms.Lrn
import models._
import models.declaration.notifications.Notification
import models.declaration.submissions.Submission
import models.dis.MrnStatus
import play.api.Logging
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CustomsDeclareExportsConnector @Inject() (appConfig: AppConfig, httpClient: HttpClient, metrics: Metrics) extends Logging {

  private def url(path: String): String =
    s"${appConfig.customsDeclareExportsBaseUrl}$path"

  private def logPayload[T](prefix: String, payload: T)(implicit wts: Writes[T]): Unit =
    logger.debug(s"$prefix: ${Json.toJson(payload)}")

  private val createTimer: Timer = metrics.defaultRegistry.timer("declaration.create.timer")

  def createDeclaration(declaration: ExportsDeclaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] = {
    logPayload("Create Declaration Request", declaration)
    val createStopwatch = createTimer.time

    httpClient
      .POST[ExportsDeclaration, ExportsDeclaration](url(s"${appConfig.declarationsPath}"), declaration)
      .andThen {
        case Success(response) =>
          logPayload("Create Declaration Response", response)
          createStopwatch.stop()

        case Failure(_) =>
          createStopwatch.stop()
      }
  }

  def deleteDraftDeclaration(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    httpClient.DELETE[Unit](url(s"${appConfig.declarationsPath}/$id"))

  private val fetchTimer: Timer = metrics.defaultRegistry.timer("declaration.fetch.timer")

  def findDeclaration(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ExportsDeclaration]] = {
    val fetchStopwatch = fetchTimer.time

    httpClient
      .GET[Option[ExportsDeclaration]](url(s"${appConfig.declarationsPath}/$id"))
      .andThen { case _ =>
        fetchStopwatch.stop
      }
  }

  def findDeclarations(page: models.Page)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Paginated[ExportsDeclaration]] = {
    val pagination = models.Page.bindable.unbind("page", page)
    httpClient.GET[Paginated[ExportsDeclaration]](url(s"${appConfig.declarationsPath}?$pagination"))
  }

  def findSavedDeclarations(page: models.Page)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Paginated[ExportsDeclaration]] = {
    val pagination = models.Page.bindable.unbind("page", page)
    val sort = DeclarationSort.bindable.unbind("sort", DeclarationSort(SortBy.UPDATED, SortDirection.DES))

    httpClient.GET[Paginated[ExportsDeclaration]](url(s"${appConfig.declarationsPath}?status=DRAFT&$pagination&$sort"))
  }

  def findOrCreateDraftForRejected(rejectedId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {
    val fetchStopwatch = fetchTimer.time

    httpClient.GET[String](url(s"${appConfig.draftDeclarationPath}/$rejectedId")).andThen { case _ =>
      fetchStopwatch.stop
    }
  }

  def findOrCreateDraftForAmend(submissionId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {
    val fetchStopwatch = fetchTimer.time

    httpClient
      .GET[String](url(s"${appConfig.amendDeclarationPath}/$submissionId"))
      .andThen { case _ =>
        fetchStopwatch.stop
      }
  }

  def submitDeclaration(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Submission] =
    httpClient.POSTEmpty[Submission](url(s"${appConfig.submissionPath}/$id"))

  private val updateTimer: Timer = metrics.defaultRegistry.timer("declaration.update.timer")

  def updateDeclaration(declaration: ExportsDeclaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] = {
    logPayload("Update Declaration Request", declaration)
    val updateStopwatch = updateTimer.time()

    httpClient
      .PUT[ExportsDeclaration, ExportsDeclaration](url(s"${appConfig.declarationsPath}/${declaration.id}"), declaration)
      .andThen {
        case Success(request) =>
          logPayload("Update Declaration Response", request)
          updateStopwatch.stop()

        case Failure(_) =>
          updateStopwatch.stop()
      }
  }

  def fetchSubmissionPage(queryString: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PageOfSubmissions] =
    httpClient.GET[PageOfSubmissions](url(s"${appConfig.pageOfSubmissionsPath}?$queryString"))

  def findSubmission(uuid: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Submission]] =
    httpClient
      .GET[Option[Submission]](url(s"${appConfig.submissionPath}/$uuid"))

  def isLrnAlreadyUsed(lrn: Lrn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    httpClient.GET[Boolean](url(s"${appConfig.lrnAlreadyUsedPath}/${lrn.lrn}"))

  def findNotifications(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Notification]] =
    httpClient.GET[Seq[Notification]](url(s"${appConfig.submissionPath}${appConfig.notificationsPath}/$id"))

  def findLatestNotification(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Notification]] =
    httpClient.GET[Option[Notification]](url(s"${appConfig.latestNotificationPath}/$id"))

  def fetchMrnStatus(mrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[MrnStatus]] = {
    val fetchStopwatch = fetchTimer.time

    httpClient
      .GET[Option[MrnStatus]](url(s"${appConfig.fetchMrnStatusPath}/$mrn"))
      .andThen { case _ =>
        fetchStopwatch.stop
      }
  }

  def createCancellation(cancellation: CancelDeclaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CancellationStatus] = {
    logPayload("Create Cancellation Request", cancellation)

    httpClient.POST[CancelDeclaration, CancellationStatus](url(s"${appConfig.cancelDeclarationPath}"), cancellation)
  }

  def getVerifiedEmailAddress(eori: EORI)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Email]] =
    httpClient
      .GET[Option[Email]](url(s"${appConfig.fetchVerifiedEmailPath}/${eori.value}"))
      .map { maybeVerifiedEmail =>
        maybeVerifiedEmail match {
          case Some(Email(_, true))  => logger.debug(s"Found verified email for eori: $eori")
          case Some(Email(_, false)) => logger.debug(s"Undeliverable email for eori: $eori")
          case None                  => logger.info(s"Unverified email for eori: $eori")
        }

        maybeVerifiedEmail
      }
}
