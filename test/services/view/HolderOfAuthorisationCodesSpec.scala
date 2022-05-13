/*
 * Copyright 2022 HM Revenue & Customs
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

package services.view

import base.UnitWithMocksSpec
import config.AppConfig
import connectors.FileBasedCodeListConnector
import forms.declaration.declarationHolder.DeclarationHolder
import mock.FeatureFlagMocks
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach

import java.util.Locale.ENGLISH

class HolderOfAuthorisationCodesSpec extends UnitWithMocksSpec with BeforeAndAfterEach with FeatureFlagMocks {

  private val appConfig = mock[AppConfig]

  private lazy val codeListConnector = new FileBasedCodeListConnector(appConfig)

  private lazy val holderOfAuthorisationCodes = new HolderOfAuthorisationCodes(codeListConnector, mockMerchandiseInBagConfig)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    when(appConfig.holderOfAuthorisationCodes).thenReturn("/code-lists/holderOfAuthorisationCodes.json")
  }

  "HolderOfAuthorisationCodes.asListOfAutoCompleteItems" should {
    "return 'Holder of Authorisation' codes as AutoCompleteItems" when {
      "merchandiseInBag feature is enabled" in {

        when(mockMerchandiseInBagConfig.isMerchandiseInBagEnabled).thenReturn(true)

        val autoCompleteItems = holderOfAuthorisationCodes.asListOfAutoCompleteItems(ENGLISH)
        autoCompleteItems.size mustBe 53

        val item0 = "EXRR - Submission of an arrived export declaration for RoRo movements (where CSE Authorisation is not used)"
        autoCompleteItems.head mustBe AutoCompleteItem(item0, "EXRR")

        autoCompleteItems(4) mustBe AutoCompleteItem("MIB - Merchandise in Baggage", "MIB")

        val item24 = "TEAH - Temporary Admission authorisation – Auction Houses (no guarantee for VAT required)"
        autoCompleteItems(25) mustBe AutoCompleteItem(item24, "TEAH")

        val item25 = "TST - Authorisation to operate storage facilities for the temporary storage of goods"
        autoCompleteItems(26) mustBe AutoCompleteItem(item25, "TST")

        autoCompleteItems(40) mustBe AutoCompleteItem("EPSS - Excise Payment Security System", "EPSS")

        val item40 = "ETD - Electronic Transport Document (authorised for use as a customs declaration)"
        autoCompleteItems(41) mustBe AutoCompleteItem(item40, "ETD")

        autoCompleteItems(45) mustBe AutoCompleteItem("REM - Remission of the amounts of import or export duty", "REM")
      }
      "merchandiseInBag feature is disabled" in {

        when(mockMerchandiseInBagConfig.isMerchandiseInBagEnabled).thenReturn(false)

        val autoCompleteItems = holderOfAuthorisationCodes.asListOfAutoCompleteItems(ENGLISH)
        autoCompleteItems.size mustBe 52

        autoCompleteItems must not contain AutoCompleteItem("MIB - Merchandise in Baggage", "MIB")
      }
    }
  }

  "HolderOfAuthorisationCodes.codeDescription" should {
    "return the description of a 'Holder of Authorisation' code in the expected format" in {
      val description = holderOfAuthorisationCodes.codeDescription(ENGLISH, "UKCS")
      description mustBe "UKCS - UK Continental Shelf"
    }
  }

  "HolderOfAuthorisationCodes.codeDescriptions" should {

    "return an empty sequence when 'holders' param is empty" in {
      val descriptions = holderOfAuthorisationCodes.codeDescriptions(ENGLISH, List.empty)
      descriptions mustBe Seq.empty
    }

    "return an empty sequence when 'holders' param does not contain auth codes" in {
      val holders = List(DeclarationHolder(None, None, None))
      val descriptions = holderOfAuthorisationCodes.codeDescriptions(ENGLISH, holders)
      descriptions mustBe Seq.empty
    }

    "return descriptions of 'Holder of Authorisation' codes in the expected format" in {
      val holders =
        List(DeclarationHolder(Some("APEX"), None, None), DeclarationHolder(None, None, None), DeclarationHolder(Some("UKCS"), None, None))
      val descriptions = holderOfAuthorisationCodes.codeDescriptions(ENGLISH, holders)
      descriptions.head mustBe "APEX - Approved Exporter"
      descriptions.last mustBe "UKCS - UK Continental Shelf"
    }
  }
}
