/*
 * Copyright 2018 HM Revenue & Customs
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

package forms

import org.scalatest.{Matchers, WordSpec}

class ConsignmentDataSpec extends WordSpec with Matchers {

  val ducrNumber = Some("ducrNumber")
  val mucrNumber = Some("mucrNumber")

  "ConsignmentData" should {
    "change choice to single shipment if mucr is missing" in {
      val preparedData = ConsignmentData("consolidation", None, ducrNumber, None)
      val expectedResult = ConsignmentData("singleShipment", None, None, ducrNumber)

      ConsignmentData.cleanConsignmentData(preparedData) shouldBe expectedResult
    }

    "clean consignment data for single shipment if contain something from consolidation" in {
      val firstPreparedData = ConsignmentData("singleShipment", mucrNumber, ducrNumber, ducrNumber)
      val secondPreparedData = ConsignmentData("singleShipment", None, ducrNumber, ducrNumber)
      val thirdPreparedData = ConsignmentData("singleShipment", mucrNumber, None, ducrNumber)
      val expectedResult = ConsignmentData("singleShipment", None, None, ducrNumber)

      ConsignmentData.cleanConsignmentData(firstPreparedData) shouldBe expectedResult
      ConsignmentData.cleanConsignmentData(secondPreparedData) shouldBe expectedResult
      ConsignmentData.cleanConsignmentData(thirdPreparedData) shouldBe expectedResult
    }

    "clean consignment data for consolidation if contain ducr from single shipment" in {
      val preparedData = ConsignmentData("consolidation", mucrNumber, ducrNumber, ducrNumber)
      val expectedResult = ConsignmentData("consolidation", mucrNumber, ducrNumber, None)

      ConsignmentData.cleanConsignmentData(preparedData) shouldBe expectedResult
    }

    "return correct ducr number for consolidation" in {
      val ducrConsolidation = Some("ducrConsolidation")
      val ducrSingleShipment = Some("ducrSingleShipment")

      val consolidationData = ConsignmentData("consolidation", None, ducrConsolidation, ducrSingleShipment)
      val singleShipmentData = ConsignmentData("singleShipment", None, ducrConsolidation, ducrSingleShipment)

      ConsignmentData.ducr(consolidationData) shouldBe ducrConsolidation
      ConsignmentData.ducr(singleShipmentData) shouldBe ducrSingleShipment
    }
  }
}
