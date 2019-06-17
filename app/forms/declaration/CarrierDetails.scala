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

import play.api.data.{Form, Forms}
import play.api.libs.json.Json

case class CarrierDetails(details: EntityDetails)

object CarrierDetails {
  implicit val format = Json.format[CarrierDetails]

  val id = "CarrierDetails"

  val mapping = Forms.mapping("details" -> EntityDetails.mapping)(CarrierDetails.apply)(CarrierDetails.unapply)

  def form(): Form[CarrierDetails] = Form(mapping)
}
