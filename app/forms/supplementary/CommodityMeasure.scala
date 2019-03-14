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

package forms.supplementary
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class CommodityMeasure(supplementaryUnits: Option[String], netMass: String, grossMass: String)
object CommodityMeasure {

  implicit val format = Json.format[CommodityMeasure]

  val commodityFormId = "CommodityMeasure"
  val ADD_ONE = "supplementary.commodityMeasure.global.addOne"

  val mapping = Forms.mapping(
    "supplementaryUnits" -> optional(
      text().verifying("supplementary.commodityMeasure.supplementaryUnits.error", validateDecimal(16)(2))
    ),
    "netMass" -> text
      .verifying("supplementary.commodityMeasure.netMass.empty", nonEmpty)
      .verifying("supplementary.commodityMeasure.netMass.error", isEmpty or validateDecimal(11)(3)),
    "grossMass" -> text
      .verifying("supplementary.commodityMeasure.grossMass.empty", nonEmpty)
      .verifying("supplementary.commodityMeasure.grossMass.error", isEmpty or validateDecimal(16)(2))
  )(CommodityMeasure.apply)(CommodityMeasure.unapply)

  def form(): Form[CommodityMeasure] = Form(mapping)
}
