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
import config.AppConfig
import connectors.CodeListConnector
import controllers.declaration.routes
import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1007, ChoiceOthers}
import forms.declaration.LocationOfGoods
import forms.declaration.LocationOfGoods.form
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import models.DeclarationType._
import models.Mode
import models.codes.Country
import models.requests.JourneyRequest
import org.jsoup.nodes.{Document, Element}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{Assertion, BeforeAndAfterEach}
import play.api.mvc.Call
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.location_of_goods
import views.tags.ViewTest

import scala.collection.JavaConverters.asScalaIteratorConverter
import scala.collection.immutable.ListMap

@ViewTest
class LocationOfGoodsViewSpec extends UnitViewSpec with Stubs with Injector with BeforeAndAfterEach {

  private val appConfig = instanceOf[AppConfig]

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

  "Goods Location View" when {
    def createView(implicit request: JourneyRequest[_]): Document = page(Mode.Normal, form)

    val prefix = "declaration.locationOfGoods"

    // V1 Content (default)
    AdditionalDeclarationType.values.toList.foreach { additionalType =>
      s"AdditionalDeclarationType is $additionalType" should {
        implicit val request = withRequest(additionalType)
        val view = createView

        "display same page title as header" in {
          view.title must include(view.getElementsByTag("h1").text)
        }

        "display section header" in {
          view.getElementById("section-header") must containMessage("declaration.section.3")
        }

        "display the expected page title" in {
          val title = view.getElementsByTag("h1").text
          title mustBe messages(s"$prefix.title.v1")
        }

        "display the expected body" in {
          val paragraphs = view.getElementsByClass("govuk-body")
          paragraphs.size mustBe 5

          paragraphs.get(0).text mustBe messages(s"$prefix.body.v1.1")
          paragraphs.get(1).text mustBe messages(s"$prefix.body.v1.2")
          paragraphs.get(2).text mustBe messages(s"$prefix.body.v1.3")
        }

        "display the 'Find the goods location code' expander " in {
          verifyExpander(view, 1)
        }

        "display the expected hint" in {
          val hint = view.getElementById("code-hint")
          assert(hint.hasClass("govuk-hint"))
          hint.text mustBe messages(s"$prefix.hint.v1")
        }

        "display the expected tariff details" in {
          val key = if (request.isType(CLEARANCE)) "clearance" else "common"

          val tariffTitle = view.getElementsByClass("govuk-details__summary-text").last
          tariffTitle.text mustBe messages(s"tariff.expander.title.$key")

          val tariffDetails = view.getElementsByClass("govuk-details__text").last.text

          val keyPrefix = s"tariff.declaration.locationOfGoods.$key"
          val expectedText = removeLineBreakIfAny(messages(s"$keyPrefix.text", messages(s"$keyPrefix.linkText.0")))
          removeBlanksIfAnyBeforeDot(tariffDetails) mustBe expectedText
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
    }

    // V2 Content
    List(STANDARD_FRONTIER, SIMPLIFIED_FRONTIER, OCCASIONAL_FRONTIER, CLEARANCE_FRONTIER).foreach { additionalType =>
      s"AdditionalDeclarationType is $additionalType and the authorisation code is 'CSE'" should {
        implicit val request = withRequest(additionalType, withDeclarationHolders(Some("CSE")))
        val view = createView

        "display the expected page title" in {
          val title = view.getElementsByTag("h1").text
          title mustBe messages(s"$prefix.title.v2")
        }

        "display the expected body" in {
          val paragraphs = view.getElementsByClass("govuk-body")
          paragraphs.size mustBe 4

          paragraphs.get(0).text mustBe messages(s"$prefix.body.v2.1")

          val paragraph1 = paragraphs.get(1)
          paragraph1.text mustBe messages(s"$prefix.body.v2.2", messages(s"$prefix.body.v2.2.link"))
          paragraph1.child(0) must haveHref(appConfig.locationCodesForCsePremises)

          val paragraph2 = paragraphs.get(2)

          val email = messages(s"$prefix.body.v2.3.email")
          val subject = messages(s"$prefix.body.v2.3.subject")

          paragraph2.text mustBe messages(s"$prefix.body.v2.3", email)
          paragraph2.child(0) must haveHref(s"mailto:$email?subject=$subject")
        }

        "display the expected hint" in {
          val hint = view.getElementById("code-hint")
          assert(hint.hasClass("govuk-hint"))
          hint.text mustBe messages(s"$prefix.hint.v2")
        }

        "NOT display the 'Find the goods location code' expander " in {
          val expander = view.getElementsByClass("govuk-details").first.children
          expander.size mustBe 2
          val key = if (request.isType(CLEARANCE)) "clearance" else "common"
          expander.first.text mustBe messages(s"tariff.expander.title.$key")
        }
      }
    }

    // V3 Content
    List(STANDARD_FRONTIER, SIMPLIFIED_FRONTIER, OCCASIONAL_FRONTIER, CLEARANCE_FRONTIER).foreach { additionalType =>
      s"AdditionalDeclarationType is $additionalType and the authorisation code is 'EXRR'" should {
        implicit val request = withRequest(additionalType, withDeclarationHolders(Some("EXRR")))
        val view = createView

        "display the expected page title" in {
          val title = view.getElementsByTag("h1").text
          title mustBe messages(s"$prefix.title.v3")
        }

        "display the expected body" in {
          val paragraphs = view.getElementsByClass("govuk-body")
          paragraphs.size mustBe 4

          paragraphs.get(0).text mustBe messages(s"$prefix.body.v3.1")

          val bulletPoints = view.getElementsByClass("govuk-list--bullet").first.children
          bulletPoints.size mustBe 8
          for (ix <- 1 to 8)
            bulletPoints.get(ix - 1).text mustBe messages(s"$prefix.body.v3.bullet$ix")
        }

        "display the expected inset text" in {
          val insetText = view.getElementsByClass("govuk-inset-text")
          insetText.size mustBe 1

          val paragraphs = insetText.get(0).getElementsByClass("govuk-body")
          paragraphs.size mustBe 2

          val paragraph1 = paragraphs.get(0)
          paragraph1.text mustBe messages(s"$prefix.inset.v3.body1", messages(s"$prefix.inset.v3.body1.link"))
          paragraph1.child(0) must haveHref(appConfig.getGoodsMovementReference)

          val paragraph2 = paragraphs.get(1)
          paragraph2.text mustBe messages(s"$prefix.inset.v3.body2", messages(s"$prefix.inset.v3.body2.link"))
          paragraph2.child(0) must haveHref(appConfig.guidance.january2022locations)
        }

        "display the expected hint" in {
          val hint = view.getElementById("code-hint")
          assert(hint.hasClass("govuk-hint"))
          hint.text mustBe messages(s"$prefix.hint.v3")
        }

        "NOT display the 'Find the goods location code' expander " in {
          val expander = view.getElementsByClass("govuk-details").first.children
          expander.size mustBe 2
          val key = if (request.isType(CLEARANCE)) "clearance" else "common"
          expander.first.text mustBe messages(s"tariff.expander.title.$key")
        }
      }
    }

    // V4 Content
    List(STANDARD_PRE_LODGED, SIMPLIFIED_PRE_LODGED, OCCASIONAL_PRE_LODGED, CLEARANCE_PRE_LODGED).foreach { additionalType =>
      List(Choice1007, ChoiceOthers).foreach { authProcedureCode =>
        s"AdditionalDeclarationType is $additionalType and the authorisation procedure code is '$authProcedureCode'" should {
          implicit val request = withRequest(additionalType, withAuthorisationProcedureCodeChoice(authProcedureCode))
          val view = createView

          "display the expected page title" in {
            val title = view.getElementsByTag("h1").text
            title mustBe messages(s"$prefix.title.v4")
          }

          "display the expected body" in {
            val paragraphs = view.getElementsByClass("govuk-body")
            paragraphs.size mustBe 5

            paragraphs.get(0).text mustBe messages(s"$prefix.body.v4.1")

            val paragraph2 = paragraphs.get(1)
            paragraph2.text mustBe messages(s"$prefix.body.v4.2", messages(s"$prefix.body.v4.2.link"))
            paragraph2.child(0) must haveHref(appConfig.previousProcedureCodes)

            val label = paragraph2.nextElementSibling
            assert(label.hasClass("govuk-heading-s"))
            label.text mustBe messages(s"$prefix.body.v4.3.label")

            val paragraph3 = paragraphs.get(2)
            paragraph3.text mustBe messages(s"$prefix.body.v4.3", messages(s"$prefix.body.v4.3.link"))
            paragraph3.child(0) must haveHref(appConfig.locationCodesForPortsUsingGVMS)
          }

          "display the expected hint" in {
            val hint = view.getElementById("code-hint")
            assert(hint.hasClass("govuk-hint"))
            hint.text mustBe messages(s"$prefix.hint.v4")
          }

          "display the 'Find the goods location code' expander " in {
            verifyExpander(view, 4)
          }
        }
      }
    }

    def verifyExpander(view: Document, version: Int): Unit = {
      val expander = view.getElementsByClass("govuk-details").first
      val expectedTextOfPreviousSibling = messages(s"$prefix.${if (version == 1) "body.v1.3" else "hint.v4"}")
      expander.previousElementSibling.text mustBe expectedTextOfPreviousSibling

      val children = expander.children
      children.size mustBe 2
      children.first.text mustBe messages(s"$prefix.expander.title")

      val details = children.last

      val sections = details.children
      sections.size mustBe 20
      sections.first.text mustBe messages(s"$prefix.expander.v$version.intro")

      val iterator: Iterator[Element] = sections.iterator.asScala.drop(1)

      for (ix <- 1 to 9) {
        val title = iterator.next
        title.tagName mustBe "h2"
        title.text mustBe messages(s"$prefix.expander.paragraph$ix.title")
        assert(title.hasClass("govuk-heading-s"))

        if (ix < 9) {
          val link1 = iterator.next.children.get(0)
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
      behave like viewWithCorrectBackButton(request.declarationType, routes.RoutingCountriesController.displayRoutingCountry())
    }

    onJourney(SUPPLEMENTARY, CLEARANCE) { request =>
      behave like viewWithCorrectBackButton(request.declarationType, routes.DestinationCountryController.displayPage())
    }

    def viewWithCorrectBackButton(declarationType: DeclarationType, redirect: Call): Unit =
      "have correct back-link" when {
        "display 'Back' button that links to correct page" in {
          val view = page(Mode.Normal, form)(journeyRequest(declarationType), messages)

          val backButton = view.getElementById("back-link")

          backButton must containMessage("site.back")
          backButton.getElementById("back-link") must haveHref(redirect)
        }
      }
  }

  private def verifyError(code: String, errorKey: String = "error"): Assertion = {
    val view: Document = page(Mode.Normal, form.fillAndValidate(LocationOfGoods(code)))(journeyRequest(STANDARD), messages)

    view must haveGovukGlobalErrorSummary
    view must containErrorElementWithTagAndHref("a", "#code")

    view must containErrorElementWithMessageKey(s"declaration.locationOfGoods.code.$errorKey")
  }
}
