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
import controllers.declaration.routes
import forms.common.Eori
import forms.declaration.PersonPresentingGoodsDetails
import models.DeclarationType
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.person_presenting_goods_details

class PersonPresentingGoodsDetailsViewSpec extends UnitViewSpec with Injector with CommonMessages {

  private val page = instanceOf[person_presenting_goods_details]
  private def createView(form: Form[PersonPresentingGoodsDetails] = PersonPresentingGoodsDetails.form): Document =
    page(form)(journeyRequest(DeclarationType.CLEARANCE), messages)

  "Person Presenting Goods Details view" when {

    "on empty page" should {

      "display page title" in {

        createView().getElementsByTag("h1").first() must containMessage("declaration.personPresentingGoodsDetails.title")
      }

      "display section header" in {

        createView().getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display text input element" in {

        createView().getElementById("eori").attr("value") mustBe empty
      }

      "display 'For more information about this' element" in {

        val details = createView().getElementsByClass("govuk-details").first()

        details must containMessage("tariff.expander.title.clearance")
      }

      "display 'Back' button" in {

        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(routes.EntryIntoDeclarantsRecordsController.displayPage.url)
      }

    }

    "on page filled" should {
      "have the text input element pre-populated with value from cache" in {

        val form = PersonPresentingGoodsDetails.form.fill(PersonPresentingGoodsDetails(Eori("GB1234567890000")))

        createView(form).getElementById("eori").attr("value") mustBe "GB1234567890000"
      }
    }

    "provided with form with errors" should {

      "display error summary" in {

        val form = PersonPresentingGoodsDetails.form.withError(FormError("eori", "declaration.eori.error.format"))

        createView(form) must haveGovukGlobalErrorSummary
      }

      "display error next to the text input element" in {

        val form = PersonPresentingGoodsDetails.form.withError(FormError("eori", "declaration.eori.error.format"))

        createView(form) must haveGovukFieldError("eori", messages("declaration.eori.error.format"))
      }
    }
  }

}
