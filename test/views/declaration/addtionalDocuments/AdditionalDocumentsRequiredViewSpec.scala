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

package views.declaration.addtionalDocuments

import base.Injector
import config.AppConfig
import controllers.declaration.routes
import forms.common.YesNoAnswer
import forms.declaration.CommodityDetails
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.data.Form
import play.api.mvc.Call
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.additionalDocuments.additional_documents_required
import views.tags.ViewTest

@ViewTest
class AdditionalDocumentsRequiredViewSpec extends UnitViewSpec with CommonMessages with Injector {

  private val form: Form[YesNoAnswer] = YesNoAnswer.form()

  private val itemId = "itemId"
  private val item = anItem(withItemId(itemId), withAdditionalInformation("1234", "description"))

  private val msgKey = "declaration.additionalDocumentsRequired"

  "'Additional Documents Required' view" should {

    val appConfig = instanceOf[AppConfig]
    val additionalDocumentsRequiredPage = instanceOf[additional_documents_required]

    def createView(form: Form[YesNoAnswer] = form)(implicit request: JourneyRequest[_]): Document =
      additionalDocumentsRequiredPage(Mode.Normal, itemId, form)(request, messages)

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display two Yes/No radio buttons" in {
        val radios = view.getElementsByClass("govuk-radios").first.children
        radios.size mustBe 2
        Option(radios.first.getElementById("code_yes")) must be('defined)
        Option(radios.last.getElementById("code_no")) must be('defined)
      }

      "select the 'Yes' radio when clicked" in {
        val form = YesNoAnswer.form().bind(Map("yesNo" -> "Yes"))
        val view = createView(form = form)
        view.getElementById("code_yes") must beSelected
      }

      "select the 'No' radio when clicked" in {
        val form = YesNoAnswer.form().bind(Map("yesNo" -> "No"))
        val view = createView(form = form)
        view.getElementById("code_no") must beSelected
      }

      "display the 1st paragraph within the inset text" in {
        val insets = view.getElementsByClass("govuk-inset-text").get(0)
        insets.children.first.text mustBe messages(s"$msgKey.inset.text1")
      }

      "display the 2nd paragraph within the inset text and the expected link" when {

        "a commodity code with 10-digits has been entered" in {
          val commodityCode = "4602191000"
          val item = anItem(withItemId(itemId), withCommodityDetails(CommodityDetails(Some(commodityCode), None)))
          val aDeclaration = aDeclarationAfter(request.cacheModel, withItem(item))

          val view = createView()(journeyRequest(aDeclaration))
          val paragraph = view.getElementsByClass("govuk-inset-text").get(0).children.get(1)

          val expectedLinkText = messages(s"$msgKey.inset.link2", commodityCode)
          val expectedHref = appConfig.commodityCodeTariffPageUrl.replace(CommodityDetails.placeholder, commodityCode)

          paragraph.text mustBe messages(s"$msgKey.inset.text2", expectedLinkText)
          paragraph.child(0) must haveHref(expectedHref)
        }

        "a commodity code with 8-digits has been entered" in {
          val commodityCode = "46021910"
          val item = anItem(withItemId(itemId), withCommodityDetails(CommodityDetails(Some(commodityCode), None)))
          val aDeclaration = aDeclarationAfter(request.cacheModel, withItem(item))

          val view = createView()(journeyRequest(aDeclaration))
          val paragraph = view.getElementsByClass("govuk-inset-text").get(0).children.get(1)

          val expectedLinkText = messages(s"$msgKey.inset.link2", commodityCode)
          val expectedHref = appConfig.commodityCodeTariffPageUrl.replace(CommodityDetails.placeholder, s"${commodityCode}00")

          paragraph.text mustBe messages(s"$msgKey.inset.text2", expectedLinkText)
          paragraph.child(0) must haveHref(expectedHref)
        }

        "no commodity code was selected" in {
          val paragraph = view.getElementsByClass("govuk-inset-text").get(0).children.get(1)
          paragraph.text mustBe messages(s"$msgKey.inset.text2", messages(s"$msgKey.inset.link2Alt"))
          paragraph.child(0) must haveHref(appConfig.tradeTariffUrl)
        }
      }

      "display the 3rd paragraph within the inset text" in {
        val paragraph = view.getElementsByClass("govuk-inset-text").get(0).children.last
        paragraph.child(0) must haveHref(appConfig.licensesForExportingGoods)

        val expectedText = messages(s"$msgKey.inset.text3", messages(s"$msgKey.inset.link3"))
        removeBlanksIfAnyBeforeDot(paragraph.text) mustBe expectedText
      }

    }

    onJourney(CLEARANCE)(aDeclaration(withItem(item))) { implicit request =>
      "display page title" in {
        createView().getElementsByTag("h1").first() must containMessage(s"$msgKey.clearance.title")
      }

      "display a 'Back' button that links to the 'Additional Information' page" when {

        "Additional Information are present" in {
          verifyBackButton(routes.AdditionalInformationController.displayPage(Mode.Normal, itemId))
        }
      }
    }
    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL)(aDeclaration(withItem(item))) { implicit request =>
      "display page title" in {
        createView().getElementsByTag("h1").first() must containMessage(s"$msgKey.title")
      }

      "display a 'Back' button that links to the 'Is License Required' page" in {
        verifyBackButton(routes.IsLicenceRequiredController.displayPage(Mode.Normal, itemId))
      }
    }

    def verifyBackButton(call: Call)(implicit request: JourneyRequest[_]): Assertion = {
      val backButton = createView().getElementById("back-link")
      backButton must containMessage(backToPreviousQuestionCaption)
      backButton must haveHref(call)
    }

  }

}
