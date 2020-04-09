/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.common.Address
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json

case class ConsigneeDetails(details: EntityDetails)

object ConsigneeDetails extends DeclarationPage {
  implicit val format = Json.format[ConsigneeDetails]

  val id = "ConsigneeDetails"

  val consigneeMapping: Mapping[EntityDetails] = Forms.mapping("address" -> Address.mapping)(
    address => EntityDetails(None, Some(address))
  )(entityDetails => entityDetails.address)

  val mapping = Forms.mapping("details" -> consigneeMapping)(ConsigneeDetails.apply)(ConsigneeDetails.unapply)

  def form(): Form[ConsigneeDetails] = Form(mapping)
}
