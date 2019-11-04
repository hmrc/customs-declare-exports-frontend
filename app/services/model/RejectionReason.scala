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

package services.model

import com.github.tototoshi.csv._
import models.Pointer
import models.declaration.notifications.Notification
import play.api.Logger
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}

import scala.io.Source

case class RejectionReason(code: String, cdsDescription: String, exportsDescription: String, pointer: Option[Pointer])

object RejectionReason {
  val allRejectedErrors: List[RejectionReason] = {
    val reader =
      CSVReader.open(Source.fromURL(getClass.getClassLoader.getResource("code-lists/errors-dms-rej-list.csv"), "UTF-8"))

    val errors: List[List[String]] = reader.all()

    errors.map(RejectionReason.apply)
  }

  implicit val format: OFormat[RejectionReason] = Json.format[RejectionReason]
  private val logger = Logger(this.getClass)

  def apply(list: List[String]): RejectionReason = list match {
    case code :: cdsDescription :: exportsDescription :: Nil => RejectionReason(code, cdsDescription, exportsDescription, None)
    case error =>
      logger.warn("Incorrect error: " + error)
      throw new IllegalArgumentException("Error has incorrect structure")
  }

  def fromNotifications(notifications: Seq[Notification])(implicit messages: Messages): Seq[RejectionReason] = {
    val rejectionNotification = notifications.find(_.isStatusRejected)

    rejectionNotification.map { notification =>
      notification.errors.map { error =>
        logMissingPointerMessageKey(error.pointer)

        RejectionReason(
          error.validationCode,
          getCdsErrorDescription(error.validationCode),
          getExportsErrorDescription(error.validationCode),
          error.pointer.filter(p => messages.isDefinedAt(p.messageKey))
        )
      }
    }.getOrElse(Seq.empty)
  }

  private def logMissingPointerMessageKey(pointer: Option[Pointer])(implicit messages: Messages): Unit =
    pointer.foreach(p => if (messages.isDefinedAt(p.messageKey)) logger.warn("Missing error message key: " + p.messageKey))

  def getCdsErrorDescription(errorCode: String): String =
    allRejectedErrors.find(_.code == errorCode).map(_.cdsDescription).getOrElse("Unknown error")

  def getExportsErrorDescription(errorCode: String): String =
    allRejectedErrors.find(_.code == errorCode).map(_.exportsDescription).getOrElse("Unknown error")
}
