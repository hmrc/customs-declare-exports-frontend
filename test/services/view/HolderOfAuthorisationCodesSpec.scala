/*
 * Copyright 2024 HM Revenue & Customs
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
import connectors.{FileBasedCodeListConnector, GoodsLocationCodesConnector}
import forms.section2.authorisationHolder.AuthorisationHolder
import forms.section2.authorisationHolder.AuthorizationTypeCodes.{EXRR, MIB}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.{Environment, Mode}
import utils.JsonFile

import java.util.Locale.ENGLISH

class HolderOfAuthorisationCodesSpec extends UnitWithMocksSpec with BeforeAndAfterEach {

  private val appConfig = mock[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    when(appConfig.holderOfAuthorisationCodeFile).thenReturn("/code-lists/holder-of-authorisation-codes/holder-of-authorisation-codes.json")
    when(appConfig.procedureCodesListFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.procedureCodesForC21ListFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.additionalProcedureCodes).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.additionalProcedureCodesForC21).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.dmsErrorCodes).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.countryCodes).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.packageTypeCodeFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.officeOfExitsCodeFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.customsOfficesCodeFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.documentTypeCodeFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.currencyCodesFile).thenReturn("/code-lists/manyCodes.json")
  }

  private lazy val glc = mock[GoodsLocationCodesConnector]
  private lazy val jsonFile = new JsonFile(Environment.simple(mode = Mode.Test))
  private lazy val codeListConnector = new FileBasedCodeListConnector(appConfig, glc, jsonFile)
  private lazy val holderOfAuthorisationCodes = new HolderOfAuthorisationCodes(codeListConnector)

  "HolderOfAuthorisationCodes.asListOfAutoCompleteItems" should {
    "return 'Holder of Authorisation' codes as AutoCompleteItems" in {
      val autoCompleteItems = holderOfAuthorisationCodes.asListOfAutoCompleteItems(ENGLISH)
      autoCompleteItems.size mustBe 57

      val item0 = "EXRR - Submission of an arrived export declaration for RoRo movements (where CSE Authorisation is not used)"
      autoCompleteItems(28) mustBe AutoCompleteItem(item0, EXRR)

      autoCompleteItems(38) mustBe AutoCompleteItem("MIB - Merchandise in Baggage", MIB)

      val item24 = "TEAH - Temporary Admission authorisation – Auction Houses (no guarantee for VAT required)"
      autoCompleteItems(51) mustBe AutoCompleteItem(item24, "TEAH")

      val item25 = "TST - Authorisation to operate storage facilities for the temporary storage of goods"
      autoCompleteItems(53) mustBe AutoCompleteItem(item25, "TST")

      autoCompleteItems(23) mustBe AutoCompleteItem("EPSS - Excise Payment Security System", "EPSS")

      val item40 = "ETD - Electronic Transport Document (authorised for use as a customs declaration)"
      autoCompleteItems(24) mustBe AutoCompleteItem(item40, "ETD")

      autoCompleteItems(41) mustBe AutoCompleteItem("REM - Remission of the amounts of import or export duty", "REM")
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
      val holders = List(AuthorisationHolder(None, None, None))
      val descriptions = holderOfAuthorisationCodes.codeDescriptions(ENGLISH, holders)
      descriptions mustBe Seq.empty
    }

    "return descriptions of 'Holder of Authorisation' codes in the expected format" in {
      val holders =
        List(AuthorisationHolder(Some("APEX"), None, None), AuthorisationHolder(None, None, None), AuthorisationHolder(Some("UKCS"), None, None))
      val descriptions = holderOfAuthorisationCodes.codeDescriptions(ENGLISH, holders)
      descriptions.head mustBe "APEX - Approved Exporter"
      descriptions.last mustBe "UKCS - UK Continental Shelf"
    }
  }
}
