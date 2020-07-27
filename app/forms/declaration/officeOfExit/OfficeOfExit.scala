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

package forms.declaration.officeOfExit
import play.api.libs.json.Json

case class OfficeOfExit(officeId: Option[String], isUkOfficeOfExit: Option[String])

object OfficeOfExit {
  implicit val format = Json.format[OfficeOfExit]

  def from(officeOfExitOutsideUK: OfficeOfExitOutsideUK): OfficeOfExit =
    OfficeOfExit(Some(officeOfExitOutsideUK.officeId.toUpperCase), Some(AllowedUKOfficeOfExitAnswers.no))

  def from(officeOfExitInsideUK: OfficeOfExitInsideUK, existingValue: Option[OfficeOfExit]): OfficeOfExit =
    officeOfExitInsideUK.isUkOfficeOfExit match {
      case AllowedUKOfficeOfExitAnswers.yes => OfficeOfExit(officeOfExitInsideUK.officeId, Some(officeOfExitInsideUK.isUkOfficeOfExit))
      case AllowedUKOfficeOfExitAnswers.no =>
        existingValue.flatMap(_.isUkOfficeOfExit) match {
          case Some(AllowedUKOfficeOfExitAnswers.no) => OfficeOfExit(existingValue.flatMap(_.officeId), Some(AllowedUKOfficeOfExitAnswers.no))
          case _                                     => OfficeOfExit(None, Some(AllowedUKOfficeOfExitAnswers.no))
        }
    }
}
object AllowedUKOfficeOfExitAnswers {
  val yes = "Yes"
  val no = "No"

  val allowedCodes = Seq(yes, no)
}
