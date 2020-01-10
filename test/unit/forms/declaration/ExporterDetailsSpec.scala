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

package unit.forms.declaration

import forms.LightFormMatchers
import forms.declaration.ExporterDetails
import org.scalatest.{MustMatchers, WordSpec}

class ExporterDetailsSpec extends WordSpec with MustMatchers with LightFormMatchers {

  import forms.declaration.ExporterDetailsSpec._

  "Exporter Details" should {
    "have correct id" in {
      ExporterDetails.id mustEqual "ExporterDetails"
    }
  }

  "Exporter Details form" should {
    "validate is eori and address is non empty" in {
      ExporterDetails.form().bind(emptyExporterDetailsJSON).error("details") must haveMessage("supplementary.namedEntityDetails.error")
    }
    val outcomeFromIncorrectForm = ExporterDetails.form().bind(incorrectExporterDetailsJSON)
    "validate eori and address" in {
      outcomeFromIncorrectForm.error("details.eori") must haveMessage("supplementary.eori.error.format")
    }
    "validate address fullname" in {
      outcomeFromIncorrectForm.error("details.address.fullName") must haveMessage("supplementary.address.fullName.error")
    }
    "validate address addresline" in {
      outcomeFromIncorrectForm.error("details.address.addressLine") must haveMessage("supplementary.address.addressLine.error")
    }
    "validate town or city" in {
      outcomeFromIncorrectForm.error("details.address.townOrCity") must haveMessage("supplementary.address.townOrCity.error")
    }
    "validate post code" in {
      outcomeFromIncorrectForm.error("details.address.postCode") must haveMessage("supplementary.address.postCode.error")
    }
    "validate country" in {
      outcomeFromIncorrectForm.error("details.address.country") must haveMessage("supplementary.address.country.error")
    }

    "bind correctly to EORI only request" in {
      ExporterDetails.form().bind(correctExporterDetailsEORIOnlyJSON) mustBe errorless
    }

    "bind correctly to address only data" in {
      ExporterDetails.form().bind(correctExporterDetailsAddressOnlyJSON) mustBe errorless
    }

    "bind correctly to EORI and address data at once" in {
      ExporterDetails.form().bind(correctExporterDetailsJSON) mustBe errorless
    }
  }
}
