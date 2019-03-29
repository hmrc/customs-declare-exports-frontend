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

package helpers.views.declaration

trait LocationOfGoodsMessages {

  val locationOfGoods: String = "supplementary.goodsLocation"

  val title: String = locationOfGoods + ".title"
  val typeOfLocation: String = locationOfGoods + ".typeOfLocation"
  val typeOfLocationError: String = locationOfGoods + ".typeOfLocation.error"
  val typeOfLocationEmpty: String = locationOfGoods + ".typeOfLocation.empty"
  val qualifierOfIdent: String = locationOfGoods + ".qualifierOfIdentification"
  val qualifierOfIdentError: String = locationOfGoods + ".qualifierOfIdentification.error"
  val qualifierOfIdentEmpty: String = locationOfGoods + ".qualifierOfIdentification.empty"
  val identOfLocation: String = locationOfGoods + ".identificationOfLocation"
  val identOfLocationError: String = locationOfGoods + ".identificationOfLocation.error"
  val additionalIdentifier: String = locationOfGoods + ".additionalIdentifier"
  val additionalIdentifierError: String = locationOfGoods + ".additionalIdentifier.error"
  val streetAndNumber: String = locationOfGoods + ".streetAndNumber"
  val streetAndNumberError: String = locationOfGoods + ".streetAndNumber.error"
  val logPostCode: String = locationOfGoods + ".postCode"
  val logPostCodeError: String = locationOfGoods + ".postCode.error"
  val city: String = locationOfGoods + ".city"
  val cityError: String = locationOfGoods + ".city.error"
}
