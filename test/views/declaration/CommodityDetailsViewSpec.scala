/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.declaration.routes.{AdditionalFiscalReferencesController, AdditionalProcedureCodesController, FiscalInformationController}
import forms.declaration.{CommodityDetails, FiscalInformation}
import models.DeclarationType
import models.DeclarationType.DeclarationType
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.UnitViewSpec
import views.html.declaration.commodity_details
import views.tags.ViewTest

@ViewTest
class CommodityDetailsViewSpec extends UnitViewSpec with Injector {

  val page = instanceOf[commodity_details]

  def createView(form: Form[CommodityDetails])(implicit request: JourneyRequest[_]): Document =
    page(itemId, form)(request, messages)

  // scalastyle:off
  def commodityDetailsView(
    declarationType: DeclarationType,
    form: Form[CommodityDetails],
    commodityDetails: Option[CommodityDetails] = None
  ): Unit = {
    val view = createView(commodityDetails.fold(form)(form.fill))(journeyRequest(declarationType))

    "display the expected notification banner" in {
      val banner = view.getElementsByClass("govuk-notification-banner").get(0)

      val title = banner.getElementsByClass("govuk-notification-banner__title").text
      title mustBe messages("declaration.commodityDetails.notification.title")

      val content = banner.getElementsByClass("govuk-notification-banner__content").get(0)
      content.child(0).text mustBe messages("declaration.commodityDetails.notification.body.1")
      content.child(1).text mustBe messages("declaration.commodityDetails.notification.body.2")
    }

    "display the section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.5")
    }

    "display page title" in {
      view.getElementById("title").text mustBe messages("declaration.commodityDetails.title")
    }

    "display body texts for the commodity code input field" in {
      val label = view.getElementsByClass("govuk-heading-m").get(0).child(0)
      label.tagName() mustBe "label"
      label.attr("for") mustBe "combinedNomenclatureCode"
      label.text mustBe messages("declaration.commodityDetails.combinedNomenclatureCode.label")

      val commodityCodeLookupLink = view.getElementsByClass("govuk-body").get(2).child(0)
      commodityCodeLookupLink.tagName() mustBe "a"
      commodityCodeLookupLink must haveHref("https://www.gov.uk/guidance/using-the-trade-tariff-tool-to-find-a-commodity-code")
      commodityCodeLookupLink.text mustBe messages("declaration.commodityDetails.combinedNomenclatureCode.body.1.link")

      val body2 = view.getElementsByClass("govuk-body").get(3).text
      body2 mustBe messages("declaration.commodityDetails.combinedNomenclatureCode.body.2")
    }

    "display the commodity code input field" in {
      val expectedCode = commodityDetails.flatMap(_.combinedNomenclatureCode).getOrElse("")
      view.getElementById(CommodityDetails.combinedNomenclatureCodeKey).attr("value") mustBe expectedCode
    }

    "display a label texts for the description textarea field" in {
      val label = view.getElementsByClass("govuk-heading-m").get(1).child(0)
      label.tagName() mustBe "label"
      label.attr("for") mustBe "descriptionOfGoods"
      label.text mustBe messages("declaration.commodityDetails.description.label")
    }

    "display a hint text for the description textarea field" in {
      val hint = view.getElementsByClass("govuk-hint").get(0)
      hint.text mustBe messages("declaration.commodityDetails.description.hint")
    }

    "display the description textarea field" in {
      val expectedDescription = commodityDetails.flatMap(_.descriptionOfGoods).getOrElse("")
      view.getElementById(CommodityDetails.descriptionOfGoodsKey).text mustBe expectedDescription
    }

    "display 'Back' button that links to the 'Additional Procedure Codes' page" in {
      val backButton = view.getElementById("back-link")
      backButton.getElementById("back-link") must haveHref(AdditionalProcedureCodesController.displayPage(itemId))
    }

    "display 'Back' button that links to the 'Fiscal References List' page" when {
      "Procedure code is for Onward Supply Relief and fiscal information are entered" in {
        val item = anItem(withItemId(itemId), withProcedureCodes(), withFiscalInformation(), withAdditionalFiscalReferenceData())
        val declaration = aDeclaration(withType(declarationType), withItem(item))

        val view = createView(commodityDetails.fold(form)(form.fill))(journeyRequest(declaration))

        val backButton = view.getElementById("back-link")
        backButton.getElementById("back-link") must haveHref(AdditionalFiscalReferencesController.displayPage(itemId))
      }
    }

    "display 'Back' button that links to the 'Onward Supply Relief' page" when {
      "Procedure code is for Onward Supply Relief and fiscal information are NOT entered" in {
        val item = anItem(withItemId(itemId), withProcedureCodes(), withFiscalInformation(FiscalInformation("No")))
        val declaration = aDeclaration(withType(declarationType), withItem(item))

        val view = createView(commodityDetails.fold(form)(form.fill))(journeyRequest(declaration))

        val backButton = view.getElementById("back-link")
        backButton.getElementById("back-link") must haveHref(FiscalInformationController.displayPage(itemId))
      }
    }

    "display 'Save and continue' button on page" in {
      val saveButton = view.select("#submit")
      saveButton.text mustBe messages(saveAndContinueCaption)
    }
  }
  // scalastyle:on

  "Commodity Details View on empty page" when {
    for (decType <- DeclarationType.values)
      s"we are on $decType journey" should {
        behave like commodityDetailsView(decType, CommodityDetails.form(journeyRequest(decType)))
      }
  }

  "Commodity Details View on populated page" when {
    val details = Some(CommodityDetails(Some("12345678"), Some("Description")))

    for (decType <- DeclarationType.values)
      s"we are on $decType journey" should {
        behave like commodityDetailsView(decType, CommodityDetails.form(journeyRequest(decType)), details)
      }
  }
}
