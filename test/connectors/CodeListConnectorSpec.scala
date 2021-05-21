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
import models.codes.ProcedureCode
import org.mockito.Mockito.{reset, when}

import java.util.Locale.ENGLISH

class CodeListConnectorSpec extends ConnectorSpec {

  private val appConfig = mock[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    when(appConfig.procedureCodeListFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.procedureCodeForC21ListFile).thenReturn("/code-lists/manyCodes.json")
  }

  "FileBasedCodeListConnector" should {
    "throw exception on initialisation" when {
      "code list file is missing" in {
        when(appConfig.procedureCodeListFile).thenReturn("")

        intercept[IllegalArgumentException](new FileBasedCodeListConnector(appConfig))
      }

      "code list file is malformed" in {
        when(appConfig.procedureCodeListFile).thenReturn("/code-lists/malformedCodes.json")

        intercept[IllegalArgumentException](new FileBasedCodeListConnector(appConfig))
      }
    }

    "return a list of procedure codes" when {
      "'ENGLISH' locale passed with English descriptions" in {
        val connector = new FileBasedCodeListConnector(appConfig)
        connector.getProcedureCodes(ENGLISH) must be(
          List(ProcedureCode("001", "English"), ProcedureCode("002", "English"), ProcedureCode("003", "English"))
        )
      }

      "'WELSH' locale passed with Welsh descriptions" in {
        val connector = new FileBasedCodeListConnector(appConfig)
        connector.getProcedureCodes(connector.WELSH) must be(
          List(ProcedureCode("001", "Welsh"), ProcedureCode("002", "Welsh"), ProcedureCode("003", "Welsh"))
        )
      }
    }

    "return a list of C21 procedure codes" when {
      "'ENGLISH' locale passed with English descriptions" in {
        val connector = new FileBasedCodeListConnector(appConfig)
        connector.getProcedureCodesForC21(ENGLISH) must be(
          List(ProcedureCode("001", "English"), ProcedureCode("002", "English"), ProcedureCode("003", "English"))
        )
      }

      "'WELSH' locale passed with Welsh descriptions" in {
        val connector = new FileBasedCodeListConnector(appConfig)
        connector.getProcedureCodesForC21(connector.WELSH) must be(
          List(ProcedureCode("001", "Welsh"), ProcedureCode("002", "Welsh"), ProcedureCode("003", "Welsh"))
        )
      }
    }
  }
}
