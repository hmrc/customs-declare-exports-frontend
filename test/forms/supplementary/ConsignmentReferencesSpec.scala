/*
 * Copyright 2019 HM Revenue & Customs
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

package forms.supplementary

import forms.Ducr
import org.scalatest.{MustMatchers, WordSpec}

class ConsignmentReferencesSpec extends WordSpec with MustMatchers {

  private val ducr = "8GB123456123456-1234567890QWERTYUIO"
  private val lrn = "1234567890123456789012"

  private val consignmentReferences =
    ConsignmentReferences(ducr = Some(Ducr(ducr)), lrn = lrn)

  private val expectedConsignmentReferencesProperties: Map[String, String] = Map(
    "declaration.goodsShipment.ucr.traderAssignedReferenceId" -> ducr,
    "declaration.functionalReferenceId" -> lrn
  )

  "ConsignmentReferences" should {
    "convert itself to consignment references properties" in {
      consignmentReferences.toMetadataProperties() must equal(expectedConsignmentReferencesProperties)
    }
  }

}
