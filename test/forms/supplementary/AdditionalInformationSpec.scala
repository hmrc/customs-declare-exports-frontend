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
import play.api.libs.json.{JsObject, JsString, JsValue}

class AdditionalInformationSpec extends WordSpec with MustMatchers {
  import AdditionalInformationSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val additionalInformation = correctAdditionalInformation
      val expectedProperties: Map[String, String] = Map(
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalInformations[0].statementCode" -> additionalInformation.code.get,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalInformations[0].statementDescription" -> additionalInformation.description.get
      )

      additionalInformation.toMetadataProperties() must equal(expectedProperties)
    }
  }

}

object AdditionalInformationSpec {
  val correctAdditionalInformation =
    AdditionalInformation(code = Some("12345"), description = Some("Description for Additional Information"))
  val emptyAdditionalInformation = AdditionalInformation(None, None)

  val correctAdditionalInformationJSON: JsValue = JsObject(
    Map("code" -> JsString("12345"), "description" -> JsString("Description for Additional Information"))
  )
  val emptyAdditionalInformationJSON: JsValue = JsObject(Map("code" -> JsString(""), "description" -> JsString("")))

}
