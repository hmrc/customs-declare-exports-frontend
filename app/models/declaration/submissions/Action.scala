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

package models.declaration.submissions

import models.ExportsDeclaration
import models.declaration.submissions.RequestType._
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

sealed trait Action {

  val id: String
  val notifications: Option[Seq[NotificationSummary]]
  val requestTimestamp: ZonedDateTime

  val latestNotificationSummary: Option[NotificationSummary] =
    notifications.flatMap(_.lastOption)

  val versionNo: Int

}

case class SubmissionAction(
  id: String,
  requestTimestamp: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
  notifications: Option[Seq[NotificationSummary]] = None,
  decId: String
) extends Action {
  val versionNo: Int = 1
}

case class CancellationAction(
  id: String,
  requestTimestamp: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
  notifications: Option[Seq[NotificationSummary]] = None,
  decId: String,
  versionNo: Int
) extends Action

case class AmendmentAction(
  id: String,
  requestTimestamp: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
  notifications: Option[Seq[NotificationSummary]] = None,
  decId: String,
  versionNo: Int
) extends Action

object AmendmentAction {
  def apply(id: String, declaration: ExportsDeclaration, submission: Submission) =
    new AmendmentAction(id, decId = declaration.id, versionNo = submission.latestVersionNo + 1)
}

case class ExternalAmendmentAction(
  id: String,
  requestTimestamp: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
  notifications: Option[Seq[NotificationSummary]] = None,
  decId: Option[String],
  versionNo: Int
) extends Action

object ExternalAmendmentAction {
  def apply(id: String, decId: String, submission: Submission) =
    new ExternalAmendmentAction(id, decId = Some(decId), versionNo = submission.latestVersionNo + 1)
}

object Action {

  val defaultDateTimeZone: ZoneId = ZoneId.of("UTC")

  implicit val readLocalDateTimeFromString: Reads[ZonedDateTime] = implicitly[Reads[LocalDateTime]]
    .map(ZonedDateTime.of(_, ZoneId.of("UTC")))

  private val allWrites = (JsPath \ "id").write[String] and
    (JsPath \ "requestTimestamp").write[ZonedDateTime] and
    (JsPath \ "notifications").writeNullable[Seq[NotificationSummary]]

  implicit val writes = Writes[Action] {
    case s: SubmissionAction =>
      (allWrites and (JsPath \ "decId").write[String])(unlift(SubmissionAction.unapply)).writes(s) ++ Json.obj(
        "requestType" -> RequestTypeFormat.writes(SubmissionRequest)
      )
    case c: CancellationAction =>
      (allWrites and (JsPath \ "decId").write[String] and (JsPath \ "versionNo").write[Int])(unlift(CancellationAction.unapply)).writes(c) ++ Json
        .obj("requestType" -> RequestTypeFormat.writes(CancellationRequest))
    case a: AmendmentAction =>
      (allWrites and (JsPath \ "decId").write[String] and (JsPath \ "versionNo").write[Int])(unlift(AmendmentAction.unapply)).writes(a) ++ Json.obj(
        "requestType" -> RequestTypeFormat.writes(AmendmentRequest)
      )
    case e: ExternalAmendmentAction =>
      (allWrites and (JsPath \ "decId").writeNullable[String] and (JsPath \ "versionNo").write[Int])(unlift(ExternalAmendmentAction.unapply))
        .writes(e) ++ Json.obj("requestType" -> RequestTypeFormat.writes(ExternalAmendmentRequest))
  }

  private val allActionReads = (__ \ "id").read[String] and
    ((__ \ "requestTimestamp").read[ZonedDateTime] or (__ \ "requestTimestamp").read[ZonedDateTime](readLocalDateTimeFromString)) and
    (__ \ "notifications").readNullable[Seq[NotificationSummary]]

  implicit val reads: Reads[Action] =
    (__ \ "requestType").read[RequestType].flatMap {
      case SubmissionRequest =>
        (allActionReads and (__ \ "decId").read[String]) { (id, requestTimestamp, notifications, decId) =>
          SubmissionAction(id, requestTimestamp, notifications, decId)
        }
      case CancellationRequest =>
        (allActionReads and (__ \ "decId").read[String] and (__ \ "versionNo").read[Int]) { (id, requestTimestamp, notifications, decId, versionNo) =>
          CancellationAction(id, requestTimestamp, notifications, decId, versionNo)
        }
      case AmendmentRequest =>
        (allActionReads and (__ \ "decId").read[String] and (__ \ "versionNo").read[Int]) { (id, requestTimestamp, notifications, decId, versionNo) =>
          AmendmentAction(id, requestTimestamp, notifications, decId, versionNo)
        }
      case ExternalAmendmentRequest =>
        (allActionReads and (__ \ "decId").readNullable[String] and (__ \ "versionNo").read[Int]) {
          (id, requestTimestamp, notifications, decId, versionNo) =>
            ExternalAmendmentAction(id, requestTimestamp, notifications, decId, versionNo)
        }
    }

}
