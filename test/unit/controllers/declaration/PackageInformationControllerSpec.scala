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
import controllers.util.{Add, SaveAndContinue}
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.PackageInformation
import play.api.test.Helpers._
import services.cache.ExportsItemBuilder
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.package_information

class PackageInformationControllerSpec extends ControllerSpec with ErrorHandlerMocks with ExportsItemBuilder {

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

    val exampleItem = anItem()

    val itemId = exampleItem.id

    authorizedUser()
    withNewCaching(aDeclaration(withChoice(SupplementaryDec)))
    setupErrorHandler()
  }

  "Package Information Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in new SetUp {

        withNewCaching(aDeclaration(withChoice(SupplementaryDec)))

        val result = controller.displayPage(itemId)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked and cache contain some data" in new SetUp {

        val itemWithPackageInformation = anItem(
          withPackageInformation(typesOfPackages = Some("12"), numberOfPackages = Some(10), shippingMarks = Some("123"))
        )
        withNewCaching(aDeclaration(withItem(itemWithPackageInformation)))

        val result = controller.displayPage(itemId)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "action is incorrect" in new SetUp {

        withNewCaching(aDeclaration(withChoice(SupplementaryDec)))

        val body =
          Seq(("typesOfPackages", "NT"), ("numberOfPackages", "123"), ("shippingMarks", "abc"), ("wrongAction", ""))

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user tried to continue without adding an item" in new SetUp {

        withNewCaching(aDeclaration(withChoice(SupplementaryDec)))

        val body = (SaveAndContinue.toString, "")

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body))

        status(result) must be(BAD_REQUEST)
      }

      "user tried to add incorrect item" in new SetUp {

        withNewCaching(aDeclaration(withChoice(SupplementaryDec)))

        val body =
          Seq(("typesOfPackages", "wrongType"), ("numberOfPackages", ""), ("shippingMarks", ""), (Add.toString, ""))

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reached limit of items" in new SetUp {

        val packageInformation = PackageInformation("", 0, "")
        val maxItems = anItem(withPackageInformation(List.fill(99)(packageInformation)))
        withNewCaching(aDeclaration(withItem(maxItems)))

        val body =
          Seq(("typesOfPackages", "NT"), ("numberOfPackages", "123"), ("shippingMarks", "abc"), (Add.toString, ""))

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user tried to add duplicated value" in new SetUp {

        val item = anItem(
          withPackageInformation(
            typesOfPackages = Some("NT"),
            numberOfPackages = Some(1),
            shippingMarks = Some("value")
          )
        )
        withNewCaching(aDeclaration(withItem(item)))

        val body =
          Seq(("typesOfPackages", "NT"), ("numberOfPackages", "1"), ("shippingMarks", "value"), (Add.toString, ""))

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user added correct item" in new SetUp {

        withNewCaching(aDeclaration(withChoice(SupplementaryDec)))

        val body =
          Seq(("typesOfPackages", "NT"), ("numberOfPackages", "1"), ("shippingMarks", "value"), (Add.toString, ""))

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body: _*))

        status(result) must be(SEE_OTHER)
      }

      "user clicked continue with item in a cache" in new SetUp {

        val item = anItem(
          withPackageInformation(
            typesOfPackages = Some("NT"),
            numberOfPackages = Some(1),
            shippingMarks = Some("value")
          )
        )
        withNewCaching(aDeclaration(withItem(item)))

        val body = (SaveAndContinue.toString, "")

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(body))

        status(result) must be(SEE_OTHER)
      }
    }
  }
}
