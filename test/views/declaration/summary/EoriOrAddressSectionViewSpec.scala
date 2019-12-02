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

package views.declaration.summary

import forms.common.Address
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.eori_or_address_section

class EoriOrAddressSectionViewSpec extends UnitViewSpec with ExportsTestData {

  val pageId = "pageID"
  val eori = "eori1"
  val eoriLabel = "eoriLabel"
  val fullName = "FullName"
  val street = "Street"
  val city = "City"
  val postCode = "Postcode"
  val country = "Country"
  val address = Address(fullName, street, city, postCode, country)
  val addressLabel = "addressLabel"

  "Eori or address section" should {

    "display eori row without value if eori and address is empty and eori is default row" in {

      val view = eori_or_address_section(pageId, None, eoriLabel, None, addressLabel)(messages, journeyRequest())

      view.getElementById(s"$pageId-eori-label").text() mustBe messages(eoriLabel)
      view.getElementById(s"$pageId-eori").text() mustBe empty
    }

    "display address row without value if eori and address is empty and eori is not default row" in {

      val view = eori_or_address_section(pageId, None, eoriLabel, None, addressLabel, false)(messages, journeyRequest())

      view.getElementById(s"$pageId-address-label").text() mustBe messages(addressLabel)
      view.getElementById(s"$pageId-address").text() mustBe empty
    }

    "display eori when defined" in {

      val view = eori_or_address_section(pageId, Some(eori), eoriLabel, None, addressLabel)(messages, journeyRequest())

      view.getElementById(s"$pageId-eori-label").text() mustBe messages(eoriLabel)
      view.getElementById(s"$pageId-eori").text() mustBe eori
    }

    "display address when defined" in {

      val view = eori_or_address_section(pageId, None, eoriLabel, Some(address), addressLabel)(messages, journeyRequest())

      view.getElementById(s"$pageId-address-label").text() mustBe messages(addressLabel)
      view.getElementById(s"$pageId-address-0").text() mustBe fullName
      view.getElementById(s"$pageId-address-1").text() mustBe street
      view.getElementById(s"$pageId-address-2").text() mustBe city
      view.getElementById(s"$pageId-address-3").text() mustBe postCode
      view.getElementById(s"$pageId-address-4").text() mustBe country
    }

    "display both eori and address when defined" in {

      val view = eori_or_address_section(pageId, Some(eori), eoriLabel, Some(address), addressLabel)(messages, journeyRequest())

      view.getElementById(s"$pageId-eori-label").text() mustBe messages(eoriLabel)
      view.getElementById(s"$pageId-eori").text() mustBe eori
      view.getElementById(s"$pageId-address-label").text() mustBe messages(addressLabel)
      view.getElementById(s"$pageId-address-0").text() mustBe fullName
      view.getElementById(s"$pageId-address-1").text() mustBe street
      view.getElementById(s"$pageId-address-2").text() mustBe city
      view.getElementById(s"$pageId-address-3").text() mustBe postCode
      view.getElementById(s"$pageId-address-4").text() mustBe country
    }
  }
}
