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

import forms.ConsignmentChoice._
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formatter
import play.api.libs.json._

object ConsignmentChoice {
  sealed trait Consignment

  case object Consolidation extends Consignment
  case object SingleShipment extends Consignment

  implicit object ConsignmentFormat extends Format[Consignment] {
    def writes(choice: Consignment): JsValue = choice match {
      case Consolidation => JsString("consolidation")
      case SingleShipment => JsString("singleShipment")
    }

    def reads(choice: JsValue): JsResult[Consignment] = choice match {
      case JsString("consolidation") => JsSuccess(Consolidation)
      case JsString("singleShipment") => JsSuccess(SingleShipment)
      case _ => JsError("Incorrect value")
    }
  }

  class ConsignmentFormatter extends Formatter[Consignment] {
    import play.api.data.FormError

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Consignment] = try{
      Right(
        data(key) match {
          case "consolidation" => Consolidation
          case "singleShipment" => SingleShipment
          case _ => throw new Exception("Incorrect value")
        }
      )
    }
    catch{
      case _:Throwable => Left(Seq(new FormError(key, Seq("error.consignment"))))
    }

    override def unbind(key: String, value: Consignment): Map[String, String] = Map(key -> value.toString)
  }

  object UtilsConsignment {
    import play.api.data.FieldMapping

    def of(implicit binder: play.api.data.format.Formatter[Consignment]): FieldMapping[Consignment] =
      FieldMapping[Consignment]()(binder)

    val formBinder = of(new ConsignmentFormatter())
  }
}

case class ConsignmentData(
  choice: Consignment,
  mucrConsolidation: Option[String],
  ducrConsolidation: Option[String],
  ducrSingleShipment: Option[String]
)

object ConsignmentData {
  implicit val format = Json.format[ConsignmentData]

  def ducr(consignmentData: ConsignmentData): Option[String] =
    if(consignmentData.choice == Consolidation) {
      consignmentData.ducrConsolidation
    } else {
      consignmentData.ducrSingleShipment
    }

  def cleanConsignmentData(consignmentData: ConsignmentData): ConsignmentData =
    consignmentData match {
      case ConsignmentData(Consolidation, None, ducr, _) =>
        ConsignmentData(SingleShipment, None, None, ducr)

      case ConsignmentData(Consolidation, mucr, ducr, _) =>
        ConsignmentData(Consolidation, mucr, ducr, None)

      case ConsignmentData(SingleShipment, _, _, ducr) =>
        ConsignmentData(SingleShipment, None, None, ducr)

      case _ =>
        consignmentData
    }
}

object ConsignmentDataValidationHelper {
  private final val correctDucrFormat = "^\\d[A-Z]{2}\\d{12}-[0-9A-Z]{1,19}$"

  private final val correctMucrFormats: Seq[String] = Seq(
    "^A:[A-Z]{3}[0-9A-Z]{0,8}$",
    "^C:[A-Z]{3}[0-9A-Z]{4}$",
    "^[A-Z]{2}\\/[A-Z]{3}-[0-9A-Z]{5,}",
    "^[A-Z]{2}\\/[A-Z]{4}-[0-9A-Z]{5,}",
    "^[A-Z]{2}\\/[0-9]{12}-[0-9A-Z]{1,}"
  )

  def emptyDucr(consignmentData: ConsignmentData): Boolean =
    if(consignmentData.choice == Consolidation) {
      ducrEmptyValidationHelper(consignmentData.ducrConsolidation)
    } else {
      ducrEmptyValidationHelper(consignmentData.ducrSingleShipment)
    }

  private def ducrEmptyValidationHelper(ducr: Option[String]): Boolean = ducr match {
    case Some(value) => !value.trim.isEmpty
    case None => false
  }

  def ducrFormat(consignmentData: ConsignmentData): Boolean =
    if(consignmentData.choice == Consolidation) {
      ducrFormatValidationHelper(consignmentData.ducrConsolidation)
    } else {
      ducrFormatValidationHelper(consignmentData.ducrSingleShipment)
    }

  private def ducrFormatValidationHelper(ducr: Option[String]): Boolean = ducr match {
    case Some(value) if(!value.trim.isEmpty) => value.matches(correctDucrFormat)
    case _ => true
  }

  def mucrFormat(consignmentData: ConsignmentData): Boolean = consignmentData.mucrConsolidation match {
    case Some(value) if consignmentData.choice == Consolidation =>
      correctMucrFormats.exists(value.matches(_))
    case _ => true
  }
}

class ConsignmentFormProvider {
  import forms.ConsignmentChoice._

  def apply(): Form[ConsignmentData] =
    Form(
      mapping(
        "choice" -> UtilsConsignment.formBinder,
        "mucrConsolidation" -> optional(text),
        "ducrConsolidation" -> optional(text),
        "ducrSingleShipment" -> optional(text)
      )(ConsignmentData.apply)(ConsignmentData.unapply _)
        .verifying("error.mucr.format", ConsignmentDataValidationHelper.mucrFormat(_))
        .verifying("error.ducr.empty", ConsignmentDataValidationHelper.emptyDucr(_))
        .verifying("error.ducr.format", ConsignmentDataValidationHelper.ducrFormat(_))
    )
}