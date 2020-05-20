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
import forms.common.YesNoAnswer
import forms.declaration.PackageInformation
import helpers.views.declaration.CommonMessages
import models.Mode
import org.jsoup.nodes.Document
import org.scalatest.MustMatchers
import play.api.data.Form
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.package_information_remove
import views.tags.ViewTest

@ViewTest
class PackageInformationRemoveViewSpec extends UnitViewSpec with Stubs with MustMatchers with CommonMessages with Injector {

  import PackageInformationViewSpec._

  private val itemId = "item1"
  private val realMessages = validatedMessages
  private val form: Form[YesNoAnswer] = YesNoAnswer.form()
  private val page = instanceOf[package_information_remove]

  private def createView(form: Form[YesNoAnswer] = form, packageInfo: PackageInformation = packageInformation): Document =
    page(Mode.Normal, itemId, packageInfo, form)(request, realMessages)

  "PackageInformation Remove View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1").text() must be(realMessages("declaration.packageInformation.remove.title"))
    }

    "display PackageInformation to remove" in {
      view.getElementsByClass("govuk-summary-list__value").get(0).text() mustBe packageInformation.typesOfPackagesText.get
      view.getElementsByClass("govuk-summary-list__value").get(1).text() mustBe packageInformation.numberOfPackages.get.toString
      view.getElementsByClass("govuk-summary-list__value").get(2).text() mustBe packageInformation.shippingMarks.get
    }

    "display 'Back' button that links to 'PackageInformation summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer must containText(realMessages(backCaption))
      backLinkContainer.getElementById("back-link") must haveHref(
        controllers.declaration.routes.PackageInformationSummaryController.displayPage(Mode.Normal, itemId)
      )
    }

    "display 'Save and continue' button on page" in {
      val saveButton = view.getElementById("submit")
      saveButton must containText(realMessages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton must containText(realMessages(saveAndReturnCaption))
    }
  }

  "PackageInformation Remove View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(YesNoAnswer.form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#yesNo")

      view must containErrorElementWithMessage(realMessages("error.yesNo.required"))
    }

  }
}
