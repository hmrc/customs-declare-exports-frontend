/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.section1.Lrn
import models.CancellationStatus.CancellationResult
import models._
import models.declaration.DeclarationStatus.{AMENDMENT_DRAFT, DRAFT, INITIAL}
import models.declaration.notifications.Notification
import models.declaration.submissions.EnhancedStatus.{ERRORS, EnhancedStatus}
import models.declaration.submissions.{Action, Submission, SubmissionAmendment}
import models.dis.MrnStatus
import play.api.Logging
import play.api.http.Status.{CREATED, NOT_FOUND}
import play.api.libs.json._
import services.AuditCreateDraftDec
import services.audit.AuditService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, JsValidationException}
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CustomsDeclareExportsConnector @Inject() (appConfig: AppConfig, httpClientV2: HttpClientV2, metrics: Metrics, auditService: AuditService)
    extends Connector with AuditCreateDraftDec with Logging {

  protected val httpClient: HttpClientV2 = httpClientV2

  private def getUrl(path: String): String =
    s"${appConfig.customsDeclareExportsBaseUrl}$path"

  private def logPayload[T](prefix: String, payload: T)(implicit wts: Writes[T]): Unit =
    logger.debug(s"$prefix: ${Json.toJson(payload)}")

  private def parseTextResponse(body: String): String =
    body.slice(1, body.length - 1)

  private val createTimer: Timer = metrics.defaultRegistry.timer("declaration.create.timer")

  def createDeclaration(
    declaration: ExportsDeclaration,
    eori: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] = {
    logPayload("Create Declaration Request", declaration)
    val createStopwatch = createTimer.time

    postJson[ExportsDeclaration, ExportsDeclaration](getUrl(s"${appConfig.declarationsPath}"), declaration).andThen {
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
    delete[Unit](getUrl(s"${appConfig.declarationsPath}/$id"))

  private val fetchTimer: Timer = metrics.defaultRegistry.timer("declaration.fetch.timer")

  def findDeclaration(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ExportsDeclaration]] =
    if (id.trim.isEmpty) Future.successful(None)
    else {
      val fetchStopwatch = fetchTimer.time

      val url = getUrl(s"${appConfig.declarationsPath}/$id")

      get[HttpResponse](url, List.empty).map { httpResponse =>
        if (httpResponse.status == NOT_FOUND) None
        else
          Json.parse(httpResponse.body).validate[ExportsDeclaration] match {
            case JsSuccess(declaration, _) => Some(declaration)
            case JsError(error) =>
              logger.error(s"Illegal Json body while retrieving Draft Declaration with id($id):\n${httpResponse.body}\n")
              throw new JsValidationException("GET", url, classOf[ExportsDeclaration], error.toString())
          }
      }.andThen { case _ =>
        fetchStopwatch.stop
      }
    }

  def fetchDraftDeclarations(page: models.Page)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Paginated[DraftDeclarationData]] = {
    val pagination = models.Page.bindable.unbind("page", page)
    val sort = DeclarationSort.bindable.unbind("sort", DeclarationSort(SortBy.UPDATED, SortDirection.DESC))

    get[Paginated[DraftDeclarationData]](getUrl(s"${appConfig.draftDeclarationsPath}?$pagination&$sort"))
  }

  def findOrCreateDraftForAmendment(parentId: String, enhancedStatus: EnhancedStatus, eori: String, draftDec: ExportsDeclaration)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[String] = {
    val url = getUrl(s"${appConfig.draftAmendmentPath}/$parentId/${enhancedStatus.toString}")
    val fetchStopwatch = fetchTimer.time

    get[HttpResponse](url, List.empty).map { httpResponse =>
      val newDecId = parseTextResponse(httpResponse.body)
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
    }.andThen { case _ =>
      fetchStopwatch.stop
    }
  }

  def findOrCreateDraftForRejection(rejectedParentId: String, eori: String, draftDec: ExportsDeclaration)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[String] = {
    val url = getUrl(s"${appConfig.draftRejectionPath}/$rejectedParentId")
    val fetchStopwatch = fetchTimer.time

    get[HttpResponse](url, List.empty).map { httpResponse =>
      val newDecId = parseTextResponse(httpResponse.body)
      if (httpResponse.status == CREATED)
        audit(eori, newDecId, draftDec.additionalDeclarationType, draftDec.ducr, DRAFT, Some(draftDec.id), Some(ERRORS), auditService)
      newDecId
    }.andThen { case _ =>
      fetchStopwatch.stop
    }
  }

  def findDraftByParent(parentId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ExportsDeclaration]] = {
    val fetchStopwatch = fetchTimer.time

    get[Option[ExportsDeclaration]](getUrl(s"${appConfig.draftByParentPath}/$parentId")).andThen { case _ =>
      fetchStopwatch.stop
    }
  }

  def submitDeclaration(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Submission] =
    postWithoutBody[Submission](getUrl(s"${appConfig.submissionPath}/$id"))

  def submitAmendment(amendment: SubmissionAmendment)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    postJson[SubmissionAmendment, String](getUrl(s"${appConfig.amendmentsPath}"), amendment)

  def resubmitAmendment(amendment: SubmissionAmendment)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    postJson[SubmissionAmendment, String](getUrl(s"${appConfig.resubmitAmendmentPath}"), amendment)

  private val updateTimer: Timer = metrics.defaultRegistry.timer("declaration.update.timer")

  def updateDeclaration(decl: ExportsDeclaration, eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] = {
    logPayload("Update Declaration Request", decl)
    val updateStopwatch = updateTimer.time()

    putJson[ExportsDeclaration, ExportsDeclaration](getUrl(s"${appConfig.declarationsPath}"), decl).andThen {
      case Success(declaration) =>
        logPayload("Update Declaration Response", declaration)
        updateStopwatch.stop()
        if (decl.declarationMeta.status == INITIAL && decl.ducr.isDefined)
          audit(eori, decl.id, decl.additionalDeclarationType, decl.ducr, DRAFT, None, None, auditService)

      case Failure(_) =>
        updateStopwatch.stop()
    }
  }

  def fetchSubmissionPage(queryString: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PageOfSubmissions] =
    get[PageOfSubmissions](getUrl(s"${appConfig.pageOfSubmissionsPath}?$queryString"))

  def findSubmission(uuid: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Submission]] =
    get[Option[Submission]](getUrl(s"${appConfig.submissionPath}/$uuid"))

  def findSubmissionByAction(actionId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Submission]] =
    get[Option[Submission]](getUrl(s"${appConfig.submissionByActionPath}/$actionId"))

  def findAction(actionId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Action]] =
    get[Option[Action]](getUrl(s"${appConfig.actionPath}/$actionId"))

  def findNotifications(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Notification]] =
    get[Seq[Notification]](getUrl(s"${appConfig.notificationsPath}/$id"))

  def findLatestNotification(actionId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Notification]] =
    get[Option[Notification]](getUrl(s"${appConfig.latestNotificationPath}/$actionId"))

  def isLrnAlreadyUsed(lrn: Lrn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] =
    get[Boolean](getUrl(s"${appConfig.lrnAlreadyUsedPath}/${lrn.lrn}"))

  def fetchMrnStatus(mrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MrnStatus] = {
    val fetchStopwatch = fetchTimer.time

    get[MrnStatus](getUrl(s"${appConfig.fetchMrnStatusPath}/$mrn")).andThen { case _ =>
      fetchStopwatch.stop
    }
  }

  def createCancellation(cancellation: CancelDeclaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CancellationResult] = {
    logPayload("Create Cancellation Request", cancellation)
    postJson[CancelDeclaration, CancellationResult](getUrl(s"${appConfig.cancelDeclarationPath}"), cancellation)
  }

  def getVerifiedEmailAddress(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Email]] =
    get[Option[Email]](getUrl(s"${appConfig.fetchVerifiedEmailPath}"))
}
