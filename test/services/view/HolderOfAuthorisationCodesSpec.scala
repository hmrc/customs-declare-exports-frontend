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

package services.view

import java.util.Locale.ENGLISH

import base.UnitSpec
import config.AppConfig
import connectors.FileBasedCodeListConnector
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach

class HolderOfAuthorisationCodesSpec extends UnitSpec with BeforeAndAfterEach {

  private val appConfig = mock[AppConfig]

  private val codeListConnector = new FileBasedCodeListConnector(appConfig)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    when(appConfig.holderOfAuthorisationCodes).thenReturn("/code-lists/holderOfAuthorisationCodes.json")
  }

  "Holder of Authorisation codes" should {

    "be available for all supported languages, or default to English" in {
      val holderOfAuthorisationCodes = new HolderOfAuthorisationCodes(appConfig, codeListConnector)

      Seq(ENGLISH, codeListConnector.WELSH).foreach {
        holderOfAuthorisationCodes.getCodes(_).size mustBe 43
      }
    }

    "ordered according to 3 groups " in {
      val holderOfAuthorisationCodes = new HolderOfAuthorisationCodes(appConfig, codeListConnector)
      val codes = holderOfAuthorisationCodes.getCodes(ENGLISH).toList

      codes(0)._2 must startWith("ACP - ")
      codes(24)._2 must startWith("UKCS - ")

      codes(25)._2 must startWith("CGU - ")
      codes(39)._2 must startWith("TST - ")

      codes(40)._2 must startWith("ACE - ")
      codes(42)._2 must startWith("TRD - ")
    }

    "be able to be provided as AutoCompleteItems " in {
      val holderOfAuthorisationCodes = new HolderOfAuthorisationCodes(appConfig, codeListConnector)
      val autoCompleteItems = holderOfAuthorisationCodes.asListOfAutoCompleteItems(ENGLISH)

      autoCompleteItems(0) mustBe AutoCompleteItem("ACP - Authorised issuer to establish the proof of the customs status of Union goods", "ACP")
      autoCompleteItems(24) mustBe AutoCompleteItem("UKCS - UK Continental Shelf", "UKCS")

      autoCompleteItems(25) mustBe AutoCompleteItem("CGU - Customs comprehensive guarantee", "CGU")
      autoCompleteItems(39) mustBe AutoCompleteItem("TST - Authorisation to operate storage facilities for the temporary storage of goods", "TST")

      autoCompleteItems(40) mustBe AutoCompleteItem("ACE - Authorised consignee for Union transit", "ACE")
      autoCompleteItems(42) mustBe AutoCompleteItem("TRD - Authorisation to use transit declaration with a reduced dataset", "TRD")
    }
  }
}
