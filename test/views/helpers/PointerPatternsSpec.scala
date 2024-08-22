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

package views.helpers

import models.Pointer
import services.cache.ExportsTestHelper
import views.common.UnitViewSpec

class PointerPatternsSpec extends UnitViewSpec with ExportsTestHelper {

  val additionalActorsPointer = "declaration.parties.additionalActors.actors.$"
  val consignorDetailsPointer = "declaration.parties.consignorDetails"

  val sampleDec = aDeclaration()

  "PointerPatterns" can {
    "expand single parent pointers into a sequence of their child pointers, so" should {
      s"expand '$additionalActorsPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(Pointer(additionalActorsPointer), sampleDec, sampleDec)

        expandedPointers.map(_.pattern) must equal(Seq(
          s"$additionalActorsPointer.eori",
          s"$additionalActorsPointer.type"
        ))
      }

      s"expand '$consignorDetailsPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(Pointer(consignorDetailsPointer), sampleDec, sampleDec)

        expandedPointers.map(_.pattern) must equal(Seq(
          s"$consignorDetailsPointer.details.address.fullName",
          s"$consignorDetailsPointer.details.address.addressLine",
          s"$consignorDetailsPointer.details.address.townOrCity",
          s"$consignorDetailsPointer.details.address.postCode",
          s"$consignorDetailsPointer.details.address.country",
        ))
      }
    }
  }

}
