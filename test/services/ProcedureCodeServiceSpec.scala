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

package services

import base.UnitWithMocksSpec
import connectors.{CodeLinkConnector, CodeListConnector}
import mock.FeatureFlagMocks
import models.DeclarationType
import models.codes.{AdditionalProcedureCode, ProcedureCode}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import services.ProcedureCodeServiceSpec._

import java.util.Locale
import java.util.Locale.ENGLISH
import scala.collection.immutable.ListMap

class ProcedureCodeServiceSpec extends UnitWithMocksSpec with BeforeAndAfterEach with FeatureFlagMocks {

  private val codeListConnector = mock[CodeListConnector]
  private val codeLinkConnector = mock[CodeLinkConnector]

  private val service = new ProcedureCodeService(codeListConnector, codeLinkConnector, mockMerchandiseInBagConfig)

  private val nonC21Journeys = DeclarationType.values.filter(_ != DeclarationType.CLEARANCE)

  override def beforeEach(): Unit = {
    reset(codeListConnector, codeLinkConnector)

    when(codeListConnector.getProcedureCodes(any())).thenReturn(sampleProcedureCodes)
    when(codeListConnector.getProcedureCodesForC21(any())).thenReturn(sampleC21ProcedureCodes)

    when(codeListConnector.getAdditionalProcedureCodesMap(any())).thenReturn(additionalProcedureCodesMap)
    when(codeListConnector.getAdditionalProcedureCodesMapForC21(any())).thenReturn(c21AdditionalProcedureCodesMap)

    when(codeLinkConnector.getValidAdditionalProcedureCodesForProcedureCode(any())).thenReturn(None)
    when(codeLinkConnector.getValidAdditionalProcedureCodesForProcedureCodeC21(any())).thenReturn(None)
    when(codeLinkConnector.getValidAdditionalProcedureCodesForProcedureCode(meq(sampleProcedureCode1.code)))
      .thenReturn(Some(validAdditionalCodesForProcedureCode1))
    when(codeLinkConnector.getValidAdditionalProcedureCodesForProcedureCodeC21(meq(sampleC21ProcedureCode1.code)))
      .thenReturn(Some(validAdditionalCodesForC21ProcedureCode1))
  }

  "ProcedureCodeService getProcedureCodesFor" should {
    Seq(false, true).foreach { eidr =>
      nonC21Journeys.foreach { decType =>
        s"return list of all procedure codes for ${decType} journey where EIRD is ${eidr}" in {
          service.getProcedureCodesFor(decType, eidr, ENGLISH) must equal(Seq(sampleProcedureCode1, sampleProcedureCode2))
        }
      }
    }

    s"return list of all C21 procedure codes for ${DeclarationType.CLEARANCE} journey when EIRD is false" in {
      service.getProcedureCodesFor(DeclarationType.CLEARANCE, false, ENGLISH) must equal(Seq(sampleC21ProcedureCode1, sampleC21ProcedureCode2))
    }

    s"return list of all procedure codes for ${DeclarationType.CLEARANCE} journey when EIRD is true" in {
      service.getProcedureCodesFor(DeclarationType.CLEARANCE, true, ENGLISH) must equal(Seq(sampleProcedureCode1, sampleProcedureCode2))
    }

    s"return the same procedure codes irrespective of the Locale's country or variant" in {
      service.getProcedureCodesFor(DeclarationType.CLEARANCE, true, ENGLISH) must equal(Seq(sampleProcedureCode1, sampleProcedureCode2))
      service.getProcedureCodesFor(DeclarationType.CLEARANCE, true, new Locale(ENGLISH.getCountry, "gb")) must equal(
        Seq(sampleProcedureCode1, sampleProcedureCode2)
      )
      service.getProcedureCodesFor(DeclarationType.CLEARANCE, true, new Locale(ENGLISH.getCountry, "gb", "scouse")) must equal(
        Seq(sampleProcedureCode1, sampleProcedureCode2)
      )
    }
  }

  "ProcedureCodeService getProcedureCodeFor" should {
    Seq(false, true).foreach { eidr =>
      nonC21Journeys.foreach { decType =>
        s"return the ProcedureCode entity for the given code string for ${decType} journey where EIRD is ${eidr} " in {
          service.getProcedureCodeFor(sampleProcedureCode1.code, decType, eidr, ENGLISH) must equal(Some(sampleProcedureCode1))
        }
      }
    }

    s"return the C21 ProcedureCode entity for the given code string for ${DeclarationType.CLEARANCE} journey when EIRD is false" in {
      service.getProcedureCodeFor(sampleC21ProcedureCode1.code, DeclarationType.CLEARANCE, false, ENGLISH) must equal(Some(sampleC21ProcedureCode1))
    }

    s"return the ProcedureCode entity for the given code string for ${DeclarationType.CLEARANCE} journey when EIRD is true" in {
      service.getProcedureCodeFor(sampleProcedureCode1.code, DeclarationType.CLEARANCE, true, ENGLISH) must equal(Some(sampleProcedureCode1))
    }

    s"return the same ProcedureCode entity irrespective of the Locale's country or variant" in {
      service.getProcedureCodeFor(sampleProcedureCode1.code, DeclarationType.CLEARANCE, true, ENGLISH) must equal(Some(sampleProcedureCode1))
      service.getProcedureCodeFor(sampleProcedureCode1.code, DeclarationType.CLEARANCE, true, new Locale(ENGLISH.getCountry, "gb")) must equal(
        Some(sampleProcedureCode1)
      )
      service.getProcedureCodeFor(
        sampleProcedureCode1.code,
        DeclarationType.CLEARANCE,
        true,
        new Locale(ENGLISH.getCountry, "gb", "scouse")
      ) must equal(Some(sampleProcedureCode1))
    }
  }

  "ProcedureCodeService getAdditionalProcedureCodesFor" should {
    "return list of all valid additional procedure codes for a given procedure code" when {
      "merchandiseInBag is turned on" in {

        when(mockMerchandiseInBagConfig.isMerchandiseInBagEnabled).thenReturn(true)

        service.getAdditionalProcedureCodesFor(sampleProcedureCode1.code, ENGLISH) must equal(
          Seq(sampleAdditionalProcedureCode1, sampleAdditionalProcedureCode2, sampleAdditionalProcedureCode1MB)
        )
      }
      "merchandiseInBag is turned off" in {

        when(mockMerchandiseInBagConfig.isMerchandiseInBagEnabled).thenReturn(false)

        service.getAdditionalProcedureCodesFor(sampleProcedureCode1.code, ENGLISH) must equal(
          Seq(sampleAdditionalProcedureCode1, sampleAdditionalProcedureCode2)
        )
      }
    }

    "return list of all valid C21 additional procedure codes for a given C21 procedure code" in {
      service.getAdditionalProcedureCodesFor(sampleC21ProcedureCode1.code, ENGLISH) must equal(
        Seq(sampleC21AdditionalProcedureCodes1, sampleC21AdditionalProcedureCodes2)
      )
    }

    "return nothing for an unknown procedure code" in {
      service.getAdditionalProcedureCodesFor("UNKNOWN", ENGLISH) must equal(Seq.empty[AdditionalProcedureCode])
    }

    s"return the same ProcedureCode entity irrespective of the Locale's country or variant" in {
      service.getAdditionalProcedureCodesFor("UNKNOWN", ENGLISH) must equal(Seq.empty[AdditionalProcedureCode])
      service.getAdditionalProcedureCodesFor("UNKNOWN", new Locale(ENGLISH.getCountry, "gb")) must equal(Seq.empty[AdditionalProcedureCode])
      service.getAdditionalProcedureCodesFor("UNKNOWN", new Locale(ENGLISH.getCountry, "gb", "scouse")) must equal(Seq.empty[AdditionalProcedureCode])
    }
  }
}

object ProcedureCodeServiceSpec {
  val sampleProcedureCode1 = ProcedureCode("0001", "First procedure code")
  val sampleProcedureCode2 = ProcedureCode("0002", "Second procedure code")

  val sampleProcedureCodes = ListMap(sampleProcedureCode1.code -> sampleProcedureCode1, sampleProcedureCode2.code -> sampleProcedureCode2)

  val sampleC21ProcedureCode1 = ProcedureCode("1001", "First C21 procedure code")
  val sampleC21ProcedureCode2 = ProcedureCode("1002", "Second C21 procedure code")

  val sampleC21ProcedureCodes =
    ListMap(sampleC21ProcedureCode1.code -> sampleC21ProcedureCode1, sampleC21ProcedureCode2.code -> sampleC21ProcedureCode2)

  val sampleAdditionalProcedureCode1 = AdditionalProcedureCode("001", "First additional procedure code")
  val sampleAdditionalProcedureCode2 = AdditionalProcedureCode("002", "Second additional procedure code")
  val sampleAdditionalProcedureCode1MB = AdditionalProcedureCode("1MB", "Merchandise In Baggage")

  val additionalProcedureCodesMap = ListMap(
    sampleAdditionalProcedureCode1.code -> sampleAdditionalProcedureCode1,
    sampleAdditionalProcedureCode2.code -> sampleAdditionalProcedureCode2,
    sampleAdditionalProcedureCode1MB.code -> sampleAdditionalProcedureCode1MB
  )

  val sampleC21AdditionalProcedureCodes1 = AdditionalProcedureCode("101", "First C21 additional procedure code")
  val sampleC21AdditionalProcedureCodes2 = AdditionalProcedureCode("102", "Second C21 additional procedure code")

  val c21AdditionalProcedureCodesMap = ListMap(
    sampleC21AdditionalProcedureCodes1.code -> sampleC21AdditionalProcedureCodes1,
    sampleC21AdditionalProcedureCodes2.code -> sampleC21AdditionalProcedureCodes2
  )

  val validAdditionalCodesForProcedureCode1 =
    Seq(sampleAdditionalProcedureCode1.code, sampleAdditionalProcedureCode2.code, sampleAdditionalProcedureCode1MB.code)
  val validAdditionalCodesForC21ProcedureCode1 = Seq(sampleC21AdditionalProcedureCodes1.code, sampleC21AdditionalProcedureCodes2.code)
}
