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

import config.AppConfig
import models.codes.{AdditionalProcedureCode, ProcedureCode}
import org.mockito.Mockito.{reset, when}

import java.util.Locale.{ENGLISH, JAPANESE}

class CodeListConnectorSpec extends ConnectorSpec {

  private val appConfig = mock[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    when(appConfig.procedureCodesListFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.procedureCodesForC21ListFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.additionalProcedureCodes).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.additionalProcedureCodesForC21).thenReturn("/code-lists/manyCodes.json")
  }

  private lazy val connector = new FileBasedCodeListConnector(appConfig)

  "FileBasedCodeListConnector" should {
    "throw exception on initialisation" when {
      "code list file is missing" in {
        when(appConfig.procedureCodesListFile).thenReturn("")

        intercept[IllegalArgumentException](new FileBasedCodeListConnector(appConfig))
      }

      "code list file is malformed" in {
        when(appConfig.procedureCodesListFile).thenReturn("/code-lists/malformedCodes.json")

        intercept[IllegalArgumentException](new FileBasedCodeListConnector(appConfig))
      }

      "code list file is empty" in {
        when(appConfig.procedureCodesListFile).thenReturn("/code-lists/empty.json")

        intercept[IllegalArgumentException](new FileBasedCodeListConnector(appConfig))
      }
    }

    "return a list of procedure codes" when {
      "'ENGLISH' locale passed return codes with English descriptions" in {
        connector.getProcedureCodes(ENGLISH) must be(
          List(ProcedureCode("001", "English"), ProcedureCode("002", "English"), ProcedureCode("003", "English"))
        )
      }

      "'WELSH' locale passed return codes with Welsh descriptions" in {
        connector.getProcedureCodes(connector.WELSH) must be(
          List(ProcedureCode("001", "Welsh"), ProcedureCode("002", "Welsh"), ProcedureCode("003", "Welsh"))
        )
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        connector.getProcedureCodes(JAPANESE) must be(
          List(ProcedureCode("001", "English"), ProcedureCode("002", "English"), ProcedureCode("003", "English"))
        )
      }
    }

    "return a list of C21 procedure codes" when {
      "'ENGLISH' locale passed return codes with English descriptions" in {
        connector.getProcedureCodesForC21(ENGLISH) must be(
          List(ProcedureCode("001", "English"), ProcedureCode("002", "English"), ProcedureCode("003", "English"))
        )
      }

      "'WELSH' locale passed return codes with Welsh descriptions" in {
        connector.getProcedureCodesForC21(connector.WELSH) must be(
          List(ProcedureCode("001", "Welsh"), ProcedureCode("002", "Welsh"), ProcedureCode("003", "Welsh"))
        )
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        connector.getProcedureCodes(JAPANESE) must be(
          List(ProcedureCode("001", "English"), ProcedureCode("002", "English"), ProcedureCode("003", "English"))
        )
      }
    }

    "return a list of additional procedure codes" when {
      "'ENGLISH' locale passed return codes with English descriptions" in {
        connector.getAdditionalProcedureCodes(ENGLISH) must be(
          List(AdditionalProcedureCode("001", "English"), AdditionalProcedureCode("002", "English"), AdditionalProcedureCode("003", "English"))
        )
      }

      "'WELSH' locale passed return codes with Welsh descriptions" in {
        connector.getAdditionalProcedureCodes(connector.WELSH) must be(
          List(AdditionalProcedureCode("001", "Welsh"), AdditionalProcedureCode("002", "Welsh"), AdditionalProcedureCode("003", "Welsh"))
        )
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        connector.getProcedureCodes(JAPANESE) must be(
          List(ProcedureCode("001", "English"), ProcedureCode("002", "English"), ProcedureCode("003", "English"))
        )
      }
    }

    "return a list of C21 additional procedure codes" when {
      "'ENGLISH' locale passed return codes with English descriptions" in {
        connector.getAdditionalProcedureCodesForC21(ENGLISH) must be(
          List(AdditionalProcedureCode("001", "English"), AdditionalProcedureCode("002", "English"), AdditionalProcedureCode("003", "English"))
        )
      }

      "'WELSH' locale passed return codes with Welsh descriptions" in {
        connector.getAdditionalProcedureCodesForC21(connector.WELSH) must be(
          List(AdditionalProcedureCode("001", "Welsh"), AdditionalProcedureCode("002", "Welsh"), AdditionalProcedureCode("003", "Welsh"))
        )
      }

      "unsupported 'JAPANESE' locale is passed return codes with English descriptions" in {
        connector.getProcedureCodes(JAPANESE) must be(
          List(ProcedureCode("001", "English"), ProcedureCode("002", "English"), ProcedureCode("003", "English"))
        )
      }
    }
  }
}
