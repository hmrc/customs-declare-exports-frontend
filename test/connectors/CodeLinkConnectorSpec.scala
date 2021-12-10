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

import base.UnitWithMocksSpec
import config.AppConfig
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach

class CodeLinkConnectorSpec extends UnitWithMocksSpec with BeforeAndAfterEach {

  private val appConfig = mock[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    when(appConfig.procedureCodeToAdditionalProcedureCodesLinkFile).thenReturn("/code-links/manyLinks.json")
    when(appConfig.procedureCodeToAdditionalProcedureCodesC21LinkFile).thenReturn("/code-links/manyLinks.json")
    when(appConfig.countryCodeToAliasesLinkFile).thenReturn("/code-links/manyLinks.json")
    when(appConfig.countryCodeToShortNameLinkFile).thenReturn("/code-links/manyLinks.json")
  }

  private lazy val connector = new FileBasedCodeLinkConnector(appConfig)

  "FileBasedCodeListConnector" should {
    "throw exception on initialisation" when {
      "code link file is missing" in {
        when(appConfig.procedureCodeToAdditionalProcedureCodesLinkFile).thenReturn("")

        intercept[IllegalArgumentException](new FileBasedCodeLinkConnector(appConfig))
      }

      "code link file is malformed" in {
        when(appConfig.procedureCodeToAdditionalProcedureCodesLinkFile).thenReturn("/code-lists/malformedLinks.json")

        intercept[IllegalArgumentException](new FileBasedCodeLinkConnector(appConfig))
      }

      "code link file is empty" in {
        when(appConfig.procedureCodeToAdditionalProcedureCodesLinkFile).thenReturn("/code-lists/empty.json")

        intercept[IllegalArgumentException](new FileBasedCodeLinkConnector(appConfig))
      }
    }

    "return a list of valid additional procedure codes for non-C21 journeys" when {
      "the procedure code exists in the list" in {
        connector.getValidAdditionalProcedureCodesForProcedureCode("1040") must be(Some(List("C12", "F75")))
      }

      "the procedure code does not exists in the list" in {
        connector.getValidAdditionalProcedureCodesForProcedureCode("940") must be(None)
      }

      "the procedure code does not have any valid additional procedure codes" in {
        connector.getValidAdditionalProcedureCodesForProcedureCode("0000") must be(Some(List.empty[String]))
      }
    }

    "return a list of valid additional procedure codes for C21 journeys" when {
      "the procedure code exists in the list" in {
        connector.getValidAdditionalProcedureCodesForProcedureCodeC21("1040") must be(Some(List("C12", "F75")))
      }

      "the procedure code does not exists in the list" in {
        connector.getValidAdditionalProcedureCodesForProcedureCodeC21("940") must be(None)
      }

      "the procedure code does not have any valid additional procedure codes" in {
        connector.getValidAdditionalProcedureCodesForProcedureCode("0000") must be(Some(List.empty[String]))
      }
    }

    "return a list of aliases for a country" when {
      "the country code exists in the list" in {
        connector.getAliasesForCountryCode("1040") must be(Some(List("C12", "F75")))
      }

      "the country code does not exists in the list" in {
        connector.getAliasesForCountryCode("940") must be(None)
      }

      "the country code does not have any aliases" in {
        connector.getAliasesForCountryCode("0000") must be(Some(List.empty[String]))
      }
    }

    "return a list of short names for a country" when {
      "the country code exists in the list" in {
        connector.getShortNamesForCountryCode("1040") must be(Some(List("C12", "F75")))
      }

      "the country code does not exists in the list" in {
        connector.getShortNamesForCountryCode("940") must be(None)
      }

      "the country code does not have any short names" in {
        connector.getShortNamesForCountryCode("0000") must be(Some(List.empty[String]))
      }
    }
  }
}
