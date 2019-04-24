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

package forms.declaration
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsBoolean, JsObject, JsString, JsValue}

class TransportInformationSpec extends WordSpec with MustMatchers {}

object TransportInformationSpec {
  val correctTransportInformation =
    TransportInformation(Some("1"), "3", "10", Some("123112yu78"), "40", Some("1234567878ui"), Some("GB"), false)
  val correctTransportInformationJSON: JsValue =
    JsObject(
      Map(
        "inlandModeOfTransportCode" -> JsString("1"),
        "borderModeOfTransportCode" -> JsString("3"),
        "meansOfTransportOnDepartureType" -> JsString("10"),
        "meansOfTransportOnDepartureIDNumber" -> JsString("123112yu78"),
        "meansOfTransportCrossingTheBorderType" -> JsString("40"),
        "meansOfTransportCrossingTheBorderIDNumber" -> JsString("1234567878ui"),
        "meansOfTransportCrossingTheBorderNationality" -> JsString("Portugal"),
        "container" -> JsBoolean(true)
      )
    )
}
