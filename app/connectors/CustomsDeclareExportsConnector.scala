/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.exchange.ExportsDeclarationExchange
import forms.{CancelDeclaration, Lrn}
import models._
import models.declaration.notifications.Notification
import models.declaration.submissions.Submission
import models.dis.MrnStatus
import play.api.Logging
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CustomsDeclareExportsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient, metrics: Metrics) extends Logging {

  private def logPayload[T](prefix: String, payload: T)(implicit wts: Writes[T]): T = {
    logger.debug(s"$prefix: ${Json.toJson(payload)}")
    payload
  }

  def deleteDraftDeclaration(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    httpClient.DELETE[Unit](s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.declarationsPath}/$id")

  private val createTimer: Timer = metrics.defaultRegistry.timer("declaration.create.timer")

  def createDeclaration(declaration: ExportsDeclarationExchange)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] = {
    logPayload("Create Declaration Request", declaration)
    val createStopwatch = createTimer.time()
    httpClient
      .POST[ExportsDeclarationExchange, ExportsDeclarationExchange](
        s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.declarationsPath}",
        declaration
      )
      .andThen {
        case Success(response) =>
          logPayload("Create Declaration Response", response)
          createStopwatch.stop()
        case Failure(_) =>
          createStopwatch.stop()
      }
      .map(_.toExportsDeclaration)
  }

  private val updateTimer: Timer = metrics.defaultRegistry.timer("declaration.update.timer")

  def updateDeclaration(declaration: ExportsDeclaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] = {
    logPayload("Update Declaration Request", declaration)
    val updateStopwatch = updateTimer.time()
    httpClient
      .PUT[ExportsDeclarationExchange, ExportsDeclarationExchange](
        s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.declarationsPath}/${declaration.id}",
        ExportsDeclarationExchange(declaration)
      )
      .andThen {
        case Success(request) =>
          logPayload("Update Declaration Response", request)
          updateStopwatch.stop()
        case Failure(_) =>
          updateStopwatch.stop()
      }
      .map(_.toExportsDeclaration)
  }

  def findDeclarations(page: Page)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Paginated[ExportsDeclaration]] = {
    val pagination = Page.bindable.unbind("page", page)
    httpClient
      .GET[Paginated[ExportsDeclarationExchange]](s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.declarationsPath}?$pagination")
      .map(_.map(_.toExportsDeclaration))
  }

  def findSavedDeclarations(page: Page)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Paginated[ExportsDeclaration]] = {
    val pagination = Page.bindable.unbind("page", page)
    val sort = DeclarationSort.bindable.unbind("sort", DeclarationSort(SortBy.UPDATED, SortDirection.DES))
    httpClient
      .GET[Paginated[ExportsDeclarationExchange]](
        s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.declarationsPath}?status=DRAFT&$pagination&$sort"
      )
      .map(_.map(_.toExportsDeclaration))
  }

  private val fetchTimer: Timer = metrics.defaultRegistry.timer("declaration.fetch.timer")

  def findDeclaration(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ExportsDeclaration]] = {
    val fetchStopwatch = fetchTimer.time()
    httpClient
      .GET[Option[ExportsDeclarationExchange]](s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.declarationsPath}/$id")
      .map(_.map(_.toExportsDeclaration))
      .andThen {
        case _ => fetchStopwatch.stop()
      }
  }

  def submitDeclaration(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Submission] =
    httpClient
      .POSTEmpty[Submission](s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.singleSubmissionPath}/$id")

  def fetchSubmissions()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Submission]] =
    httpClient.GET[Seq[Submission]](s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.submissionsPath}").map { response =>
      logger.debug(s"CUSTOMS_DECLARE_EXPORTS fetch submission response is --> ${response.toString}")
      response
    }

  def findSubmission(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Submission]] =
    httpClient
      .GET[Seq[Submission]](s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.submissionsPath}", Seq("id" -> id))
      .map(_.headOption)

  def findSubmissionsByLrn(lrn: Lrn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Submission]] =
    httpClient
      .GET[Seq[Submission]](s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.submissionsPath}", Seq("lrn" -> lrn.value))

  def findNotifications(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Notification]] =
    httpClient
      .GET[Seq[Notification]](s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.singleSubmissionPath}${appConfig.notificationsPath}/$id")

  def findLatestNotification(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Notification]] =
    httpClient
      .GET[Option[Notification]](s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.latestNotificationPath}/$id")

  def fetchNotifications()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Notification]] =
    httpClient.GET[Seq[Notification]](s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.notificationsPath}")

  def fetchMrnStatus(mrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[MrnStatus]] = {
    val fetchStopwatch = fetchTimer.time()
    httpClient
      .GET[Option[MrnStatus]](s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.fetchMrnStatusPath}/$mrn")
      .andThen {
        case _ => fetchStopwatch.stop()
      }
  }

  def createCancellation(cancellation: CancelDeclaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CancellationStatus] = {
    logPayload("Create Cancellation Request", cancellation)

    httpClient
      .POST[CancelDeclaration, CancellationStatus](s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.cancelDeclarationPath}", cancellation)
  }

  def getVerifiedEmailAddress(eori: EORI)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Email]] =
    httpClient
      .GET[Option[Email]](s"${appConfig.customsDeclareExportsBaseUrl}${appConfig.fetchVerifiedEmailPath}/${eori.value}")
      .map { maybeVerifiedEmail =>
        maybeVerifiedEmail match {
          case Some(Email(_, true)) =>
            logger.debug(s"Found verified email for eori: $eori")
          case Some(Email(_, false)) =>
            logger.debug(s"Undeliverable email for eori: $eori")
          case None =>
            logger.info(s"Unverified email for eori: $eori")
        }
        maybeVerifiedEmail
      }
}
