/*
 * Copyright 2023 HM Revenue & Customs
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

package services.ead

import base.{Injector, MockConnectors, MockExportCacheService, UnitWithMocksSpec}
import connectors.CustomsDeclareExportsConnector
import models.dis.MrnStatusSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import services.cache.SubmissionBuilder
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class EADServiceSpec
    extends UnitWithMocksSpec with MockExportCacheService with MockConnectors with ScalaFutures with OptionValues with Injector
    with SubmissionBuilder {

  private val connector = mock[CustomsDeclareExportsConnector]

  private implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  private implicit val ec: ExecutionContext = ExecutionContext.global

  private val service = new EADService(connector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(connector)
  }

  "EADService" should {

    "should return a status" when {
      val mrn = "18GBAKZ81EQJ2FGVR"

      "mrn is valid" in {
        // Given
        when(connector.fetchMrnStatus(any[String])(any(), any())).thenReturn(Future.successful(Some(MrnStatusSpec.completeMrnStatus)))

        // When
        val status = await(service.generateStatus(mrn))

        status.mrn must be(MrnStatusSpec.completeMrnStatus.mrn)
        status.eori must be(MrnStatusSpec.completeMrnStatus.eori)
        status.declarationType must be(MrnStatusSpec.completeMrnStatus.declarationType)
        status.ucr.value must be(MrnStatusSpec.completeMrnStatus.ucr.get)
        status.totalPackageQuantity must be(MrnStatusSpec.completeMrnStatus.totalPackageQuantity)
        status.goodsItemQuantity must be(MrnStatusSpec.completeMrnStatus.goodsItemQuantity)
        status.releasedDateTime.value must be(MrnStatusSpec.completeMrnStatus.releasedDateTime.get)
        status.acceptanceDateTime.value must be(MrnStatusSpec.completeMrnStatus.acceptanceDateTime.get)
        status.receivedDateTime must be(MrnStatusSpec.completeMrnStatus.receivedDateTime)
        status.versionId must be(MrnStatusSpec.completeMrnStatus.versionId)
        status.previousDocuments.head.typeCode must be(MrnStatusSpec.completeMrnStatus.previousDocuments.head.typeCode)
        status.previousDocuments.head.id must be(MrnStatusSpec.completeMrnStatus.previousDocuments.head.id)
      }

      "should throw an exception" when {
        val mrn = "18GBAKZ81EQJ2FGVR"

        "mrn information is not available" in {
          when(connector.fetchMrnStatus(any[String])(any(), any())).thenReturn(Future.successful(None))

          intercept[IllegalArgumentException](await(service.generateStatus(mrn))).getMessage must be("No declaration information was found")
        }

        "a Runtime exception is thrown" in {
          when(connector.fetchMrnStatus(any[String])(any(), any())).thenReturn(Future.failed(new RuntimeException("some issue")))

          intercept[IllegalArgumentException](await(service.generateStatus(mrn))).getMessage must be("No declaration information was found")
        }
      }
    }
  }
}
