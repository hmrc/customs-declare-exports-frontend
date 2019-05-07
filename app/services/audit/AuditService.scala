/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.Logger
import services.audit.AuditTypes.Audit
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject()(connector: AuditConnector, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  private def createAuditEvent(audit: Audit, auditData: Map[String, String])(implicit hc: HeaderCarrier) =
    DataEvent(
      auditSource = appConfig.appName,
      auditType = audit.toString,
      tags = AuditExtensions
        .auditHeaderCarrier(hc)
        .toAuditTags(
          transactionName = s"Export-Declaration-${audit.toString}-request",
          path = s"customs-declare-exports/${audit.toString}"
        ),
      detail = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails() ++ auditData
    )

  def audit(audit: Audit, auditData: Map[String, String])(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val event = createAuditEvent(audit: Audit, auditData: Map[String, String])
    connector.sendEvent(event).map {
      case Success =>
        Logger.debug(s"Exports ${event.auditType} audit successful")
        Success
      case Failure(err, _) =>
        Logger.warn(s"Exports ${event.auditType} Audit Error, message: $err")
        Failure(err)
      case Disabled =>
        Logger.warn(s"Auditing Disabled")
        Disabled
    }
  }

}

object AuditTypes extends Enumeration {
  type Audit = Value
  val Submission, Cancellation, SubmissionSuccess, SubmissionFailure = Value
}
object EventData extends Enumeration {
  type Data = Value
  val EORI, LRN, DUCR, SubmissionResult, Success, Failure = Value
}
