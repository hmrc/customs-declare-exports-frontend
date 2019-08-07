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

package unit.controllers.declaration

import controllers.declaration.PackageInformationController
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.PackageInformation
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.declaration.package_information

class PackageInformationControllerSpec extends ControllerSpec {

  trait SetUp {
    val packageInformationPage = new package_information(mainTemplate)

    val controller = new PackageInformationController(
      mockAuthAction,
      mockJourneyAction,
      mockErrorHandler,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      packageInformationPage
    )(ec)

    val itemId = "itemId"

    authorizedUser()
    withCaching(None)
    withNewCaching(aCacheModel(withChoice(SupplementaryDec)))
  }

  "Package Information Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in new SetUp {

        val result = controller.displayPage(itemId)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked and cache contain some data" in new SetUp {

        val itemWithPackageInformation = aCachedItem(withPackageInformation(Some("12"), Some(10), Some("123")))
        withNewCaching(aCacheModel(withItem(itemWithPackageInformation)))

        val result = controller.displayPage(itemId)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "action is incorrect" in new SetUp {

        val body =
          Seq(("typesOfPackages", "NT"), ("numberOfPackages", "123"), ("shippingMarks", "abc"), ("wrongAction", ""))

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user tried to continue without adding an item" in new SetUp {

        val body = (SaveAndContinue.toString, "")

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body))

        status(result) must be(BAD_REQUEST)
      }

      "user tried to add incorrect item" in new SetUp {

        val body =
          Seq(("typesOfPackages", "wrongType"), ("numberOfPackages", ""), ("shippingMarks", ""), (Add.toString, ""))

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reached limit of items" in new SetUp {

        val packageInformation = PackageInformation(None, None, None)
        val maxItems = aCachedItem(withPackageInformation(packageInformation, Seq.fill(98)(packageInformation): _*))
        withNewCaching(aCacheModel(withItem(maxItems)))

        val body =
          Seq(("typesOfPackages", "NT"), ("numberOfPackages", "123"), ("shippingMarks", "abc"), (Add.toString, ""))

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user tried to add duplicated value" in new SetUp {

        val item = aCachedItem(withPackageInformation(Some("NT"), Some(1), Some("value")))
        withNewCaching(aCacheModel(withItem(item)))

        val body =
          Seq(("typesOfPackages", "NT"), ("numberOfPackages", "1"), ("shippingMarks", "value"), (Add.toString, ""))

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user added correct item" in new SetUp {

        val body =
          Seq(("typesOfPackages", "NT"), ("numberOfPackages", "1"), ("shippingMarks", "value"), (Add.toString, ""))

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(SEE_OTHER)
      }

      "user clicked continue with item in a cache" in new SetUp {

        val item = aCachedItem(withPackageInformation(Some("NT"), Some(1), Some("value")))
        withNewCaching(aCacheModel(withItem(item)))

        val body = (SaveAndContinue.toString, "")

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body))

        status(result) must be(SEE_OTHER)
      }
    }
  }
}
