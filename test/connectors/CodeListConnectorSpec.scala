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

package connectors

import java.util.Locale.{ENGLISH, JAPANESE}

import scala.collection.immutable.ListMap

import base.UnitWithMocksSpec
import config.AppConfig
import models.codes.{AdditionalProcedureCode, HolderOfAuthorisationCode, ProcedureCode}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach

class CodeListConnectorSpec extends UnitWithMocksSpec with BeforeAndAfterEach {

  private val appConfig = mock[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    when(appConfig.holderOfAuthorisationCodes).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.procedureCodesListFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.procedureCodesForC21ListFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.additionalProcedureCodes).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.additionalProcedureCodesForC21).thenReturn("/code-lists/manyCodes.json")
  }

  private lazy val codeListConnector = new FileBasedCodeListConnector(appConfig)

  "FileBasedCodeListConnector" should {
    "throw exception on initialisation" when {

      "code list file is missing" in {
        when(appConfig.procedureCodesListFile).thenReturn("")

        intercept[IllegalArgumentException](new FileBasedCodeListConnector(appConfig).getProcedureCodes(ENGLISH))
      }

      "code list file is malformed" in {
        when(appConfig.procedureCodesListFile).thenReturn("/code-lists/malformedCodes.json")

        intercept[IllegalArgumentException](new FileBasedCodeListConnector(appConfig).getProcedureCodes(ENGLISH))
      }

      "code list file is empty" in {
        when(appConfig.procedureCodesListFile).thenReturn("/code-lists/empty.json")

        intercept[IllegalArgumentException](new FileBasedCodeListConnector(appConfig).getProcedureCodes(ENGLISH))
      }
    }

    "return a map of procedure codes" when {

      "'ENGLISH' locale passed return codes with English descriptions" in {
        codeListConnector.getProcedureCodes(ENGLISH) must be(samplePCsEnglish)
      }

      "'WELSH' locale passed return codes with Welsh descriptions" in {
        codeListConnector.getProcedureCodes(codeListConnector.WELSH) must be(samplePCsWelsh)
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        codeListConnector.getProcedureCodes(JAPANESE) must be(samplePCsEnglish)
      }
    }

    "return a map of C21 procedure codes" when {

      "'ENGLISH' locale passed return codes with English descriptions" in {
        codeListConnector.getProcedureCodesForC21(ENGLISH) must be(samplePCsEnglish)
      }

      "'WELSH' locale passed return codes with Welsh descriptions" in {
        codeListConnector.getProcedureCodesForC21(codeListConnector.WELSH) must be(samplePCsWelsh)
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        codeListConnector.getProcedureCodes(JAPANESE) must be(samplePCsEnglish)
      }
    }

    "return a map of additional procedure codes" when {
      "'ENGLISH' locale passed return codes with English descriptions" in {
        codeListConnector.getAdditionalProcedureCodesMap(ENGLISH) must be(sampleAPCsEnglish)
      }

      "'WELSH' locale passed return codes with Welsh descriptions" in {
        codeListConnector.getAdditionalProcedureCodesMap(codeListConnector.WELSH) must be(sampleAPCsWelsh)
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        codeListConnector.getAdditionalProcedureCodesMap(JAPANESE) must be(sampleAPCsEnglish)
      }
    }

    "return a map of C21 additional procedure codes" when {
      "'ENGLISH' locale passed return codes with English descriptions" in {
        codeListConnector.getAdditionalProcedureCodesMapForC21(ENGLISH) must be(sampleAPCsEnglish)
      }

      "'WELSH' locale passed return codes with Welsh descriptions" in {
        codeListConnector.getAdditionalProcedureCodesMapForC21(codeListConnector.WELSH) must be(sampleAPCsWelsh)
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        codeListConnector.getAdditionalProcedureCodesMapForC21(JAPANESE) must be(sampleAPCsEnglish)
      }
    }

    "return a map of 'Holder of Authorisation' codes" when {
      "'ENGLISH' locale passed return codes with English descriptions" in {
        codeListConnector.getHolderOfAuthorisationCodes(ENGLISH) must be(sampleHACsEnglish)
      }

      "'WELSH' locale passed return codes with Welsh descriptions" in {
        codeListConnector.getHolderOfAuthorisationCodes(codeListConnector.WELSH) must be(sampleHACsWelsh)
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        codeListConnector.getHolderOfAuthorisationCodes(JAPANESE) must be(sampleHACsEnglish)
      }
    }

    "return a map of 'Holder of Authorisation' codes ordered as expected" when {
      "receives a supported language as input, or default to English for unsupported languages" in {
        when(appConfig.holderOfAuthorisationCodes).thenReturn("/code-lists/holderOfAuthorisationCodes.json")
        val codeListConnector = new FileBasedCodeListConnector(appConfig)
        (codeListConnector.supportedLanguages :+ JAPANESE).foreach { locale =>
          val codes = codeListConnector.getHolderOfAuthorisationCodes(locale).keys.toList
          codes.size mustBe 45

          codes(0) mustBe "ACP"
          codes(25) mustBe "UKCS"

          codes(26) mustBe "CGU"
          codes(40) mustBe "TST"

          codes(41) mustBe "ACE"
          codes(44) mustBe "TRD"
        }
      }
    }
  }

  private val samplePCsEnglish =
    ListMap("001" -> ProcedureCode("001", "English"), "002" -> ProcedureCode("002", "English"), "003" -> ProcedureCode("003", "English"))

  private val samplePCsWelsh =
    ListMap("001" -> ProcedureCode("001", "Welsh"), "002" -> ProcedureCode("002", "Welsh"), "003" -> ProcedureCode("003", "Welsh"))

  private val sampleAPCsEnglish = ListMap(
    "001" -> AdditionalProcedureCode("001", "English"),
    "002" -> AdditionalProcedureCode("002", "English"),
    "003" -> AdditionalProcedureCode("003", "English")
  )

  private val sampleAPCsWelsh = ListMap(
    "001" -> AdditionalProcedureCode("001", "Welsh"),
    "002" -> AdditionalProcedureCode("002", "Welsh"),
    "003" -> AdditionalProcedureCode("003", "Welsh")
  )

  private val sampleHACsEnglish = ListMap(
    "001" -> HolderOfAuthorisationCode("001", "English"),
    "002" -> HolderOfAuthorisationCode("002", "English"),
    "003" -> HolderOfAuthorisationCode("003", "English")
  )

  private val sampleHACsWelsh = ListMap(
    "001" -> HolderOfAuthorisationCode("001", "Welsh"),
    "002" -> HolderOfAuthorisationCode("002", "Welsh"),
    "003" -> HolderOfAuthorisationCode("003", "Welsh")
  )
}
