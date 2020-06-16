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
import controllers.declaration.routes
import controllers.util.{SaveAndContinue, SaveAndReturn}
import forms.common.YesNoAnswer
import forms.declaration.AdditionalInformation
import helpers.views.declaration.CommonMessages
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import services.cache.ExportsTestData
import unit.tools.Stubs
import utils.ListItem
import views.declaration.spec.UnitViewSpec
import views.html.declaration.additionalInformtion.additional_information
import views.tags.ViewTest

@ViewTest
class AdditionalInformationViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs with Injector {

  val itemId = "a7sc78"
  private val form: Form[YesNoAnswer] = YesNoAnswer.form()
  private val page = instanceOf[additional_information]

  private def createView(form: Form[YesNoAnswer] = form, cachedData: Seq[AdditionalInformation] = Seq())(
    implicit request: JourneyRequest[_]
  ): Document =
    page(Mode.Normal, itemId, form, cachedData)(request, messages)

  "Additional Information View" should {

    "have a proper messages" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages must haveTranslationFor("declaration.additionalInformation.table.heading")
      messages must haveTranslationFor("declaration.additionalInformation.table.multiple.heading")
      messages must haveTranslationFor("declaration.additionalInformation.code")
      messages must haveTranslationFor("declaration.additionalInformation.item.code")
      messages must haveTranslationFor("declaration.additionalInformation.description")
      messages must haveTranslationFor("declaration.additionalInformation.item.description")
      messages must haveTranslationFor("declaration.additionalInformation.table.update.hint")
    }
  }

  "Additional Information View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {

        createView().getElementsByTag("h1").text() mustBe messages("declaration.additionalInformation.table.multiple.heading")
      }

      "display section header" in {

        createView().getElementById("section-header").text() must include("supplementary.items")
      }

      "display 'Back' button that links to 'Commodity measure' page" when {
        "on the Standard journey" in {

          val backButton = createView().getElementById("back-link")

          backButton.text() mustBe messages(backCaption)
          backButton.attr("href") mustBe routes.AdditionalInformationRequiredController.displayPage(Mode.Normal, itemId).url
        }

        "on the Simplified journey" in {

          val backButton = createView().getElementById("back-link")

          backButton.text() mustBe messages(backCaption)
          backButton.attr("href") mustBe routes.AdditionalInformationRequiredController.displayPage(Mode.Normal, itemId).url
        }
      }

      "display 'Save and continue' button" in {
        val view: Document = createView()
        view must containElement("button").withName(SaveAndContinue.toString)
      }

      "display 'Save and return' button" in {
        val view: Document = createView()
        view must containElement("button").withName(SaveAndReturn.toString)
      }

    }
  }

  "Additional Information View when filled" should {

    "display one row with data in table" which {
      onEveryDeclarationJourney() { implicit request =>
        val additionalInformation = AdditionalInformation("12345", "12345678")
        val view = page(Mode.Normal, itemId, form, Seq(additionalInformation))(journeyRequest(DeclarationType.STANDARD), messages)
        val row = view.selectFirst("#additional_information tbody tr")

        "has Code header" in {
          view.select("#additional_information thead tr th").get(0).text() mustBe "declaration.additionalInformation.table.headers.code"
        }

        "has 'Required information' header" in {
          view
            .select("#additional_information thead tr th")
            .get(1)
            .text() mustBe "declaration.additionalInformation.table.headers.description"
        }

        "has row with 'Code' in " in {
          view.select("#additional_information-row0-code").first().text() mustBe "12345"
        }

        "has row with 'Required information" in {
          view.select("#additional_information-row0-info").first().text() mustBe "12345678"
        }

        "have change link" in {
          val removeLink = row.select(".govuk-link").get(0)
          removeLink.text() mustBe messages("site.change") + messages("declaration.additionalInformation.table.update.hint")
          removeLink must haveHref(
            controllers.declaration.routes.AdditionalInformationChangeController
              .displayPage(Mode.Normal, itemId, ListItem.createId(0, additionalInformation))
          )
        }

        "have remove link" in {
          val removeLink = row.select(".govuk-link").get(1)
          removeLink.text() mustBe messages("site.remove") + messages("declaration.additionalInformation.table.update.hint")
          removeLink must haveHref(
            controllers.declaration.routes.AdditionalInformationRemoveController
              .displayPage(Mode.Normal, itemId, ListItem.createId(0, additionalInformation))
          )
        }

      }
    }
  }
}
