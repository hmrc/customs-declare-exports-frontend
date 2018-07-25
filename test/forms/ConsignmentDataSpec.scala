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

import forms.ConsignmentChoice.{Consolidation, SingleShipment}
import org.scalatest.{Matchers, WordSpec}

class ConsignmentDataSpec extends WordSpec with Matchers {

  val ducrNumber = Some("ducrNumber")
  val mucrNumber = Some("mucrNumber")

  val ducrConsolidation = Some("ducrConsolidation")
  val ducrSingleShipment = Some("ducrSingleShipment")

  "ConsignmentData" should {
    "change choice to single shipment if mucr is missing" in {
      val preparedData = ConsignmentData(Consolidation, None, ducrNumber, None)
      val expectedResult = ConsignmentData(SingleShipment, None, None, ducrNumber)

      ConsignmentData.cleanConsignmentData(preparedData) shouldBe expectedResult
    }

    "clean consignment data for single shipment if contain something from consolidation" in {
      val firstPreparedData = ConsignmentData(SingleShipment, mucrNumber, ducrNumber, ducrNumber)
      val secondPreparedData = ConsignmentData(SingleShipment, None, ducrNumber, ducrNumber)
      val thirdPreparedData = ConsignmentData(SingleShipment, mucrNumber, None, ducrNumber)
      val expectedResult = ConsignmentData(SingleShipment, None, None, ducrNumber)

      ConsignmentData.cleanConsignmentData(firstPreparedData) shouldBe expectedResult
      ConsignmentData.cleanConsignmentData(secondPreparedData) shouldBe expectedResult
      ConsignmentData.cleanConsignmentData(thirdPreparedData) shouldBe expectedResult
    }

    "clean consignment data for consolidation if contain ducr from single shipment" in {
      val preparedData = ConsignmentData(Consolidation, mucrNumber, ducrNumber, ducrNumber)
      val expectedResult = ConsignmentData(Consolidation, mucrNumber, ducrNumber, None)

      ConsignmentData.cleanConsignmentData(preparedData) shouldBe expectedResult
    }

    "return correct ducr number for consolidation" in {
      val consolidationData = ConsignmentData(Consolidation, None, ducrConsolidation, ducrSingleShipment)

      ConsignmentData.ducr(consolidationData) shouldBe ducrConsolidation
    }

    "return correct ducr number for single shipment" in {
      val singleShipmentData = ConsignmentData(SingleShipment, None, ducrConsolidation, ducrSingleShipment)

      ConsignmentData.ducr(singleShipmentData) shouldBe ducrSingleShipment
    }
  }
}
