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

class ConsignmentDataValidationHelperSpec extends WordSpec with Matchers {
	val correctDucr = "5GB123456789000-123ABC456DEFIIIIIII"
	val correctMucr = "A:GBP23"

	"Consignment data validation helper" should {
		"return correct information about empty DUCR number for consolidation" in {
			val validData = ConsignmentData(Consolidation, None, Some("ducrConsolidation"), None)
			val invalidData = ConsignmentData(Consolidation, None, None, Some("ducrSingleShipment"))

			ConsignmentDataValidationHelper.emptyDucr(validData) shouldBe true
			ConsignmentDataValidationHelper.emptyDucr(invalidData) shouldBe false
		}

		"return correct information about empty DUCR number for single shipment" in {
			val validData = ConsignmentData(SingleShipment, None, None, Some("ducrSingleShipment"))
			val invalidData = ConsignmentData(SingleShipment, None, Some("ducrConsolidation"), None)

			ConsignmentDataValidationHelper.emptyDucr(validData) shouldBe true
			ConsignmentDataValidationHelper.emptyDucr(invalidData) shouldBe false
		}

		"return correct information about DUCR format for consolidation" in {
			val validData = ConsignmentData(Consolidation, None, Some(correctDucr), None)
			val invalidData = ConsignmentData(Consolidation, None, Some("incorrectDucr"), None)

			ConsignmentDataValidationHelper.ducrFormat(validData) shouldBe true
			ConsignmentDataValidationHelper.ducrFormat(invalidData) shouldBe false
		}

		"return correct information about DUCR format for single shipment" in {
			val validData = ConsignmentData(SingleShipment, None, None, Some(correctDucr))
			val invalidData = ConsignmentData(SingleShipment, None, None, Some("incorrectDucr"))

			ConsignmentDataValidationHelper.ducrFormat(validData) shouldBe true
			ConsignmentDataValidationHelper.ducrFormat(invalidData) shouldBe false
		}

		"return correct information about MUCR format if MUCR is empty" in {
			val consolidationData = ConsignmentData(Consolidation, None, None, None)
			val singleShipmentData = ConsignmentData(SingleShipment, None, None, None)

			ConsignmentDataValidationHelper.mucrFormat(consolidationData) shouldBe true
			ConsignmentDataValidationHelper.mucrFormat(singleShipmentData) shouldBe true
		}

		"return correct information about MUCR format" in {
			val validData = ConsignmentData(Consolidation, Some(correctMucr), None, None)
			val invalidData = ConsignmentData(Consolidation, Some("incorrectMucr"), None, None)

			ConsignmentDataValidationHelper.mucrFormat(validData) shouldBe true
			ConsignmentDataValidationHelper.mucrFormat(invalidData) shouldBe false
		}
	}
}
