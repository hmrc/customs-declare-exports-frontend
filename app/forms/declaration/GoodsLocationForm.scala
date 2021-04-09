/*
 * Copyright 2021 HM Revenue & Customs
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
import models.declaration.GoodsLocation
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms}
import play.api.data.Forms.text
import play.api.libs.json.{Json, OFormat}
import services.Countries.allCountries
import utils.validators.forms.FieldValidator._

case class GoodsLocationForm(code: String) {

  def toModel(): GoodsLocation = GoodsLocation(
    country = code.slice(0, 2),
    typeOfLocation = code.slice(2, 3),
    qualifierOfIdentification = code.slice(3, 4),
    identificationOfLocation = code.drop(4)
  )
}

object GoodsLocationForm extends DeclarationPage {

  implicit val format: OFormat[GoodsLocationForm] = Json.format[GoodsLocationForm]

  val formId = "Location"

  /**
    * Country is in two first characters in Location Code
    */
  private val validateCountry: String => Boolean = (input: String) => {
    val countryCode = input.take(2).toUpperCase
    allCountries.exists(_.countryCode == countryCode)
  }

  /**
    * Location Type is defined as third character in Location Code
    */
  private val validateLocationType: String => Boolean = (input: String) => {
    val correctLocationType: Set[String] = Set("A", "B", "C", "D")
    val predicate = isContainedIn(correctLocationType)
    input.drop(2).toUpperCase.headOption.map(_.toString).exists(predicate)
  }

  /**
    * Qualifier Code is defined in fourth characted in Location Code
    */
  private val validateQualifierCode: String => Boolean = (input: String) => {
    val correctQualifierCode: Set[String] = Set("U", "Y")
    val predicate = isContainedIn(correctQualifierCode)
    input.drop(3).toUpperCase.headOption.map(_.toString).exists(predicate)
  }

  val mapping = Forms.mapping(
    "code" -> text()
      .verifying("declaration.goodsLocation.code.empty", nonEmpty)
      .verifying(
        "declaration.goodsLocation.code.error",
        isEmpty or (
          validateCountry and validateLocationType and validateQualifierCode and
            noShorterThan(10) and noLongerThan(39) and isAlphanumeric
        )
      )
  )(form2Data)(GoodsLocationForm.unapply)

  private def form2Data(code: String): GoodsLocationForm = GoodsLocationForm(code.toUpperCase)

  def form(): Form[GoodsLocationForm] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.locationOfGoods.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
