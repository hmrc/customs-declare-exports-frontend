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

import forms.MetadataPropertiesConvertable
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class GoodsItemNumber(goodItemNumber: String) extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] = Map(
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].sequenceNumeric" -> goodItemNumber,
    "declaration.goodsItemQuantity" -> goodItemNumber
  )
}

object GoodsItemNumber {
  implicit val format = Json.format[GoodsItemNumber]

  val formId = "GoodItemNumber"

  val mapping = Forms.mapping(
    "goodItemNumber" -> text()
      .verifying("supplementary.goodItemNumber.error", noLongerThan(3) and containsNotOnlyZeros and isNumeric)
  )(GoodsItemNumber.apply)(GoodsItemNumber.unapply)

  def form(): Form[GoodsItemNumber] = Form(mapping)
}
