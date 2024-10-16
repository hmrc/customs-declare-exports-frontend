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

package connectors

import base.UnitWithMocksSpec
import config.AppConfig
import forms.section2.authorisationHolder.AuthorizationTypeCodes.MIB
import models.codes._
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.{Environment, Mode}
import services.model.{CustomsOffice, OfficeOfExit, PackageType}
import services.DocumentType
import utils.JsonFile

import java.util.Locale.{ENGLISH, JAPANESE}
import scala.collection.immutable.ListMap

class CodeListConnectorSpec extends UnitWithMocksSpec with BeforeAndAfterEach {

  private val appConfig = mock[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    when(appConfig.holderOfAuthorisationCodeFile).thenReturn("/code-lists/manyCodes.json")
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

  "FileBasedCodeListConnector" should {
    "throw exception on initialisation" when {

      "code list file is missing" in {
        when(appConfig.procedureCodesListFile).thenReturn("")

        intercept[IllegalArgumentException](new FileBasedCodeListConnector(appConfig, glc, jsonFile).getProcedureCodes(ENGLISH))
      }

      "code list file is malformed" in {
        when(appConfig.procedureCodesListFile).thenReturn("/code-lists/malformedCodes.json")

        intercept[IllegalArgumentException](new FileBasedCodeListConnector(appConfig, glc, jsonFile).getProcedureCodes(ENGLISH))
      }

      "code list file is empty" in {
        when(appConfig.procedureCodesListFile).thenReturn("/code-lists/empty.json")

        intercept[IllegalArgumentException](new FileBasedCodeListConnector(appConfig, glc, jsonFile).getProcedureCodes(ENGLISH))
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
        when(appConfig.holderOfAuthorisationCodeFile).thenReturn("/code-lists/holder-of-authorisation-codes/holder-of-authorisation-codes.json")
        val codeListConnector = new FileBasedCodeListConnector(appConfig, glc, jsonFile)
        (codeListConnector.supportedLanguages :+ JAPANESE).foreach { locale =>
          val codes = codeListConnector.getHolderOfAuthorisationCodes(locale).keys.toList
          codes.size mustBe 57

          codes.head mustBe "ACE"

          codes(38) mustBe MIB

          codes(53) mustBe "TST"

          codes(41) mustBe "REM"
          codes(24) mustBe "ETD"

          codes(31) mustBe "FAS"
          codes(42) mustBe "REP"

          codes(54) mustBe "UKCS"
        }
      }
    }

    "return a map of 'DMS Error' codes" when {
      "'ENGLISH' locale passed return codes with English descriptions" in {
        codeListConnector.getDmsErrorCodesMap(ENGLISH) must be(sampleDmsErrorsEnglish)
      }

      "'WELSH' locale passed return codes with Welsh descriptions" in {
        codeListConnector.getDmsErrorCodesMap(codeListConnector.WELSH) must be(sampleDmsErrorsWelsh)
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        codeListConnector.getDmsErrorCodesMap(JAPANESE) must be(sampleDmsErrorsEnglish)
      }
    }

    "return a map of 'Country' codes" when {
      "'ENGLISH' locale passed return codes with English descriptions" in {
        codeListConnector.getCountryCodes(ENGLISH) must be(sampleCountriesEnglish)
      }

      "'WELSH' locale passed return codes with Welsh descriptions" in {
        codeListConnector.getCountryCodes(codeListConnector.WELSH) must be(sampleCountriesWelsh)
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        codeListConnector.getCountryCodes(JAPANESE) must be(sampleCountriesEnglish)
      }
    }

    "return a map of Package Type Codes" when {
      "'ENGLISH' locale passed return codes with English descriptions" in {
        codeListConnector.getPackageTypes(ENGLISH) must be(samplePtcsEnglish)
      }

      "'WELSH' local passed return codes with Welsh descriptions" in {
        codeListConnector.getPackageTypes(codeListConnector.WELSH) must be(samplePtcsWelsh)
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        codeListConnector.getPackageTypes(JAPANESE) must be(samplePtcsEnglish)
      }
    }

    "return a map of Office of Exit Codes" when {
      "'ENGLISH' locale passed return codes with English descriptions" in {
        codeListConnector.getOfficeOfExits(ENGLISH) must be(sampleOocsEnglish)
      }

      "'WELSH' local passed return codes with Welsh descriptions" in {
        codeListConnector.getOfficeOfExits(codeListConnector.WELSH) must be(sampleOocsWelsh)
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        codeListConnector.getOfficeOfExits(JAPANESE) must be(sampleOocsEnglish)
      }
    }

    "return a map of Customs Office Codes" when {
      "'ENGLISH' locale passed return codes with English descriptions" in {
        codeListConnector.getCustomsOffices(ENGLISH) must be(sampleCocsEnglish)
      }

      "'WELSH' local passed return codes with Welsh descriptions" in {
        codeListConnector.getCustomsOffices(codeListConnector.WELSH) must be(sampleCocsWelsh)
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        codeListConnector.getCustomsOffices(JAPANESE) must be(sampleCocsEnglish)
      }
    }

    "return a map of Document Type Codes" when {
      "'ENGLISH' locale passed return codes with English descriptions" in {
        codeListConnector.getDocumentTypes(ENGLISH) must be(sampleDocTypesEnglish)
      }

      "'WELSH' local passed return codes with Welsh descriptions" in {
        codeListConnector.getDocumentTypes(codeListConnector.WELSH) must be(sampleDocTypesWelsh)
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        codeListConnector.getDocumentTypes(JAPANESE) must be(sampleDocTypesEnglish)
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

  private val sampleDmsErrorsEnglish =
    ListMap("001" -> DmsErrorCode("001", "English"), "002" -> DmsErrorCode("002", "English"), "003" -> DmsErrorCode("003", "English"))

  private val sampleDmsErrorsWelsh =
    ListMap("001" -> DmsErrorCode("001", "Welsh"), "002" -> DmsErrorCode("002", "Welsh"), "003" -> DmsErrorCode("003", "Welsh"))

  private val sampleCountriesEnglish =
    ListMap("001" -> Country("English", "001"), "002" -> Country("English", "002"), "003" -> Country("English", "003"))

  private val sampleCountriesWelsh =
    ListMap("001" -> Country("Welsh", "001"), "002" -> Country("Welsh", "002"), "003" -> Country("Welsh", "003"))

  private val samplePtcsEnglish =
    ListMap("001" -> PackageType("001", "English"), "002" -> PackageType("002", "English"), "003" -> PackageType("003", "English"))

  private val samplePtcsWelsh =
    ListMap("001" -> PackageType("001", "Welsh"), "002" -> PackageType("002", "Welsh"), "003" -> PackageType("003", "Welsh"))

  private val sampleOocsEnglish =
    ListMap("001" -> OfficeOfExit("001", "English"), "002" -> OfficeOfExit("002", "English"), "003" -> OfficeOfExit("003", "English"))

  private val sampleOocsWelsh =
    ListMap("001" -> OfficeOfExit("001", "Welsh"), "002" -> OfficeOfExit("002", "Welsh"), "003" -> OfficeOfExit("003", "Welsh"))

  private val sampleCocsEnglish =
    ListMap("001" -> CustomsOffice("001", "English"), "002" -> CustomsOffice("002", "English"), "003" -> CustomsOffice("003", "English"))

  private val sampleCocsWelsh =
    ListMap("001" -> CustomsOffice("001", "Welsh"), "002" -> CustomsOffice("002", "Welsh"), "003" -> CustomsOffice("003", "Welsh"))

  private val sampleDocTypesEnglish =
    ListMap("001" -> DocumentType("English", "001"), "002" -> DocumentType("English", "002"), "003" -> DocumentType("English", "003"))

  private val sampleDocTypesWelsh =
    ListMap("001" -> DocumentType("Welsh", "001"), "002" -> DocumentType("Welsh", "002"), "003" -> DocumentType("Welsh", "003"))
}
