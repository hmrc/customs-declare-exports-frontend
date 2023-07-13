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

package services.audit

import com.google.inject.Inject
import config.AppConfig
import models.{CancelDeclaration, ExportsDeclaration}
import models.AuthKey.enrolment
import play.api.Logging
import play.api.libs.json.{JsObject, JsValue, Json}
import services.audit.AuditTypes.Audit
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}

import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject() (connector: AuditConnector, appConfig: AppConfig)(implicit ec: ExecutionContext) extends Logging {

  def audit(audit: Audit, auditData: Map[String, String])(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val event = createAuditEvent(audit: Audit, auditData: Map[String, String])
    connector.sendEvent(event).map(handleResponse(_, audit.toString))
  }

  private def createAuditEvent(audit: Audit, auditData: Map[String, String])(implicit hc: HeaderCarrier) =
    DataEvent(
      auditSource = appConfig.appName,
      auditType = audit.toString,
      tags = getAuditTags(s"${audit.toString}-request", path = s"${audit.toString}"),
      detail = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails() ++ auditData
    )

  def auditAmendmentSent(audit: Audit, auditData: JsObject)(implicit hc: HeaderCarrier) = {
    val hcAuditDetails = Json.toJson(AuditExtensions.auditHeaderCarrier(hc).toAuditDetails()).as[JsObject]
    val collatedDetails = hcAuditDetails.deepMerge(auditData)

    val extendedEvent = ExtendedDataEvent(
      auditSource = appConfig.appName,
      auditType = audit.toString,
      tags = getAuditTags(s"${audit.toString}-request", path = s"${audit.toString}"),
      detail = collatedDetails
    )

    connector.sendExtendedEvent(extendedEvent).map(handleResponse(_, audit.toString))
  }

  def auditAllPagesUserInput(auditType: AuditTypes.Audit, declaration: ExportsDeclaration)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val extendedEvent = ExtendedDataEvent(
      auditSource = appConfig.appName,
      auditType = auditType.toString,
      tags = getAuditTags(s"$auditType-payload-request", s"$auditType/full-payload"),
      detail = getAuditDetails(Json.toJson(declaration).as[JsObject])
    )
    connector.sendExtendedEvent(extendedEvent).map(handleResponse(_, auditType.toString))
  }

  def auditAllPagesDeclarationCancellation(cancelDeclaration: CancelDeclaration)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val auditType = AuditTypes.Cancellation.toString
    val extendedEvent = ExtendedDataEvent(
      auditSource = appConfig.appName,
      auditType = auditType,
      tags = getAuditTags(s"$auditType-payload-request", s"$auditType/full-payload"),
      detail = getAuditDetails(Json.toJson(cancelDeclaration).as[JsObject])
    )
    connector.sendExtendedEvent(extendedEvent).map(handleResponse(_, auditType))
  }

  def auditMessageInboxPartialRetrieved(eori: String, notificationType: String, path: String)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val auditType = AuditTypes.NavigateToMessages.toString
    val extendedEvent = ExtendedDataEvent(
      auditSource = appConfig.appName,
      auditType = auditType,
      detail = detailsForMessageInboxPartialRetrieved(eori, notificationType),
      tags = tagsForMessageInboxPartialRetrieved(path)
    )

    connector.sendExtendedEvent(extendedEvent).map(handleResponse(_, auditType))
  }

  private def getAuditTags(transactionName: String, path: String)(implicit hc: HeaderCarrier): Map[String, String] =
    AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(transactionName = s"export-declaration-${transactionName.toLowerCase}", path = s"customs-declare-exports/$path")

  private def handleResponse(result: AuditResult, auditType: String): AuditResult = result match {
    case Success =>
      logger.debug(s"Exports $auditType audit successful")
      Success

    case Failure(err, _) =>
      logger.warn(s"Exports $auditType Audit Error, message: $err")
      Failure(err)

    case Disabled =>
      logger.warn(s"Auditing Disabled")
      Disabled
  }

  private def getAuditDetails(userInput: JsObject)(implicit hc: HeaderCarrier): JsObject = {
    val hcAuditDetails = Json.toJson(AuditExtensions.auditHeaderCarrier(hc).toAuditDetails()).as[JsObject]
    hcAuditDetails.deepMerge(userInput)
  }

  private def detailsForMessageInboxPartialRetrieved(eori: String, notificationType: String): JsValue =
    Json.obj("enrolment" -> enrolment, "eoriNumber" -> eori, "tags" -> Json.obj("notificationType" -> notificationType))

  private def tagsForMessageInboxPartialRetrieved(path: String)(implicit hc: HeaderCarrier): Map[String, String] =
    AuditExtensions.auditHeaderCarrier(hc).toAuditTags(transactionName = "callExportPartial", path = path)
}

object AuditTypes extends Enumeration {
  type Audit = Value
  val Submission, SubmissionPayload, Cancellation, NavigateToMessages, Amendment, AmendmentPayload, AmendmentCancellation, UploadDocumentLink = Value
}

object EventData extends Enumeration {
  type Data = Value
  val eori, lrn, mrn, ducr, decType, changeReason, changeDescription, fullName, jobRole, email, confirmed, submissionResult, Success, Failure, url,
    preAmendmentDeclaration, postAmendmentDeclaration =
    Value
}
