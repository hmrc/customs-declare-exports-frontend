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

import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.FormFieldValidator._

case class TotalNumberOfItems(itemsNo: String)

object TotalNumberOfItems {
  implicit val format = Json.format[TotalNumberOfItems]

  val formId = "TotalNumberOfItems"

  val mapping = Forms.mapping(
    "items" -> text()
      .verifying("supplementary.totalNumberOfItems.error", isNumeric and noLongerThan(3) and containsNotOnlyZeros)
  )(TotalNumberOfItems.apply)(TotalNumberOfItems.unapply)

  def form(): Form[TotalNumberOfItems] = Form(mapping)

  def toMetadataProperties(numberOfItems: TotalNumberOfItems): Map[String, String] =
    Map("declaration.goodsItemQuantity" -> numberOfItems.itemsNo)
}
