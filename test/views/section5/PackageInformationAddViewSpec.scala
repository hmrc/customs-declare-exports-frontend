/*
 * Copyright 2024 HM Revenue & Customs
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

package views.section5

import base.Injector
import controllers.section5.routes._
import forms.section5.PackageInformation
import forms.section5.PackageInformation.form
import models.DeclarationType._
import models.declaration.ProcedureCodesData.lowValueDeclaration
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.Inspectors.forAll
import play.api.data.Form
import services.PackageTypesService
import services.cache.ExportsTestHelper
import views.common.PageWithButtonsSpec
import views.html.section5.packageInformation.package_information_add
import views.tags.ViewTest

@ViewTest
class PackageInformationAddViewSpec extends PageWithButtonsSpec with ExportsTestHelper with Injector {

  import PackageInformationViewSpec._

  implicit val packageTypesService: PackageTypesService = instanceOf[PackageTypesService]

  val page = instanceOf[package_information_add]

  override val typeAndViewInstance = (STANDARD, page(itemId, form)(_, _))

  def createView(frm: Form[PackageInformation] = form)(implicit request: JourneyRequest[_]): Document =
    page(itemId, frm)(request, messages)

  val itemWithPackageInfo = anItem(withPackageInformation(packageInformation))

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

    onEveryDeclarationJourney(withItems(itemWithPackageInfo)) { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.packageInformation.title")
      }

      "display 'Back' button that links to 'PackageInformation summary' page when adding subsequent value" in {
        val backLink = page(itemWithPackageInfo.id, form)(request, messages).getElementById("back-link")
        backLink must haveHref(PackageInformationSummaryController.displayPage(itemWithPackageInfo.id))
      }

      "display the expected hint paragraphs" in {
        val indexedListOfMessages =
          List("declaration.packageInformation.typesOfPackages.hint.noJs", "declaration.packageInformation.numberOfPackages.hint").zipWithIndex

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
          messages(exitAndReturnCaption)
        ).zipWithIndex

        val paragraphs = view.getElementsByClass("govuk-body").eachText()
        forAll(indexedListOfParagraphs)(t => paragraphs.get(t._2) mustBe (t._1))
      }

      checkAllSaveButtonsAreDisplayed(view)
    }
  }

  "PackageInformation Add View when adding first value" should {
    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      "display 'Back' button that links to 'statistical value' page when adding first value" in {
        val backLink = createView().getElementById("back-link")
        backLink must haveHref(StatisticalValueController.displayPage(itemId))
      }
    }

    onJourney(OCCASIONAL, SIMPLIFIED) { implicit request =>
      "display 'Back' button that links to 'statistical value' on 'low value' declarations" in {
        val item = anItem(withItemId(itemId), withProcedureCodes(additionalProcedureCodes = Seq(lowValueDeclaration)))
        val requestWithCache = journeyRequest(aDeclarationAfter(request.cacheModel, withItems(item)))
        val view = createView()(requestWithCache)
        view.getElementById("back-link") must haveHref(StatisticalValueController.displayPage(itemId))
      }
    }

    onJourney(OCCASIONAL, SIMPLIFIED) { implicit request =>
      "display 'Back' button that links to 'NACT code' page when adding first value" in {
        val backLink = createView().getElementById("back-link")
        backLink must haveHref(NactCodeSummaryController.displayPage(itemId))
      }
    }

    onJourney(CLEARANCE) { implicit request =>
      "display 'Back' button that links to 'commodity details' page when adding first value" in {
        val backLink = createView().getElementById("back-link")
        backLink must haveHref(CommodityDetailsController.displayPage(itemId))
      }
    }
  }

  "PackageInformation Add View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error if nothing is entered" in {
        val view = createView(form.fillAndValidate(PackageInformation(1, "id", None, None, None)))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#typesOfPackages")
        view must containErrorElementWithTagAndHref("a", "#numberOfPackages")
        view must containErrorElementWithTagAndHref("a", "#shippingMarks")

        view must containErrorElementWithMessageKey("declaration.packageInformation.typesOfPackages.empty")
        view must containErrorElementWithMessageKey("declaration.packageInformation.numberOfPackages.error")
        view must containErrorElementWithMessageKey("declaration.packageInformation.shippingMark.empty")
      }

      "display error if incorrect PackageInformation is entered" in {
        val view = createView(form.fillAndValidate(PackageInformation(1, "id", Some("invalid"), Some(1), Some("wrong!"))))

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
        val view = createView(form.fill(packageInformation))

        view.getElementById("typesOfPackages").attr("value") must be(packageInformation.typesOfPackages.get)
        view.getElementById("numberOfPackages").attr("value") must be(packageInformation.numberOfPackages.get.toString)
        view.getElementById("shippingMarks").attr("value") must be(packageInformation.shippingMarks.get)
      }
    }
  }
}
