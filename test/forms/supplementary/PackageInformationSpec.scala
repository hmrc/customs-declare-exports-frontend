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

import controllers.util.Add
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class PackageInformationSpec extends WordSpec with MustMatchers {


}

object PackageInformationSpec {

  val correctPackageInformationDecimalValuesJSON: JsValue =
    JsObject(
      Map(
        "typesOfPackages" -> JsString("AB"),
        "numberOfPackages" -> JsString("12345"),
        "supplementaryUnits" -> JsString("1234567890.123456"),
        "shippingMarks" -> JsString("ShippingMarks"),
        "netMass" -> JsString("12345678.123"),
        "grossMass" -> JsString("1234567890.123456")
      )
    )
  val correctPackageInformationIntegerValuesJSON: JsValue =
    JsObject(
      Map(
        "typesOfPackages" -> JsString("AB"),
        "numberOfPackages" -> JsString("12345"),
        "supplementaryUnits" -> JsString("1234567890"),
        "shippingMarks" -> JsString("ShippingMarks"),
        "netMass" -> JsString("12345678"),
        "grossMass" -> JsString("1234567890")
      )
    )
  val emptyPackageInformationJSON: JsValue =
    JsObject(
      Map(
        "typesOfPackages" -> JsString(""),
        "numberOfPackages" -> JsString(""),
        "supplementaryUnits" -> JsString(""),
        "shippingMarks" -> JsString(""),
        "netMass" -> JsString(""),
        "grossMass" -> JsString(""),
        Add.toString-> JsString("")
      )
    )
}
