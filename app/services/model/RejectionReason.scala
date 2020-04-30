/*
 * Copyright 2020 HM Revenue & Customs
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

package services.model

import com.github.tototoshi.csv._
import models.Pointer
import models.declaration.notifications.Notification
import play.api.Logger
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}

import scala.io.Source

case class RejectionReason(code: String, cdsDescription: String, exportsDescription: String, url: Option[String], pointer: Option[Pointer])

object RejectionReason {

  val allRejectionReasons: List[RejectionReason] = {
    val reader =
      CSVReader.open(Source.fromURL(getClass.getClassLoader.getResource("code-lists/errors-dms-rej-list.csv"), "UTF-8"))

    val errors: List[List[String]] = reader.all()

    errors.map(RejectionReason.apply)
  }

  def unknown(errorCode: String, pointer: Option[Pointer]) = RejectionReason(errorCode, "Unknown error", "Unknown error", None, pointer)

  implicit val format: OFormat[RejectionReason] = Json.format[RejectionReason]
  private val logger = Logger(this.getClass)

  def apply(list: List[String]): RejectionReason = list match {
    case code :: cdsDescription :: exportsDescription :: url :: Nil =>
      RejectionReason(code, cdsDescription, applyExportsDescription(cdsDescription, exportsDescription), applyErrorUrl(url), None)
    case error =>
      logger.warn("Incorrect error: " + error)
      throw new IllegalArgumentException("Error has incorrect structure")
  }

  private def applyExportsDescription(cdsDescription: String, exportsDescription: String): String =
    if (exportsDescription.isEmpty) cdsDescription else exportsDescription

  private def applyErrorUrl(url: String): Option[String] = if (url.isEmpty) None else Some(url)

  def fromNotifications(notifications: Seq[Notification])(implicit messages: Messages): Seq[RejectionReason] = {
    val rejectedNotification = notifications.find(_.isStatusRejected)

    rejectedNotification.map { notification =>
      notification.errors.map { error =>
        val pointer = error.pointer.filter { p =>
          val defined = messages.isDefinedAt(p.messageKey)
          if (!defined) logger.warn("Missing error message key: " + p.messageKey)
          defined
        }

        allRejectionReasons
          .find(_.code == error.validationCode)
          .map(_.copy(pointer = pointer))
          .getOrElse(unknown(error.validationCode, pointer))
      }
    }.getOrElse(Seq.empty)
  }

}
