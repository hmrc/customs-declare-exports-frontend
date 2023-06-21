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

import base.ExportsTestData.itemWithPC
import base.Injector
import controllers.declaration.routes.{SupervisingCustomsOfficeController, TransportLeavingTheBorderController}
import controllers.helpers.TransportSectionHelper.additionalDeclTypesAllowedOnInlandOrBorder
import forms.declaration.InlandOrBorder.{form, Border, Inland}
import models.DeclarationType.STANDARD
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.inland_border
import views.tags.ViewTest

@ViewTest
class InlandOrBorderViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[inland_border]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  def createView()(implicit request: JourneyRequest[_]): Document = page(form)(request, messages)

  "Inland or Border View" when {

    additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
      s"AdditionalDeclarationType is ${additionalType} and" should {
        implicit val request = withRequest(additionalType)
        val view = createView()

        "display page title" in {
          view.getElementsByTag("legend").text mustBe messages("declaration.inlandOrBorder.title")
        }

        s"display the '${Border.location}' option" in {
          val element = view.getElementsByAttributeValue("for", Border.location).first
          element.text mustBe messages("declaration.inlandOrBorder.answer.border")
        }

        s"display the '${Inland.location}' option" in {
          val element = view.getElementsByAttributeValue("for", Inland.location).first
          element.text mustBe messages("declaration.inlandOrBorder.answer.inland")
        }

        "display the expected inset text" in {
          val insetText = view.getElementsByClass("govuk-inset-text")
          insetText.size mustBe 1

          val content = insetText.first.children
          content.size mustBe 2
          content.get(0).text mustBe messages("declaration.inlandOrBorder.inset.body")

          val bulletPoints = content.get(1).getElementsByClass("govuk-list--bullet").first.children
          bulletPoints.size mustBe 4
          for (ix <- 1 to 4)
            bulletPoints.get(ix - 1).text mustBe messages(s"declaration.inlandOrBorder.inset.bullet.$ix.text")
        }

        checkAllSaveButtonsAreDisplayed(createView())

        "display 'Back' button that links to the 'Supervising Customs Office' page" in {
          val backButton = view.getElementById("back-link")
          backButton must containMessage("site.backToPreviousQuestion")
          backButton must haveHref(SupervisingCustomsOfficeController.displayPage)
        }

        "display the expected tariff details" in {
          val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
          tariffTitle.text mustBe messages(s"tariff.expander.title.common")

          val tariffDetails = view.getElementsByClass("govuk-details__text").first

          val prefix = "tariff.declaration.inlandOrBorder"
          val expectedText = messages(s"$prefix.common.text", messages(s"$prefix.common.linkText.0"))
          val actualText = removeBlanksIfAnyBeforeDot(tariffDetails.text)
          actualText mustBe removeLineBreakIfAny(expectedText)
        }
      }
    }

    additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
      s"AdditionalDeclarationType is ${additionalType} and" when {
        "all declaration's items have '1040' as Procedure code and '000' as unique Additional Procedure code" should {
          implicit val request = withRequest(additionalType, withItem(itemWithPC("1040")))

          "display 'Back' button that links to the 'Transport Leaving the Border' page" in {
            val backButton = createView().getElementById("back-link")
            backButton must containMessage("site.backToPreviousQuestion")
            backButton must haveHref(TransportLeavingTheBorderController.displayPage)
          }
        }
      }
    }
  }
}
