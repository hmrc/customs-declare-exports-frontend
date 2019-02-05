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
import play.api.libs.json.{JsObject, JsString, JsValue}

class ConsignmentReferencesSpec extends WordSpec with MustMatchers {
  import ConsignmentReferencesSpec._

  "ConsignmentReferences" should {
    "convert itself to consignment references properties" in {
      val consignmentReferences = correctConsignmentReferences
      val expectedConsignmentReferencesProperties: Map[String, String] =
        Map(
          "declaration.goodsShipment.ucr.traderAssignedReferenceId" -> consignmentReferences.ducr.get.ducr,
          "declaration.functionalReferenceId" -> consignmentReferences.lrn
        )

      consignmentReferences.toMetadataProperties() must equal(expectedConsignmentReferencesProperties)
    }
  }

}

object ConsignmentReferencesSpec {
  val exemplaryDucr = "8GB123456789012-1234567890QWERTYUIO"

  val correctConsignmentReferences = ConsignmentReferences(ducr = Some(Ducr(ducr = exemplaryDucr)), lrn = "123ABC")
  val correctConsignmentReferencesNoDucr = ConsignmentReferences(ducr = None, lrn = "123ABC")
  val emptyConsignmentReferences = ConsignmentReferences(ducr = None, lrn = "")

  val correctConsignmentReferencesJSON: JsValue = JsObject(
    Map("ducr.ducr" -> JsString(exemplaryDucr), "lrn" -> JsString("123ABC"))
  )
  val correctConsignmentReferencesNoDucrJSON: JsValue = JsObject(
    Map("ducr.ducr" -> JsString(""), "lrn" -> JsString("123ABC"))
  )
  val emptyConsignmentReferencesJSON: JsValue = JsObject(Map("ducr.ducr" -> JsString(""), "lrn" -> JsString("")))

}
