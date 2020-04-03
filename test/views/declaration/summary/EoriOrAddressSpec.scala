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

package views.declaration.summary

import forms.common.{Address, Eori}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Empty, HtmlContent, Text}
import views.declaration.spec.UnitViewSpec

class EoriOrAddressSpec extends UnitViewSpec {

  val pageId = "pageID"
  val eori = Eori("GB12345678")
  val eoriLabel = "eoriLabel"
  val fullName = "FullName"
  val street = "Street"
  val city = "City"
  val postCode = "Postcode"
  val country = "Country"
  val address = Address(fullName, street, city, postCode, country)
  val addressLabel = "addressLabel"
  val eoriChangeLabel = "eori-change"
  val addressChangeLabel = "address-change"

  def eoriOrAddress(eori: Option[Eori], address: Option[Address], isEoriDefault: Boolean = false) =
    EoriOrAddress
      .rows(
        key = "test",
        eori = eori,
        address = address,
        eoriLabel = eoriLabel,
        eoriChangeLabel = eoriChangeLabel,
        addressLabel = addressLabel,
        addressChangeLabel = addressChangeLabel,
        changeController = controllers.declaration.routes.ConsigneeDetailsController.displayPage(),
        isEoriDefault = isEoriDefault
      )
      .flatten

  "EoriOrAddress" should {
    "return eori row without value if eori and address is empty and eori is default row" in {

      val rows = eoriOrAddress(None, None, true)
      rows.size mustBe 1
      rows(0).key.content mustBe Text(eoriLabel)
      rows(0).value.content mustBe Empty
    }

    "return address row without value if eori and address is empty and eori is not default row" in {

      val rows = eoriOrAddress(None, None, false)
      rows.size mustBe 1
      rows(0).key.content mustBe Text(addressLabel)
      rows(0).value.content mustBe Empty
    }

    "return eori row with value if eori supplied and address is empty" in {

      val rows = eoriOrAddress(Some(eori), None)
      rows.size mustBe 1
      rows(0).key.content mustBe Text(eoriLabel)
      rows(0).value.content mustBe Text("GB12345678")
    }

    "return address row with value if address supplied and eori is empty" in {

      val rows = eoriOrAddress(None, Some(address))
      rows.size mustBe 1
      rows(0).key.content mustBe Text(addressLabel)
      rows(0).value.content mustBe HtmlContent(s"$fullName<br>$street<br>$city<br>$postCode<br>$country")
    }

    "return eori and address rows with value if both supplied" in {

      val rows = eoriOrAddress(Some(eori), Some(address))
      rows.size mustBe 2
      rows(0).key.content mustBe Text(eoriLabel)
      rows(0).value.content mustBe Text("GB12345678")
      rows(1).key.content mustBe Text(addressLabel)
      rows(1).value.content mustBe HtmlContent(s"$fullName<br>$street<br>$city<br>$postCode<br>$country")
    }
  }
}
