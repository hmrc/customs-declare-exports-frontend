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

import base.{Injector, TestHelper}
import config.AppConfig
import connectors.CodeListConnector
import controllers.declaration.routes.{DestinationCountryController, RoutingCountriesController}
import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1007, ChoiceOthers}
import forms.declaration.LocationOfGoods
import forms.declaration.LocationOfGoods.form
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.authorisationHolder.AuthorizationTypeCodes.{CSE, EXRR, MIB}
import models.DeclarationType._
import models.codes.{Country, GoodsLocationCode}
import models.requests.JourneyRequest
import org.jsoup.nodes.{Document, Element}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.Assertion
import play.api.mvc.Call
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.location_of_goods
import views.tags.ViewTest

import scala.collection.immutable.ListMap
import scala.jdk.CollectionConverters.IteratorHasAsScala

@ViewTest
class LocationOfGoodsViewSpec extends PageWithButtonsSpec with Injector {

  private val appConfig = instanceOf[AppConfig]

  implicit val mockCodeListConnector = mock[CodeListConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom", "GB")))
    when(mockCodeListConnector.allGoodsLocationCodes(any())).thenReturn(ListMap[String, GoodsLocationCode]())
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  val page = instanceOf[location_of_goods]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  "Goods Location View" when {
    def createView()(implicit request: JourneyRequest[_]): Document = page(form)

    val prefix = "declaration.locationOfGoods"

    arrivedTypes.foreach { additionalType =>
      s"AdditionalDeclarationType is $additionalType with CSE" should {

        implicit val request = withRequest(additionalType, withAuthorisationHolders(Some(CSE)))

        val view = createView()

        "display same page title as header" in {
          view.title must include(view.getElementsByTag("h1").text)
        }

        "display section header" in {
          view.getElementById("section-header") must containMessage("declaration.section.3")
        }

        "display the expected page title" in {
          val title = view.getElementsByTag("h1").text
          title mustBe messages(s"$prefix.title.v7")
        }

        "display the expected body" in {
          view.getElementsByClass("govuk-body").get(1).text mustBe messages(s"$prefix.body.v7.1")
        }

        "display the expected hint" in {
          val hint = view.getElementsByClass("govuk-hint")
          hint.first.text mustBe messages(s"$prefix.yesNo.yes.hint")
        }

        "display the expected tariff details" in {
          val key = "common"

          val tariffTitle = view.getElementsByClass("govuk-details__summary-text").last
          tariffTitle.text mustBe messages(s"tariff.expander.title.$key")

          val tariffDetails = view.getElementsByClass("govuk-details__text").last.text

          val keyPrefix = s"tariff.declaration.locationOfGoods.$key"
          val expectedText = removeLineBreakIfAny(messages(s"tariff.declaration.text", messages(s"$keyPrefix.linkText.0")))
          removeBlanksIfAnyBeforeDot(tariffDetails) mustBe expectedText
        }

        checkAllSaveButtonsAreDisplayed(createView())
      }
    }

    arrivedTypes.foreach { additionalType =>
      s"AdditionalDeclarationType is $additionalType" should {

        implicit val request = withRequest(additionalType, withAuthorisationHolders(Some(MIB)))

        val view = createView()

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

          paragraphs.get(1).text mustBe messages(s"$prefix.body.v1.1")
          paragraphs.get(2).text mustBe messages(s"$prefix.body.v1.1.1")
          paragraphs.get(3).text mustBe messages(s"$prefix.body.v1.2")
          paragraphs.get(4).text mustBe messages(s"$prefix.body.v1.3")
        }

        "display the 'Find the goods location code' expander " in {
          verifyExpander(view, 1)
        }

        "display the expected hint" in {
          val hint = view.getElementsByClass("govuk-hint")
          hint.get(2).text mustBe messages(s"$prefix.yesNo.yes.hint")
        }

        "display the expected tariff details" in {
          val key = "common"

          val tariffTitle = view.getElementsByClass("govuk-details__summary-text").last
          tariffTitle.text mustBe messages(s"tariff.expander.title.$key")

          val tariffDetails = view.getElementsByClass("govuk-details__text").last.text

          val keyPrefix = s"tariff.declaration.locationOfGoods.$key"
          val expectedText = removeLineBreakIfAny(messages("tariff.declaration.text", messages(s"$keyPrefix.linkText.0")))
          removeBlanksIfAnyBeforeDot(tariffDetails) mustBe expectedText
        }

        checkAllSaveButtonsAreDisplayed(createView())
      }
    }

    arrivedTypes.foreach { additionalType =>
      List("AEOC", EXRR).foreach { authCode =>
        s"AdditionalDeclarationType is $additionalType and the authorisation code is '$authCode'" should {
          val version = if (authCode == "AEOC") 5 else 3
          implicit val request = withRequest(additionalType, withAuthorisationHolders(Some(authCode)))
          val view = createView()

          "display the expected page title" in {
            val title = view.getElementsByTag("h1").text
            title mustBe messages(s"$prefix.title.v$version")
          }

          "display the expected body" in {
            view.getElementsByClass("govuk-body").get(1).text mustBe messages(s"$prefix.body.v$version.1")

            val bulletPoints = view.getElementsByClass("govuk-list--bullet").first.children
            bulletPoints.size mustBe 8

            bulletPoints.get(0).text mustBe messages(s"$prefix.body.v3.bullet1")
            bulletPoints.get(1).text mustBe messages(s"$prefix.body.v3.bullet2")
            bulletPoints.get(2).text mustBe messages(s"$prefix.body.v3.bullet8", messages(s"$prefix.body.v3.bullet8.hint"))
            bulletPoints.get(3).text mustBe messages(s"$prefix.body.v3.bullet3")
            bulletPoints.get(4).text mustBe messages(s"$prefix.body.v3.bullet4")
            bulletPoints.get(5).text mustBe messages(s"$prefix.body.v3.bullet5")
            bulletPoints.get(6).text mustBe messages(s"$prefix.body.v3.bullet6")
            bulletPoints.get(7).text mustBe messages(s"$prefix.body.v3.bullet7")
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
            val hint = view.getElementsByClass("govuk-hint")
            hint.get(1).text mustBe messages(s"$prefix.yesNo.yes.hint")
          }

        }
      }
    }

    (preLodgedTypes ++ Seq(SUPPLEMENTARY_EIDR, SUPPLEMENTARY_SIMPLIFIED)).foreach { additionalType =>
      s"AdditionalDeclarationType is $additionalType" should {
        implicit val request = withRequest(additionalType)
        val view = createView()

        "display the expected page title" in {
          val title = view.getElementsByTag("h1").text
          title mustBe messages(s"$prefix.title.v6")
        }

        "display the expected body" in {
          view.getElementsByClass("govuk-body").get(1).text mustBe messages(s"$prefix.body.v6.1")
        }

        "display the expected hint" in {
          val hint = view.getElementsByClass("govuk-hint")
          hint.first().text mustBe messages(s"$prefix.yesNo.yes.hint")
        }

      }
    }

    preLodgedTypes.foreach { additionalType =>
      s"AdditionalDeclarationType is $additionalType" should {

        "display the 'Find the goods location code' expander" when {
          "CHOICE1007" in {

            implicit val request = withRequest(additionalType, withAuthorisationProcedureCodeChoice(Choice1007))
            val view = createView()

            verifyExpander(view, 6)
          }
          "CHOICEOTHERS" in {

            implicit val request = withRequest(additionalType, withAuthorisationProcedureCodeChoice(ChoiceOthers))
            val view = createView()

            verifyExpander(view, 6)
          }
        }

      }
    }

    def verifyExpander(view: Document, version: Int): Unit = {
      val expander = view.getElementsByClass("govuk-details").first

      val children = expander.children
      children.size mustBe 2
      children.first.text mustBe messages(s"$prefix.expander.title")

      val details = children.last

      val sections = details.children
      sections.size mustBe 20

      if (version == 1) sections.first.text mustBe messages(s"$prefix.expander.v1.intro")
      else sections.first.text mustBe messages(s"$prefix.expander.intro")

      val iterator: Iterator[Element] = sections.iterator.asScala.drop(1)

      for (ix <- 1 to 9) {
        val title = iterator.next()
        title.tagName mustBe "h2"
        title.text mustBe messages(s"$prefix.expander.paragraph$ix.title")
        assert(title.hasClass("govuk-heading-s"))

        if (ix < 9) {
          val link1 = iterator.next().children.get(0)
          assert(link1.hasClass("govuk-link"))
          link1.text mustBe messages(s"$prefix.expander.paragraph$ix.link1")
        }
      }

      for (ix <- 9 to 10) {
        val hint = iterator.next()
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
      behave like viewWithCorrectBackButton(request.declarationType, RoutingCountriesController.displayRoutingCountry)
    }

    onJourney(SUPPLEMENTARY, CLEARANCE) { request =>
      behave like viewWithCorrectBackButton(request.declarationType, DestinationCountryController.displayPage)
    }

    onEveryDeclarationJourney() { implicit request =>
      "display the expected notification banner" in {

        val view = page(form)

        val banner = view.getElementsByClass("govuk-notification-banner").get(0)

        val title = banner.getElementsByClass("govuk-notification-banner__title").text
        title mustBe messages("declaration.locationOfGoods.notification.title")

        val content = banner.getElementsByClass("govuk-notification-banner__content").get(0)
        content.text mustBe messages("declaration.locationOfGoods.notification.content")

      }

    }

    def viewWithCorrectBackButton(declarationType: DeclarationType, redirect: Call): Unit =
      "have correct back-link" when {
        "display 'Back' button that links to correct page" in {
          val view = page(form)(journeyRequest(declarationType), messages)

          val backButton = view.getElementById("back-link")

          backButton must containMessage("site.backToPreviousQuestion")
          backButton.getElementById("back-link") must haveHref(redirect)
        }
      }
  }

  private def verifyError(code: String, errorKey: String = "error"): Assertion = {
    val view: Document = page(form.fillAndValidate(LocationOfGoods(code)))(journeyRequest(STANDARD), messages)

    view must haveGovukGlobalErrorSummary
    view must containErrorElementWithTagAndHref("a", "#code")

    view must containErrorElementWithMessageKey(s"declaration.locationOfGoods.code.$errorKey")
  }
}
