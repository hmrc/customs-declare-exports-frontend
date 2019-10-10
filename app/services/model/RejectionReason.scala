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

import java.io.File

import com.github.tototoshi.csv._
import models.Pointer
import models.declaration.notifications.Notification
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.{Json, OFormat}

import scala.io.Source

case class RejectionReason(code: String, description: String, pointer: Option[String])

object RejectionReason {

  private val logger = Logger(this.getClass)

  implicit val format: OFormat[RejectionReason] = Json.format[RejectionReason]

  def apply(list: List[String]): RejectionReason = list match {
    case code :: description :: Nil => RejectionReason(code, description, None)
    case error =>
      logger.warn("Incorrect error: " + error)
      throw new IllegalArgumentException("Error has incorrect structure")
  }

  val allRejectedErrors: List[RejectionReason] = {
    val reader =
      CSVReader.open(Source.fromURL(getClass.getClassLoader.getResource("code-lists/errors-dms-rej-list.csv"), "UTF-8"))

    val errors: List[List[String]] = reader.all()

    errors.map(RejectionReason.apply)
  }

  def getErrorDescription(errorCode: String): String =
    allRejectedErrors.find(_.code == errorCode).map(_.description).getOrElse("Unknown error")

  def fromNotifications(notifications: Seq[Notification])(implicit messages: Messages): Seq[RejectionReason] = {
    val rejectionNotification = notifications.find(_.isStatusRejected)

    rejectionNotification.map { notification =>
      notification.errors.map { error =>
        RejectionReason(
          error.validationCode,
          getErrorDescription(error.validationCode),
          error.pointer.map(_.pattern).filter(messages.isDefinedAt)
        )
      }
    }.getOrElse(Seq.empty)
  }
}
