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
import config.AppConfig
import controllers.declaration.routes.DeclarationChoiceController
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypePage.{form, radioButtonGroupId}
import models.DeclarationType._
import models.{DeclarationType}
import play.twirl.api.Html
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.additional_declaration_type
import views.tags.ViewTest

@ViewTest
class AdditionalDeclarationTypeViewSpec extends UnitViewSpec with CommonMessages with Injector {

  private val appConfig = instanceOf[AppConfig]
  private val additionalTypePage = instanceOf[additional_declaration_type]

  private def createView(declarationType: DeclarationType): Html =
    additionalTypePage(form)(journeyRequest(declarationType), messages)

  "Additional Declaration Type View" when {

    DeclarationType.values.foreach { declarationType =>
      s"the journey selected was $declarationType" should {
        val view = createView(declarationType)

        "display the expected 'Back' button" in {
          val backButton = view.getElementById("back-link")
          backButton.text mustBe messages(backToPreviousQuestionCaption)
          backButton must haveHref(DeclarationChoiceController.displayPage())
        }

        "display the expected section header" in {
          view.getElementById("section-header") must containMessage("declaration.section.1")
        }

        "display the expected page title" in {
          val title = view.getElementsByTag("h1").text
          title mustBe messages(s"declaration.declarationType.header.${declarationType}")
        }

        "display the expected tariff details" in {
          val expectedKey = if (declarationType == CLEARANCE) "clearance" else "common"

          val tariffTitle = view.getElementsByClass("govuk-details__summary-text").last
          tariffTitle.text mustBe messages(s"tariff.expander.title.$expectedKey")

          val tariffDetails = view.getElementsByClass("govuk-details__text").last.text

          val expectedText =
            removeLineBreakIfAny(messages(s"tariff.declaration.type.$expectedKey.text", messages(s"tariff.declaration.type.$expectedKey.linkText.0")))
          removeBlanksIfAnyBeforeDot(tariffDetails) mustBe expectedText
        }

        "display the expected 'Continue' button" in {
          view.getElementById("submit").text mustBe messages(continueCaption)
        }

        "NOT display the 'Save and come back later' button" in {
          Option(view.getElementById("submit_and_return")) mustBe None
        }
      }
    }

    DeclarationType.values.filterNot(_ == SUPPLEMENTARY).foreach { declarationType =>
      s"the journey selected was $declarationType" should {
        val view = createView(declarationType)

        def additionalTypesForType(declarationType: DeclarationType): Seq[AdditionalDeclarationType] =
          declarationType match {
            case STANDARD   => List(STANDARD_FRONTIER, STANDARD_PRE_LODGED)
            case SIMPLIFIED => List(SIMPLIFIED_FRONTIER, SIMPLIFIED_PRE_LODGED)
            case OCCASIONAL => List(OCCASIONAL_FRONTIER, OCCASIONAL_PRE_LODGED)
            case CLEARANCE  => List(CLEARANCE_FRONTIER, CLEARANCE_PRE_LODGED)
          }

        "display the expected radio buttons" in {
          val radioGroups = view.getElementsByClass("govuk-radios")
          radioGroups.size mustBe 1

          val radioItems = radioGroups.first.getElementsByClass("govuk-radios__item")
          radioItems.size mustBe 2

          val additionalTypes = additionalTypesForType(declarationType)

          val radioItem1 = radioItems.first
          radioItem1.children.size mustBe 2

          val radio1 = radioItem1.child(0)
          radio1.tagName mustBe "input"
          radio1.id mustBe "arrived"
          radio1.attr("name") mustBe radioButtonGroupId
          radio1.attr("value") mustBe additionalTypes.head.toString

          val labelForRadio1 = radioItem1.child(1)
          labelForRadio1.tagName mustBe "label"
          labelForRadio1.attr("for") mustBe "arrived"
          labelForRadio1.text mustBe messages("declaration.declarationType.radio.arrived", additionalTypes.head.toString)

          val radioItem2 = radioItems.last
          radioItem2.children.size mustBe 2

          val radio2 = radioItem2.child(0)
          radio1.tagName mustBe "input"
          radio2.id mustBe "prelodged"
          radio2.attr("name") mustBe radioButtonGroupId
          radio2.attr("value") mustBe additionalTypes.last.toString

          val labelForRadio2 = radioItem2.child(1)
          labelForRadio2.tagName mustBe "label"
          labelForRadio2.attr("for") mustBe "prelodged"
          labelForRadio2.text mustBe messages("declaration.declarationType.radio.prelodged", additionalTypes.last.toString)

        }

        "display the expected notification banner" in {
          val banner = view.getElementsByClass("govuk-notification-banner").get(0)

          val title = banner.getElementsByClass("govuk-notification-banner__title").text
          title mustBe messages("declaration.declarationType.notification.title")

          val content = banner.getElementsByClass("govuk-notification-banner__content").get(0)
          content.text mustBe messages(
            "declaration.declarationType.notification.content",
            messages("declaration.declarationType.notification.content.link")
          )
          val link = content.getElementsByClass("govuk-link").first
          link must haveHref(appConfig.guidance.january2022locations)
        }

        "display the expander for Arrived declarations" in {
          val expander = view.getElementById("submission-time-info")
          expander.children.size mustBe 2

          val title = expander.child(0).text
          title mustBe messages("declaration.declarationType.expander.title")

          val paragraphs = expander.child(1).getElementsByClass("govuk-body")
          paragraphs.size mustBe 3

          val firstParagraph = paragraphs.get(0)
          firstParagraph.text mustBe messages("declaration.declarationType.expander.paragraph.1")
          paragraphs.get(1).text mustBe messages("declaration.declarationType.expander.paragraph.2")
          paragraphs.get(2).text mustBe messages("declaration.declarationType.expander.paragraph.3")
        }
      }
    }

    "the journey selected was SUPPLEMENTARY" should {
      val view = createView(SUPPLEMENTARY)

      "display the expected radio buttons" in {
        val radioGroups = view.getElementsByClass("govuk-radios")
        radioGroups.size mustBe 1

        val radioItems = radioGroups.first.getElementsByClass("govuk-radios__item")
        radioItems.size mustBe 2

        val radioItem1 = radioItems.first
        radioItem1.children.size mustBe 2

        val radio1 = radioItem1.child(0)
        radio1.tagName mustBe "input"
        radio1.id mustBe "simplified"
        radio1.attr("name") mustBe radioButtonGroupId
        radio1.attr("value") mustBe SUPPLEMENTARY_SIMPLIFIED.toString

        val labelForRadio1 = radioItem1.child(1)
        labelForRadio1.tagName mustBe "label"
        labelForRadio1.attr("for") mustBe "simplified"
        labelForRadio1.text mustBe messages("declaration.declarationType.radio.supplementary.simplified", SUPPLEMENTARY_SIMPLIFIED)

        val radioItem2 = radioItems.last
        radioItem2.children.size mustBe 2

        val radio2 = radioItem2.child(0)
        radio1.tagName mustBe "input"
        radio2.id mustBe "eidr"
        radio2.attr("name") mustBe radioButtonGroupId
        radio2.attr("value") mustBe SUPPLEMENTARY_EIDR.toString

        val labelForRadio2 = radioItem2.child(1)
        labelForRadio2.tagName mustBe "label"
        labelForRadio2.attr("for") mustBe "eidr"
        labelForRadio2.text mustBe messages("declaration.declarationType.radio.supplementary.eidr", SUPPLEMENTARY_EIDR)
      }

      "NOT display any notification banner" in {
        view.getElementsByClass("govuk-notification-banner").size mustBe 0
      }

      "NOT display the expander for Arrived declarations" in {
        Option(view.getElementById("submission-time-info")) mustBe None
      }
    }
  }
}
