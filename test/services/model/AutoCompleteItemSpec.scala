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

package services.model

import services.view.AutoCompleteItem
import services.{DocumentType, HolderOfAuthorisationCode, NationalAdditionalCode}
import uk.gov.hmrc.play.test.UnitSpec

class AutoCompleteItemSpec extends UnitSpec {

  "AutoCompleteItem" should {

    "map from Country" when {
      "value is default" in {
        AutoCompleteItem.fromCountry(List(Country("name", "code"))) shouldBe List(AutoCompleteItem("name - code", "name"))
      }

      "value is specified" in {
        AutoCompleteItem.fromCountry(List(Country("name", "code")), _.countryCode) shouldBe List(
          AutoCompleteItem("name - code", "code")
        )
      }
    }

    "map from Package Type" in {
      AutoCompleteItem.fromPackageType(List(PackageType("code", "description"))) shouldBe List(
        AutoCompleteItem("description - code", "code")
      )
    }

    "map from Office Of Exit" in {
      AutoCompleteItem.fromOfficeOfExit(List(OfficeOfExit("code", "description"))) shouldBe List(
        AutoCompleteItem("description - code", "code")
      )
    }

    "map from document type" in {
      AutoCompleteItem.fromDocumentType(List(DocumentType("description", "code"))) shouldBe List(
        AutoCompleteItem("description - code", "code")
      )
    }

    "map from national additional code" in {
      AutoCompleteItem.fromNationalAdditionalCode(List(NationalAdditionalCode("code"))) shouldBe List(
        AutoCompleteItem("code", "code")
      )
    }

    "map from holder of authorisation code" in {
      AutoCompleteItem.fromHolderOfAuthorisationCode(List(HolderOfAuthorisationCode("code"))) shouldBe List(
        AutoCompleteItem("code", "code")
      )
    }

    "map from supervising customs office for Warehouse using Description - CODE" in {
      AutoCompleteItem.fromSupervisingCustomsOffice(List(CustomsOffice("code", "description"))) shouldBe List(
        AutoCompleteItem("description - code", "code")
      )
    }

    "map from supervising customs office for Office Of Exit using Description (CODE)" in {
      AutoCompleteItem.fromOfficeOfPresentation(List(CustomsOffice("code", "description"))) shouldBe List(
        AutoCompleteItem("description (code)", "code")
      )
    }
  }
}
