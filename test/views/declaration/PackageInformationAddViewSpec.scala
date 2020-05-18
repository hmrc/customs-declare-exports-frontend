/*
 * Copyright 2020 HM Revenue & Customs
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

package views.declaration

import base.Injector
import forms.declaration.{PackageInformation, TaricCode}
import helpers.views.declaration.CommonMessages
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import org.scalatest.MustMatchers
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.package_information_add
import views.tags.ViewTest

@ViewTest
class PackageInformationAddViewSpec extends UnitViewSpec with ExportsTestData with Stubs with MustMatchers with CommonMessages with Injector {

  import PackageInformationViewSpec._

  private val itemId = "item1"
  private val form: Form[PackageInformation] = PackageInformation.form(DeclarationType.STANDARD)
  private val page = instanceOf[package_information_add]
  private val realMessages = validatedMessages

  private def createView(form: Form[PackageInformation] = form, packages: Seq[PackageInformation] = Seq.empty): Document =
    page(Mode.Normal, itemId, form, packages)(journeyRequest(), realMessages)

  "PackageInformation Add View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1").text() must be(realMessages("declaration.packageInformation.title"))
    }

    "display 'Back' button that links to 'statistical value' page" in {
      val backLinkContainer = createView(packages = Seq.empty).getElementById("back-link")

      backLinkContainer.getElementById("back-link") must haveHref(
        controllers.declaration.routes.StatisticalValueController.displayPage(Mode.Normal, itemId)
      )
    }

    "display 'Back' button that links to 'PackageInformation summary' page" in {
      val backLinkContainer = createView(packages = Seq(packageInformation)).getElementById("back-link")

      backLinkContainer.getElementById("back-link") must haveHref(
        controllers.declaration.routes.PackageInformationSummaryController.displayPage(Mode.Normal, itemId)
      )
    }

    "display 'Save and continue' button on page" in {
      val saveButton = view.getElementById("submit")
      saveButton.text() must be(realMessages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton.text() must be(realMessages(saveAndReturnCaption))
    }
  }

  "PackageInformation Add View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(form.fillAndValidate(PackageInformation(None, None, None)))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#typesOfPackages")
      view must containErrorElementWithTagAndHref("a", "#numberOfPackages")
      view must containErrorElementWithTagAndHref("a", "#shippingMarks")

      view must containErrorElementWithMessage(realMessages("declaration.packageInformation.typesOfPackages.empty"))
      view must containErrorElementWithMessage(realMessages("error.number"))
      view must containErrorElementWithMessage(realMessages("declaration.packageInformation.shippingMarks.empty"))
    }

    "display error if incorrect PackageInformation is entered" in {
      val view = createView(form.fillAndValidate(PackageInformation(Some("invalid"), Some(1), Some("wrong!"))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#typesOfPackages")
      view must containErrorElementWithTagAndHref("a", "#shippingMarks")

      view must containErrorElementWithMessage(realMessages("declaration.packageInformation.typesOfPackages.error"))
      view must containErrorElementWithMessage(realMessages("declaration.packageInformation.shippingMarks.characterError"))
    }

  }

  "PackageInformation Add View when filled" should {

    "display data in PackageInformation code input" in {

      val view = createView(form.fill(packageInformation))

      view.getElementById("typesOfPackages").attr("value") must be("4321")
      view.getElementById("numberOfPackages").attr("value") must be(packageInformation.numberOfPackages.get.toString)
      view.getElementById("shippingMarks").attr("value") must be(packageInformation.shippingMarks.get)
    }
  }
}
