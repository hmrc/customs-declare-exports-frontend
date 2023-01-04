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
import controllers.declaration.routes
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.ContainerFirst
import models.DeclarationType.SUPPLEMENTARY
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.transport_container_add_first
import views.tags.ViewTest

@ViewTest
class TransportContainerAddFirstViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with CommonMessages with Injector {

  private val form: Form[ContainerFirst] = ContainerFirst.form
  private val page = instanceOf[transport_container_add_first]

  private def createView(form: Form[ContainerFirst] = form): Document =
    page(form)(journeyRequest(), messages)

  "Transport Containers Add First View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.transportInformation.containers.first.title")
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.6")
    }

    "display radio button with Yes option" in {
      view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
      view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("declaration.transportInformation.containers.yes")
    }

    "display radio button with No option" in {
      view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
      view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("declaration.transportInformation.containers.no")
    }

    "display paragraph text" in {
      val para = view.getElementsByClass("govuk-body")
      val expected = Seq(messages("declaration.transportInformation.containers.paragraph")).mkString(" ")
      para.get(0) must containText(expected)
    }

    "display bullet list text" in {
      val bullets = view.getElementsByClass("govuk-list--bullet")
      val expected = Seq(
        messages("declaration.transportInformation.containers.bullet1"),
        messages("declaration.transportInformation.containers.bullet2"),
        messages("declaration.transportInformation.containers.bullet3")
      ).mkString(" ")
      bullets.get(0) must containText(expected)
    }

    "display 'Back' button that links to the 'Express Consignment' page" when {
      "declaration's type is STANDARD" in {
        val backLinkContainer = view.getElementById("back-link")
        backLinkContainer must containMessage(backToPreviousQuestionCaption)
        backLinkContainer must haveHref(routes.ExpressConsignmentController.displayPage)
      }
    }

    "display 'Back' button that links to the 'Transport Country' page" when {
      "declaration's type is SUPPLEMENTARY" in {
        val view = page(form)(journeyRequest(SUPPLEMENTARY), messages)
        val backLinkContainer = view.getElementById("back-link")
        backLinkContainer must containMessage(backToPreviousQuestionCaption)
        backLinkContainer must haveHref(routes.TransportCountryController.displayPage)
      }
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }

  "Transport Containers Add View" should {
    "display errors for invalid input" in {
      val view = createView(ContainerFirst.form.fillAndValidate(ContainerFirst(Some("abc123@#"))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")
      view must containErrorElementWithMessageKey("declaration.transportInformation.containerId.error.invalid")
    }

    "display errors for invalid length" in {
      val view = createView(ContainerFirst.form.fillAndValidate(ContainerFirst(Some("123456789012345678"))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")
      view must containErrorElementWithMessageKey("declaration.transportInformation.containerId.error.length")
    }
  }

  "Transport Containers Add View when filled" should {
    "display data in Container ID input" in {
      val view = createView(ContainerFirst.form.fill(ContainerFirst(Some("Test"))))

      view.getElementsByAttributeValue("for", "id").get(0) must containMessage("declaration.transportInformation.containerId")
      view.getElementById("id").attr("value") must be("Test")
    }
  }
}
