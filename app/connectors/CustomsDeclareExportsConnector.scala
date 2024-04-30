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
import config.AppConfig
import config.featureFlags.DeclarationAmendmentsConfig
import forms.Lrn
import models.CancellationStatus.CancellationResult
import models._
import models.declaration.DeclarationStatus.{AMENDMENT_DRAFT, DRAFT, INITIAL}
import models.declaration.notifications.Notification
import models.declaration.submissions.EnhancedStatus.{ERRORS, EnhancedStatus}
import models.declaration.submissions.{Action, Submission, SubmissionAmendment}
import models.dis.MrnStatus
import play.api.Logging
import play.api.http.Status.CREATED
import play.api.libs.json._
import services.AuditCreateDraftDec
import services.audit.AuditService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, JsValidationException}
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CustomsDeclareExportsConnector @Inject() (
  appConfig: AppConfig,
  httpClient: HttpClient,
  metrics: Metrics,
  amendmentFlag: DeclarationAmendmentsConfig,
  auditService: AuditService
) extends AuditCreateDraftDec with Logging {

  private def getUrl(path: String): String =
    s"${appConfig.customsDeclareExportsBaseUrl}$path"

  private def logPayload[T](prefix: String, payload: T)(implicit wts: Writes[T]): Unit =
    logger.debug(s"$prefix: ${Json.toJson(payload)}")

  private val createTimer: Timer = metrics.defaultRegistry.timer("declaration.create.timer")

  def createDeclaration(
    declaration: ExportsDeclaration,
    eori: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] = {
    logPayload("Create Declaration Request", declaration)
    val createStopwatch = createTimer.time

    httpClient
      .POST[ExportsDeclaration, ExportsDeclaration](getUrl(s"${appConfig.declarationsPath}"), declaration)
      .andThen {
        case Success(newDeclaration) =>
          logPayload("Create Declaration Response", newDeclaration)
          createStopwatch.stop()
          // this will exclude draft decs created from scratch that have no DUCR defined yet
          if (declaration.ducr.isDefined)
            audit(eori, newDeclaration, auditService)

        case Failure(_) =>
          createStopwatch.stop()
      }
  }

  def deleteDraftDeclaration(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    httpClient.DELETE[Unit](getUrl(s"${appConfig.declarationsPath}/$id"))

  private val fetchTimer: Timer = metrics.defaultRegistry.timer("declaration.fetch.timer")

  def findDeclaration(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ExportsDeclaration]] = {
    val fetchStopwatch = fetchTimer.time

    val url = getUrl(s"${appConfig.declarationsPath}/$id")

    httpClient
      .GET[JsValue](url)
      .map { json =>
        json.validate[ExportsDeclaration] match {
          case JsSuccess(declaration, _) => Some(declaration)
          case JsError(error) =>
            logger.error(s"Illegal Json body while retrieving Draft Declaration with id($id):\n$json\n")
            throw new JsValidationException("GET", url, classOf[ExportsDeclaration], error.toString())
        }
      }
      .andThen { case _ =>
        fetchStopwatch.stop
      }
  }

  def findSavedDeclarations(page: models.Page)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Paginated[ExportsDeclaration]] = {
    val pagination = models.Page.bindable.unbind("page", page)
    val sort = DeclarationSort.bindable.unbind("sort", DeclarationSort(SortBy.UPDATED, SortDirection.DES))
    val statusParameters = if (amendmentFlag.isEnabled) "?status=DRAFT&status=AMENDMENT_DRAFT&" else "?status=DRAFT&"

    val url = getUrl(s"${appConfig.declarationsPath}$statusParameters$pagination&$sort")

    httpClient
      .GET[JsValue](url)
      .map { json =>
        json.validate[Paginated[ExportsDeclaration]] match {
          case JsSuccess(results, _) => results
          case JsError(error) =>
            logger.error(s"Illegal Json body while retrieving a $page of Draft Declarations:\n$json\n")
            throw new JsValidationException("GET", url, classOf[ExportsDeclaration], error.toString())
        }
      }
  }

  def findOrCreateDraftForAmendment(parentId: String, enhancedStatus: EnhancedStatus, eori: String, draftDec: ExportsDeclaration)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[String] = {
    val url = getUrl(s"${appConfig.draftAmendmentPath}/$parentId/${enhancedStatus.toString}")
    val fetchStopwatch = fetchTimer.time

    httpClient
      .doGet(url, hc.headers(HeaderNames.explicitlyIncludedHeaders))
      .map { httpResponse =>
        val newDecId = parseResponseBody(httpResponse.body)
        // this will exclude draft decs created from scratch that have no DUCR defined yet
        if (httpResponse.status == CREATED && draftDec.ducr.isDefined)
          audit(
            eori,
            newDecId,
            draftDec.additionalDeclarationType,
            draftDec.ducr,
            AMENDMENT_DRAFT,
            Some(draftDec.id),
            draftDec.declarationMeta.parentDeclarationEnhancedStatus,
            auditService
          )
        newDecId
      }
      .andThen { case _ =>
        fetchStopwatch.stop
      }
  }

  def findOrCreateDraftForRejection(rejectedParentId: String, eori: String, draftDec: ExportsDeclaration)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[String] = {
    val url = getUrl(s"${appConfig.draftRejectionPath}/$rejectedParentId")
    val fetchStopwatch = fetchTimer.time

    httpClient
      .doGet(url, hc.headers(HeaderNames.explicitlyIncludedHeaders))
      .map { httpResponse =>
        val newDecId = parseResponseBody(httpResponse.body)
        if (httpResponse.status == CREATED)
          audit(eori, newDecId, draftDec.additionalDeclarationType, draftDec.ducr, DRAFT, Some(draftDec.id), Some(ERRORS), auditService)
        newDecId
      }
      .andThen { case _ =>
        fetchStopwatch.stop
      }
  }

  def parseResponseBody(body: String): String =
    body.slice(1, body.length - 1)

  def findDraftByParent(parentId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ExportsDeclaration]] = {
    val fetchStopwatch = fetchTimer.time

    httpClient.GET[Option[ExportsDeclaration]](getUrl(s"${appConfig.draftByParentPath}/$parentId")).andThen { case _ =>
      fetchStopwatch.stop
    }
  }

  def submitDeclaration(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Submission] =
    httpClient.POSTEmpty[Submission](getUrl(s"${appConfig.submissionPath}/$id"))

  def submitAmendment(amendment: SubmissionAmendment)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    httpClient.POST[SubmissionAmendment, String](getUrl(s"${appConfig.amendmentsPath}"), amendment)

  def resubmitAmendment(amendment: SubmissionAmendment)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    httpClient.POST[SubmissionAmendment, String](getUrl(s"${appConfig.resubmitAmendmentPath}"), amendment)

  private val updateTimer: Timer = metrics.defaultRegistry.timer("declaration.update.timer")

  def updateDeclaration(dec: ExportsDeclaration, eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] = {
    logPayload("Update Declaration Request", dec)
    val updateStopwatch = updateTimer.time()

    httpClient
      .PUT[ExportsDeclaration, ExportsDeclaration](getUrl(s"${appConfig.declarationsPath}/${dec.id}"), dec)
      .andThen {
        case Success(declaration) =>
          logPayload("Update Declaration Response", declaration)
          updateStopwatch.stop()
          if (dec.declarationMeta.status == INITIAL && dec.ducr.isDefined)
            audit(eori, dec.id, dec.additionalDeclarationType, dec.ducr, DRAFT, None, None, auditService)

        case Failure(_) =>
          updateStopwatch.stop()
      }
  }

  def fetchSubmissionPage(queryString: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PageOfSubmissions] =
    httpClient.GET[PageOfSubmissions](getUrl(s"${appConfig.pageOfSubmissionsPath}?$queryString"))

  def findSubmission(uuid: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Submission]] =
    httpClient
      .GET[Option[Submission]](getUrl(s"${appConfig.submissionPath}/$uuid"))

  def findSubmissionByAction(actionId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Submission]] =
    httpClient.GET[Option[Submission]](getUrl(s"${appConfig.submissionByActionPath}/$actionId"))

  def findAction(actionId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Action]] =
    httpClient.GET[Option[Action]](getUrl(s"${appConfig.actionPath}/$actionId"))

  def findNotifications(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Notification]] =
    httpClient.GET[Seq[Notification]](getUrl(s"${appConfig.notificationsPath}/$id"))

  def findLatestNotification(actionId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Notification]] =
    httpClient.GET[Option[Notification]](getUrl(s"${appConfig.latestNotificationPath}/$actionId"))

  def isLrnAlreadyUsed(lrn: Lrn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    httpClient.GET[Boolean](getUrl(s"${appConfig.lrnAlreadyUsedPath}/${lrn.lrn}"))

  def fetchMrnStatus(mrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[MrnStatus]] = {
    val fetchStopwatch = fetchTimer.time

    httpClient
      .GET[Option[MrnStatus]](getUrl(s"${appConfig.fetchMrnStatusPath}/$mrn"))
      .andThen { case _ =>
        fetchStopwatch.stop
      }
  }

  def createCancellation(cancellation: CancelDeclaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CancellationResult] = {
    logPayload("Create Cancellation Request", cancellation)
    httpClient.POST[CancelDeclaration, CancellationResult](getUrl(s"${appConfig.cancelDeclarationPath}"), cancellation)
  }

  def getVerifiedEmailAddress(eori: EORI)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Email]] =
    httpClient
      .GET[Option[Email]](getUrl(s"${appConfig.fetchVerifiedEmailPath}/${eori.value}"))
      .map { maybeVerifiedEmail =>
        maybeVerifiedEmail match {
          case Some(Email(_, true))  => logger.debug(s"Found verified email for eori: $eori")
          case Some(Email(_, false)) => logger.debug(s"Undeliverable email for eori: $eori")
          case None                  => logger.info(s"Unverified email for eori: $eori")
        }

        maybeVerifiedEmail
      }
}
