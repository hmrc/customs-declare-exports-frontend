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

package forms.declaration
import forms.DeclarationPage
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.{hasSpecificLength, isAlphanumeric}

case class UNDangerousGoodsCode(dangerousGoodsCode: Option[String])

object UNDangerousGoodsCode extends DeclarationPage {

  implicit val format = Json.format[UNDangerousGoodsCode]

  val hasDangerousGoodsCodeKey = "hasDangerousGoodsCode"
  val dangerousGoodsCodeKey = "dangerousGoodsCode"
  private val unDangerousGoodsCodeLength = 4

  private def form2Model: (String, Option[String]) => UNDangerousGoodsCode = {
    case (hasCode, codeValue) =>
      hasCode match {
        case "Yes" => UNDangerousGoodsCode(codeValue)
        case "No"  => UNDangerousGoodsCode(None)
      }
  }

  private def model2Form: UNDangerousGoodsCode => Option[(String, Option[String])] =
    model =>
      model.dangerousGoodsCode match {
        case Some(code) => Some(("Yes", Some(code)))
        case None       => Some(("No", None))
    }

  private val mappingUNDangerousGoodsCode = optional(
    text()
      .verifying("declaration.unDangerousGoodsCode.error.length", hasSpecificLength(unDangerousGoodsCodeLength))
      .verifying("declaration.unDangerousGoodsCode.error.specialCharacters", isAlphanumeric)
  )

  val mapping =
    Forms.mapping(hasDangerousGoodsCodeKey -> text(), dangerousGoodsCodeKey -> mappingUNDangerousGoodsCode)(form2Model)(model2Form)

  def form(): Form[UNDangerousGoodsCode] = Form(mapping)
}
