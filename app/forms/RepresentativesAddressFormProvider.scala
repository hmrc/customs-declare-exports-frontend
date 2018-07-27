/*
 * Copyright 2018 HM Revenue & Customs
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

package forms

import forms.mappings.Mappings
import javax.inject.Inject
import models.Address
import play.api.data.Form
import play.api.data.Forms._

class RepresentativesAddressFormProvider @Inject() extends FormErrorHelper with Mappings {

  def apply(): Form[Address] =
    Form(
      mapping(
        "fullName" -> text("address.error.required.fullName"),
        "building" -> text("address.error.required.building"),
        "street" -> text("address.error.required.street"),
        "townOrCity" -> text("address.error.required.townOrCity"),
        "county" -> optional(text()),
        "postcode" -> text("address.error.required.postcode"),
        "country" -> text("address.error.required.country")
      )(Address.apply)(Address.unapply)
    )
}
