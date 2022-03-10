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
import forms.declaration.PackageInformation
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.Inspectors.forAll
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.package_information_add
import views.tags.ViewTest

@ViewTest
class PackageInformationAddViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages with Injector {

  import PackageInformationViewSpec._

  private val itemId = "item1"

  private def form(): Form[PackageInformation] = PackageInformation.form()
  private val page = instanceOf[package_information_add]

  private def createView(withForm: Option[Form[PackageInformation]] = None, packages: Seq[PackageInformation] = Seq.empty)(
    implicit request: JourneyRequest[_]
  ): Document =
    page(Mode.Normal, itemId, withForm.getOrElse(form), packages)(request, messages)

  "PackageInformation Add View" should {

    "have necessary message keys" in {
      messages must haveTranslationFor("declaration.packageInformation.paragraph")
      messages must haveTranslationFor("declaration.packageInformation.typesOfPackages.paragraph")
      messages must haveTranslationFor("declaration.packageInformation.numberOfPackages.details.paragraph.1")
      messages must haveTranslationFor("declaration.packageInformation.numberOfPackages.details.paragraph.2")
      messages must haveTranslationFor("declaration.packageInformation.numberOfPackages.details.paragraph.3")
      messages must haveTranslationFor("declaration.packageInformation.numberOfPackages.details.paragraph.3.link")
      messages must haveTranslationFor("declaration.packageInformation.shippingMark.paragraph")
    }

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.packageInformation.title")
      }

      "display 'Back' button that links to 'PackageInformation summary' page when adding subsequent value" in {
        val backLinkContainer = createView(packages = Seq(packageInformation)).getElementById("back-link")

        backLinkContainer.getElementById("back-link") must haveHref(
          controllers.declaration.routes.PackageInformationSummaryController.displayPage(Mode.Normal, itemId)
        )
      }

      "display the expected hint paragraphs" in {
        val indexedListOfMessages =
          List("declaration.packageInformation.typesOfPackages.hint.1", "declaration.packageInformation.numberOfPackages.hint").zipWithIndex

        val hints = view.getElementsByClass("govuk-hint")
        forAll(indexedListOfMessages)(t => hints.get(t._2) must containMessage(t._1))
      }

      "display the expected paragraphs" in {
        val indexedListOfParagraphs = List(
          messages("declaration.packageInformation.paragraph"),
          messages("declaration.packageInformation.typesOfPackages.paragraph"),
          messages("declaration.packageInformation.numberOfPackages.details.paragraph.1"),
          messages("declaration.packageInformation.numberOfPackages.details.paragraph.2"),
          messages("declaration.packageInformation.numberOfPackages.details.paragraph.3").substring(0, 8) ++
            messages("declaration.packageInformation.numberOfPackages.details.paragraph.3.link"),
          messages("declaration.packageInformation.shippingMark.paragraph"),
          messages("site.save_and_come_back_later")
        ).zipWithIndex

        val paragraphs = view.getElementsByClass("govuk-body").eachText()
        forAll(indexedListOfParagraphs)(t => paragraphs.get(t._2) mustBe (t._1))
      }

      "display 'Save and continue' button on page" in {
        val saveButton = view.getElementById("submit")
        saveButton must containMessage(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {
        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton must containMessage(saveAndReturnCaption)
      }
    }
  }

  "PackageInformation Add View when adding first value" should {
    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      "display 'Back' button that links to 'statistical value' page when adding first value" in {
        val backLinkContainer = createView(packages = Seq.empty).getElementById("back-link")

        backLinkContainer.getElementById("back-link") must haveHref(
          controllers.declaration.routes.StatisticalValueController.displayPage(Mode.Normal, itemId)
        )
      }
    }

    onJourney(OCCASIONAL, SIMPLIFIED) { implicit request =>
      "display 'Back' button that links to 'NACT code' page when adding first value" in {
        val backLinkContainer = createView(packages = Seq.empty).getElementById("back-link")

        backLinkContainer.getElementById("back-link") must haveHref(
          controllers.declaration.routes.NactCodeSummaryController.displayPage(Mode.Normal, itemId)
        )
      }
    }

    onJourney(CLEARANCE) { implicit request =>
      "display 'Back' button that links to 'commodity details' page when adding first value" in {
        val backLinkContainer = createView(packages = Seq.empty).getElementById("back-link")

        backLinkContainer.getElementById("back-link") must haveHref(
          controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, itemId)
        )
      }
    }
  }

  "PackageInformation Add View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error if nothing is entered" in {
        val view = createView(Some(form.fillAndValidate(PackageInformation("id", None, None, None))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#typesOfPackages")
        view must containErrorElementWithTagAndHref("a", "#numberOfPackages")
        view must containErrorElementWithTagAndHref("a", "#shippingMarks")

        view must containErrorElementWithMessageKey("declaration.packageInformation.typesOfPackages.empty")
        view must containErrorElementWithMessageKey("declaration.packageInformation.numberOfPackages.error")
        view must containErrorElementWithMessageKey("declaration.packageInformation.shippingMark.empty")
      }

      "display error if incorrect PackageInformation is entered" in {
        val view = createView(Some(form.fillAndValidate(PackageInformation("id", Some("invalid"), Some(1), Some("wrong!")))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#typesOfPackages")
        view must containErrorElementWithTagAndHref("a", "#shippingMarks")

        view must containErrorElementWithMessageKey("declaration.packageInformation.typesOfPackages.error")
        view must containErrorElementWithMessageKey("declaration.packageInformation.shippingMark.characterError")
      }
    }
  }

  "PackageInformation Add View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display data in PackageInformation code input" in {

        val view = createView(Some(form.fill(packageInformation)))

        view.getElementById("typesOfPackages").attr("value") must be(packageInformation.typesOfPackages.get)
        view.getElementById("numberOfPackages").attr("value") must be(packageInformation.numberOfPackages.get.toString)
        view.getElementById("shippingMarks").attr("value") must be(packageInformation.shippingMarks.get)
      }
    }
  }
}
