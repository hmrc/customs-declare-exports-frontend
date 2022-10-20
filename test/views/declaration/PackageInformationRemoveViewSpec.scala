/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.common.YesNoAnswer.form
import forms.declaration.PackageInformation
import models.DeclarationType.STANDARD
import org.jsoup.nodes.Document
import play.api.data.Form
import services.PackageTypesService
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.packageInformation.package_information_remove
import views.tags.ViewTest

@ViewTest
class PackageInformationRemoveViewSpec extends PageWithButtonsSpec with Injector {

  import PackageInformationViewSpec._

  private val page = instanceOf[package_information_remove]

  override val typeAndViewInstance = (STANDARD, page(itemId, packageInformation, form())(_, _))

  def createView(frm: Form[YesNoAnswer] = form(), packageInfo: PackageInformation = packageInformation, mode: Mode = Normal): Document =
    page(itemId, packageInfo, frm)(request, messages)

  "PackageInformation Remove View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.packageInformation.remove.title")
    }

    "display PackageInformation to remove" in {
      val packageTypesService = instanceOf[PackageTypesService]
      val expectedText = packageTypesService.typesOfPackagesText(packageInformation.typesOfPackages).get
      view.getElementsByClass("govuk-summary-list__value").get(0).text() mustBe expectedText
      view.getElementsByClass("govuk-summary-list__value").get(1).text() mustBe packageInformation.numberOfPackages.get.toString
      view.getElementsByClass("govuk-summary-list__value").get(2).text() mustBe packageInformation.shippingMarks.get
    }

    "display 'Back' button that links to 'PackageInformation summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer must containMessage(backToPreviousQuestionCaption)
      backLinkContainer.getElementById("back-link") must haveHref(
        controllers.declaration.routes.PackageInformationSummaryController.displayPage(itemId)
      )
    }

    val createViewWithMode: Mode => Document = mode => createView(mode = mode)
    checkAllSaveButtonsAreDisplayed(createViewWithMode)
  }

  "PackageInformation Remove View for invalid input" should {
    "display error if nothing is entered" in {
      val view = createView(form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code_yes")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }
  }
}
