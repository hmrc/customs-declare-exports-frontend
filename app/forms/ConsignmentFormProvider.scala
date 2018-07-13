/*
 * Copyright 2018 HM Revenue & Customs
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

package forms

import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json

case class ConsignmentData(
  choice: String,
  mucrConsolidation: Option[String],
  ducrConsolidation: Option[String],
  ducrSingleShipment: Option[String]
)

object ConsignmentData {
  implicit val format = Json.format[ConsignmentData]
}

object ConsignmentDataValidationHelper {
  private final val correctDucrFormat = "^\\d[A-Z]{2}\\d{12}-[0-9A-Z]{19}$"

  private final val correctMucrFormats: Seq[String] = Seq(
    "^A:[A-Z]{3}[0-9A-Z]{0,8}$",
    "^C:[A-Z]{3}[0-9A-Z]{4}$",
    "^[A-Z]{2}\\/[A-Z]{3}-[0-9A-Z]{5,}",
    "^[A-Z]{2}\\/[A-Z]{4}-[0-9A-Z]{5,}",
    "^[A-Z]{2}\\/[0-9]{12}-[0-9A-Z]{1,}"
  )

  def emptyDucr(consignmentData: ConsignmentData): Boolean =
    if(consignmentData.choice == "consolidation") {
      ducrEmptyValidationHelper(consignmentData.ducrConsolidation)
    } else {
      ducrEmptyValidationHelper(consignmentData.ducrSingleShipment)
    }

  private def ducrEmptyValidationHelper(ducr: Option[String]): Boolean = ducr match {
    case Some(value) => !value.isEmpty
    case None => false
  }

  def ducrFormat(consignmentData: ConsignmentData): Boolean =
    if(consignmentData.choice == "consolidation") {
      ducrFormatValidationHelper(consignmentData.ducrConsolidation)
    } else {
      ducrFormatValidationHelper(consignmentData.ducrSingleShipment)
    }

  private def ducrFormatValidationHelper(ducr: Option[String]): Boolean = ducr match {
    case Some(value) => value.matches(correctDucrFormat)
    case None => false
  }

  def mucrFormat(consignmentData: ConsignmentData): Boolean = consignmentData.mucrConsolidation match {
    case Some(value) if consignmentData.choice == "consolidation" =>
      correctMucrFormats.exists(value.matches(_))
    case None => true
  }
}

class ConsignmentFormProvider {

  def apply(): Form[ConsignmentData] =
    Form(
      mapping(
        "choice" -> text,
        "mucrConsolidation" -> optional(text),
        "ducrConsolidation" -> optional(text),
        "ducrSingleShipment" -> optional(text)
      )(ConsignmentData.apply)(ConsignmentData.unapply)
        .verifying("error.mucr.format", ConsignmentDataValidationHelper.mucrFormat(_))
        .verifying("error.ducr.empty", ConsignmentDataValidationHelper.emptyDucr(_))
        .verifying("error.ducr.format", ConsignmentDataValidationHelper.ducrFormat(_))
    )
}