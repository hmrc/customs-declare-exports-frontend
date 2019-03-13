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
import models.declaration.supplementary.AdditionalInformationData
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}

class AdditionalInformationSpec extends WordSpec with MustMatchers {
  import AdditionalInformationSpec._

  "Method toMetadataProperties" should {

    "return proper Metadata Properties" in {

      val additionalInformation = correctAdditionalInformation
      val expectedProperties: Map[String, String] = Map(
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalInformations[0].statementCode" -> additionalInformation.items.head.code,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalInformations[0].statementDescription" -> additionalInformation.items.head.description
      )

      additionalInformation.toMetadataProperties() must equal(expectedProperties)
    }
  }

  "Additional Information object" should {
    "contains correct limit value" in {
      AdditionalInformationData.maxNumberOfItems must be(99)
    }
  }

}

object AdditionalInformationSpec {

  val correctAdditionalInformation =
    AdditionalInformationData(
      Seq(AdditionalInformation(code = "M1l3s", description = "Description for Additional Information: Davis")))

  val emptyAdditionalInformation = AdditionalInformation("", "")

  val correctAdditionalInformationJSON: JsValue = JsObject(
    Map("code" -> JsString("M1l3s"), "description" -> JsString("Description for Additional Information: Davis"))
  )

  val incorrectAdditionalInformationJSON: JsValue = JsObject(
    Map("code" -> JsString("Miles"), "description" -> JsString("Description for Additional Information: Davis"))
  )

  val emptyAdditionalInformationJSON: JsValue = JsObject(Map("code" -> JsString(""), "description" -> JsString("")))

  val correctAdditionalInformationDataJSON: JsValue = JsObject(
    Map("items" -> JsArray(Seq(correctAdditionalInformationJSON)))
  )
}
