/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.section2

import forms.DeclarationPage
import forms.common.Eori
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.Parties.partiesPrefix
import models.viewmodels.TariffContentKey
import models.{Amendment, FieldMapping}
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, compareDifference, ExportsDeclarationDiff}

case class PersonPresentingGoodsDetails(eori: Eori) extends DiffTools[PersonPresentingGoodsDetails] with Amendment {

  override def createDiff(
    original: PersonPresentingGoodsDetails,
    pointerString: ExportsFieldPointer,
    sequenceId: Option[Int] = None
  ): ExportsDeclarationDiff =
    Seq(compareDifference(original.eori, eori, combinePointers(pointerString, Eori.pointer, sequenceId))).flatten

  def value: String = eori.value
}

object PersonPresentingGoodsDetails extends DeclarationPage with FieldMapping {
  implicit val format: OFormat[PersonPresentingGoodsDetails] = Json.format[PersonPresentingGoodsDetails]

  val pointer: ExportsFieldPointer = "personPresentingGoodsDetails"

  lazy val keyForAmend = s"${partiesPrefix}.personPresentingGoods"

  val fieldName = "eori"

  private val mapping = Forms.mapping(fieldName -> Eori.mapping())(PersonPresentingGoodsDetails.apply)(PersonPresentingGoodsDetails =>
    Some(PersonPresentingGoodsDetails.eori)
  )

  def form: Form[PersonPresentingGoodsDetails] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.personPresentingGoods.clearance"))
}
