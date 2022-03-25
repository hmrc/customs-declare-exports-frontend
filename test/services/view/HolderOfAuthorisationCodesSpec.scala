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

import java.util.Locale.ENGLISH

import base.UnitWithMocksSpec
import config.AppConfig
import connectors.FileBasedCodeListConnector
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach

class HolderOfAuthorisationCodesSpec extends UnitWithMocksSpec with BeforeAndAfterEach {

  private val appConfig = mock[AppConfig]

  private lazy val codeListConnector = new FileBasedCodeListConnector(appConfig)

  private lazy val holderOfAuthorisationCodes = new HolderOfAuthorisationCodes(codeListConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    when(appConfig.holderOfAuthorisationCodes).thenReturn("/code-lists/holderOfAuthorisationCodes.json")
    when(appConfig.procedureCodesListFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.procedureCodesForC21ListFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.additionalProcedureCodes).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.additionalProcedureCodesForC21).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.dmsErrorCodes).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.countryCodes).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.goodsLocationCodeFile).thenReturn("/code-lists/manyCodes.json")
  }

  "HolderOfAuthorisationCodes.codeDescription" should {
    "return the description of a 'Holder of Authorisation' code in the expected format" in {
      val description = holderOfAuthorisationCodes.codeDescription(ENGLISH, "UKCS")
      description mustBe "UKCS - UK Continental Shelf"
    }
  }

  "HolderOfAuthorisationCodes.asListOfAutoCompleteItems" should {
    "return 'Holder of Authorisation' codes as AutoCompleteItems" in {
      val autoCompleteItems = holderOfAuthorisationCodes.asListOfAutoCompleteItems(ENGLISH)
      autoCompleteItems.size mustBe 45

      autoCompleteItems(0) mustBe AutoCompleteItem("ACP - Authorised issuer to establish the proof of the customs status of Union goods", "ACP")
      autoCompleteItems(24) mustBe AutoCompleteItem("UKCS - UK Continental Shelf", "UKCS")

      autoCompleteItems(25) mustBe AutoCompleteItem("CGU - Customs comprehensive guarantee", "CGU")
      autoCompleteItems(39) mustBe AutoCompleteItem("TST - Authorisation to operate storage facilities for the temporary storage of goods", "TST")

      autoCompleteItems(40) mustBe AutoCompleteItem("ACE - Authorised consignee for Union transit", "ACE")
      autoCompleteItems(44) mustBe AutoCompleteItem("FP - Freeports Special Procedure", "FP")
    }
  }
}
