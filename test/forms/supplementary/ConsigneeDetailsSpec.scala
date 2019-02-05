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

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsValue}

class ConsigneeDetailsSpec extends WordSpec with MustMatchers {
  import ConsigneeDetailsSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      pending
      val consigneeDetails = correctConsigneeDetails
      val expectedMetadataProperties: Map[String, String] = Map.empty

      consigneeDetails.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }

}

object ConsigneeDetailsSpec {
  import forms.supplementary.EntityDetailsSpec._

  val correctConsigneeDetails = ConsigneeDetails(details = EntityDetailsSpec.correctEntityDetails)
  val correctConsigneeDetailsEORIOnly = ConsigneeDetails(details = EntityDetailsSpec.correctEntityDetailsEORIOnly)
  val correctConsigneeDetailsAddressOnly = ConsigneeDetails(details = EntityDetailsSpec.correctEntityDetailsAddressOnly)
  val emptyConsigneeDetails = ConsigneeDetails(details = EntityDetailsSpec.emptyEntityDetails)

  val correctConsigneeDetailsJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsJSON))
  val correctConsigneeDetailsEORIOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsEORIOnlyJSON))
  val correctConsigneeDetailsAddressOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsAddressOnlyJSON))
  val emptyConsigneeDetailsJSON: JsValue = JsObject(Map("details" -> emptyEntityDetailsJSON))
}
