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

import base.{Injector, TestHelper}
import connectors.CodeListConnector
import controllers.declaration.routes
import forms.declaration.LocationOfGoods
import models.DeclarationType._
import models.{DeclarationType, Mode}
import models.codes.Country
import org.jsoup.nodes.{Document, Element}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{Assertion, BeforeAndAfterEach}
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.mvc.Call
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.location_of_goods
import views.tags.ViewTest

import scala.collection.JavaConverters.asScalaIteratorConverter
import scala.collection.immutable.ListMap

@ViewTest
class LocationOfGoodsViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector with MockitoSugar with BeforeAndAfterEach {

  implicit val mockCodeListConnector = mock[CodeListConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  private val page = instanceOf[location_of_goods]
  private val form: Form[LocationOfGoods] = LocationOfGoods.form()

  private def createView(form: Form[LocationOfGoods] = form, declarationType: DeclarationType = DeclarationType.STANDARD): Document =
    page(Mode.Normal, form)(journeyRequest(declarationType), messages)

  val prefix = "declaration.locationOfGoods"

  "Goods Location View on empty page" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor(s"$prefix.code.empty")
      messages must haveTranslationFor(s"$prefix.code.error")
      messages must haveTranslationFor("tariff.declaration.locationOfGoods.clearance.text")
    }

    val view = createView()

    "display same page title as header" in {
      val viewWithMessage = createView()

      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.3")
    }

    "display header" in {
      view.getElementsByTag("h1") must containMessageForElements(s"$prefix.title")
    }

    "display main body text" in {
      view.getElementsByClass("govuk-body").get(0).text mustBe messages(s"$prefix.body")
    }

    "display the inset text" in {
      val insetContent = view.getElementsByClass("govuk-inset-text").get(0).children

      insetContent.get(0).text mustBe messages(s"$prefix.inset.1")
      insetContent.get(1).text mustBe messages(s"$prefix.inset.2")

      val ul = insetContent.last.children.get(0)
      val bulletPoints = ul.children
      bulletPoints.size mustBe 8

      bulletPoints.iterator.asScala.zipWithIndex.foreach { elementAndIndex =>
        val (element, index) = elementAndIndex
        element.text mustBe messages(s"$prefix.inset.bullet${index + 1}")
      }
    }

    "display goods location expander" in {
      val expander = view.getElementsByClass("govuk-details").first.children
      expander.size mustBe 2
      expander.first.text mustBe messages(s"$prefix.expander.title")

      val details = expander.last

      val sections = details.children
      sections.size mustBe 20
      sections.first.text mustBe messages(s"$prefix.expander.intro01")

      val iterator: Iterator[Element] = sections.iterator.asScala.drop(1)

      for (ix <- 1 to 9) {
        val title = iterator.next
        title.text mustBe messages(s"$prefix.expander.paragraph$ix.title")

        if (ix < 9) {
          val hint = iterator.next
          assert(hint.hasClass("govuk-hint"))

          val link1 = hint.children.get(0)
          assert(link1.hasClass("govuk-link"))
          link1.text mustBe messages(s"$prefix.expander.paragraph$ix.link1")
        }
      }

      for (ix <- 9 to 10) {
        val hint = iterator.next
        assert(hint.hasClass("govuk-hint"))
        val expectedText = removeLineBreakIfAny(
          messages(
            s"$prefix.expander.paragraph$ix.text",
            messages(s"$prefix.expander.paragraph$ix.link1"),
            messages(s"$prefix.expander.paragraph$ix.link2")
          )
        )
        hint.text mustBe expectedText
      }
    }

    "display tariff expander" in {
      view.getElementsByClass("govuk-details__summary-text").get(1) must containHtml(messages("tariff.expander.title.common"))
    }

    "display 'Save and continue' button" in {
      val saveButton = view.getElementById("submit")

      saveButton must containMessage("site.save_and_continue")
    }

    "display 'Save and return' button" in {
      val saveButton = view.getElementById("submit_and_return")

      saveButton must containMessage("site.save_and_come_back_later")
    }
  }

  "Goods Location View for invalid input" should {

    "display error for empty Goods Location code" in {
      verifyError("", "empty")
    }

    "display error for incorrect country in the Goods Location code" in {
      verifyError("XXAU1234567")
    }

    "display error for incorrect type of location in the Goods Location code" in {
      verifyError("GBXU12345678")
    }

    "display error for incorrect qualifier of identification in the Goods Location code" in {
      verifyError("PLAX12345678")
    }

    "display error for too short code" in {
      verifyError("PLAU123")
    }

    "display error for too long code" in {
      verifyError(s"PLAU${TestHelper.createRandomAlphanumericString(36)}")
    }

    "display error for non-alphanumeric code" in {
      verifyError("PLAX12345678-#@")
    }
  }

  "Goods Location view" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { request =>
      behave like viewWithCorrectBackButton(request.declarationType, routes.RoutingCountriesSummaryController.displayPage())
    }

    onJourney(SUPPLEMENTARY, CLEARANCE) { request =>
      behave like viewWithCorrectBackButton(request.declarationType, routes.DestinationCountryController.displayPage())
    }

    def viewWithCorrectBackButton(declarationType: DeclarationType, redirect: Call): Unit =
      "have correct back-link" when {

        val view = createView(declarationType = declarationType)

        "display 'Back' button that links to correct page" in {

          val backButton = view.getElementById("back-link")

          backButton must containMessage("site.back")
          backButton.getElementById("back-link") must haveHref(redirect)
        }
      }
  }

  private def verifyError(code: String, errorKey: String = "error"): Assertion = {
    val form = LocationOfGoods.form.fillAndValidate(LocationOfGoods(code))
    val view = createView(form)

    view must haveGovukGlobalErrorSummary
    view must containErrorElementWithTagAndHref("a", "#code")

    view must containErrorElementWithMessageKey(s"$prefix.code.$errorKey")
  }
}
