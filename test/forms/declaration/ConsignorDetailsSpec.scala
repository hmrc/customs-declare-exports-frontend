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

import forms.declaration.consignor.ConsignorDetails
import play.api.libs.json.{JsObject, JsValue}

object ConsignorDetailsSpec {
  import forms.declaration.EntityDetailsSpec._

  val correctConsignorDetailsEORIOnly = consignor.ConsignorDetails(details = EntityDetailsSpec.correctEntityDetailsEORIOnly)
  val correctConsignorDetailsAddressOnly = ConsignorDetails(details = EntityDetailsSpec.correctEntityDetailsAddressOnly)
  val correctConsignorDetailsFull = consignor.ConsignorDetails(details = EntityDetailsSpec.correctEntityDetails)

  val correctConsignorDetailsJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsJSON))
  val correctConsignorDetailsEORIOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsEORIOnlyJSON))
  val correctConsignorDetailsAddressOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsAddressOnlyJSON))
  val emptyConsignorDetailsJSON: JsValue = JsObject(Map("details" -> emptyEntityDetailsJSON))
}
