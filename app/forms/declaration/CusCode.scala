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

package forms.declaration
import forms.DeclarationPage
import forms.MappingHelper.requiredRadio
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import services.DiffTools.compareOptionalString
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class CusCode(cusCode: Option[String]) extends Ordered[CusCode] {
  override def compare(that: CusCode): Int =
    compareOptionalString(cusCode, that.cusCode)
}

object CusCode extends DeclarationPage with FieldMapping {
  implicit val format = Json.format[CusCode]

  val pointer: ExportsFieldPointer = "cusCode"

  val hasCusCodeKey = "hasCusCode"
  val cusCodeKey = "cusCode"
  private val cusCodeLength = 8

  object AllowedCUSCodeAnswers {
    val yes = "Yes"
    val no = "No"
  }

  import AllowedCUSCodeAnswers._

  private def form2Model: (String, Option[String]) => CusCode = { case (hasCode, codeValue) =>
    hasCode match {
      case AllowedCUSCodeAnswers.yes => CusCode(codeValue)
      case AllowedCUSCodeAnswers.no  => CusCode(None)
    }
  }

  private def model2Form: CusCode => Option[(String, Option[String])] =
    model =>
      model.cusCode match {
        case Some(code) => Some((yes, Some(code)))
        case None       => Some((no, None))
      }

  val mapping =
    Forms.mapping(
      hasCusCodeKey -> requiredRadio("declaration.cusCode.answer.empty"),
      cusCodeKey -> mandatoryIfEqual(
        hasCusCodeKey,
        yes,
        text()
          .verifying("declaration.cusCode.error.empty", nonEmpty)
          .verifying("declaration.cusCode.error.length", isEmpty or hasSpecificLength(cusCodeLength))
          .verifying("declaration.cusCode.error.specialCharacters", isEmpty or isAlphanumeric)
      )
    )(form2Model)(model2Form)

  def form: Form[CusCode] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.item.cusCode.common"))
}
