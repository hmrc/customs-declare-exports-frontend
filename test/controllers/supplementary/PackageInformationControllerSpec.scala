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

package controllers.supplementary

import base.{CustomExportsBaseSpec, TestHelper}
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.supplementary.PackageInformation
import forms.supplementary.PackageInformationSpec._
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class PackageInformationControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/package-information")

  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")
  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  "PackageInformationController" should {
    "display total number of items form" in {
      authorizedUser()
      withCaching[PackageInformation](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.packageInformation.title"))
      stringResult must include(messages("supplementary.packageInformation.typesOfPackages"))
      stringResult must include(messages("supplementary.packageInformation.typesOfPackages.hint"))
      stringResult must include(messages("supplementary.packageInformation.numberOfPackages"))
    }
  }

  "validate form - empty form" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val result = route(app, postRequest(uri, emptyPackageInformationJSON)).get

    status(result) must be(BAD_REQUEST)

    contentAsString(result) must include(messages("supplementary.packageInformation.typesOfPackages.empty"))
    contentAsString(result) must include(messages("supplementary.packageInformation.numberOfPackages.empty"))

/*
    contentAsString(result) must not include messages("supplementary.packageInformation.typesOfPackages.error")
    contentAsString(result) must not include messages("supplementary.packageInformation.numberOfPackages.error")
*/

  }

  "validate form - too short type of packages" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectTypeOfPackages: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(1)),
          "numberOfPackages" -> JsString("12345")
        )
      )

    val result = route(app, postRequest(uri, incorrectTypeOfPackages)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.typesOfPackages.error"))
  }

  "validate form - too long type of packages" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectTypeOfPackages: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(3)),
          "numberOfPackages" -> JsString("12345")
        )
      )

    val result = route(app, postRequest(uri, incorrectTypeOfPackages)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.typesOfPackages.error"))
  }

  "validate form - too long number of packages" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectNumberOfPackages: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(3)),
          "numberOfPackages" -> JsString("123456")
        )
      )

    val result = route(app, postRequest(uri, incorrectNumberOfPackages)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.numberOfPackages.error"))
  }

  "validate form - alpha numerical number of packages" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectNumberOfPackages: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(3)),
          "numberOfPackages" -> JsString(TestHelper.createRandomString(5))
        )
      )

    val result = route(app, postRequest(uri, incorrectNumberOfPackages)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.numberOfPackages.error"))
  }

  "validate form - too many digits on supplementary units" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectSupplementaryUnits: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(3)),
          "numberOfPackages" -> JsString("12334")
      ))

    val result = route(app, postRequest(uri, incorrectSupplementaryUnits)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.supplementaryUnits.error"))
  }

  "validate form - too many digits after comma on supplementary units" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectSupplementaryUnits: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(2)),
          "numberOfPackages" -> JsString("1234")
        )
      )

    val result = route(app, postRequest(uri, incorrectSupplementaryUnits)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.supplementaryUnits.error"))
  }

  "validate form - too large numeric value on supplementary units" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectSupplementaryUnits: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(3)),
          "numberOfPackages" -> JsString("1234")
        )
      )

    val result = route(app, postRequest(uri, incorrectSupplementaryUnits)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.supplementaryUnits.error"))
  }

  "validate form - too long shipping marks" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectShippingMarks: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(3)),
          "numberOfPackages" -> JsString(TestHelper.createRandomString(5))
        )
      )

    val result = route(app, postRequest(uri, incorrectShippingMarks)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.shippingMarks.error"))
  }

  "validate form - too many digits on net mass" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectNetMass: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(3)),
          "numberOfPackages" -> JsString(TestHelper.createRandomString(5))
        )
      )

    val result = route(app, postRequest(uri, incorrectNetMass)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.netMass.error"))
  }

  "validate form - too many digits after comma on net mass" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectNetMass: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(3)),
          "numberOfPackages" -> JsString(TestHelper.createRandomString(5))
        )
      )

    val result = route(app, postRequest(uri, incorrectNetMass)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.netMass.error"))
  }

  "validate form - too large numeric value on net mass" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectNetMass: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(3)),
          "numberOfPackages" -> JsString(TestHelper.createRandomString(5))
        )
      )

    val result = route(app, postRequest(uri, incorrectNetMass)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.netMass.error"))
  }

  "validate form - too many digits on gross mass" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectGrossMass: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(3)),
          "numberOfPackages" -> JsString(TestHelper.createRandomString(5))
        )
      )

    val result = route(app, postRequest(uri, incorrectGrossMass)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.grossMass.error"))
  }

  "validate form - too many digits after comma on gross mass" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectGrossMass: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(3)),
          "numberOfPackages" -> JsString(TestHelper.createRandomString(5))
        )
      )

    val result = route(app, postRequest(uri, incorrectGrossMass)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.grossMass.error"))
  }

  "validate form - too large numeric value on gross mass" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val incorrectGrossMass: JsValue =
      JsObject(
        Map(
          "typesOfPackages" -> JsString(TestHelper.createRandomString(2)),
          "numberOfPackages" -> JsString("12345")
        )
      )

    val result = route(app, postRequest(uri, incorrectGrossMass)).get

    status(result) must be(BAD_REQUEST)
    contentAsString(result) must include(messages("supplementary.packageInformation.grossMass.error"))
  }

  "validate form - correct values" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val result = route(app, postRequest(uri, correctPackageInformationDecimalValuesJSON)).get
    val header = result.futureValue.header

    status(result) must be(SEE_OTHER)
    header.headers.get("Location") must be(
      Some("/customs-declare-exports/declaration/supplementary/additional-information")
    )

  }

  "validate form - correct values using only integers" in {
    authorizedUser()
    withCaching[PackageInformation](None)

    val result = route(app, postRequest(uri, correctPackageInformationIntegerValuesJSON)).get
    val header = result.futureValue.header

    status(result) must be(SEE_OTHER)
    header.headers.get("Location") must be(
      Some("/customs-declare-exports/declaration/supplementary/additional-information")
    )
  }

}
