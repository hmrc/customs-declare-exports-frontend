/*
 * Copyright 2024 HM Revenue & Customs
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

package views.section5

import base.Injector
import config.AppConfig
import controllers.section5.routes.NactCodeSummaryController
import forms.section5.StatisticalValue
import forms.section5.StatisticalValue.form
import models.DeclarationType.STANDARD
import org.jsoup.nodes.Document
import play.api.data.Form
import views.common.PageWithButtonsSpec
import views.html.section5.statistical_value
import views.tags.ViewTest

import scala.jdk.CollectionConverters.IteratorHasAsScala

@ViewTest
class StatisticalValueViewSpec extends PageWithButtonsSpec with Injector {

  private val appConfig = instanceOf[AppConfig]

  private val page = instanceOf[statistical_value]

  override val typeAndViewInstance = (STANDARD, page(itemId, form)(_, _))

  def createView(frm: Form[StatisticalValue] = form): Document =
    page(itemId, frm)(journeyRequest(), messages)

  "'Statistical Value' View on empty page" should {
    val view: Document = createView()

    "display same page title as header" in {
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "display 'Back' button that links to 'TARIC Codes' page" in {
      val backButton = view.getElementById("back-link")
      backButton must containMessage("site.backToPreviousQuestion")
      backButton.getElementById("back-link") must haveHref(NactCodeSummaryController.displayPage(itemId))
    }

    "display the expected notification banner" in {
      val banner = view.getElementsByClass("govuk-notification-banner").get(0)

      val title = banner.getElementsByClass("govuk-notification-banner__title").text
      title mustBe messages("declaration.statisticalValue.notification.title")

      val content = banner.getElementsByClass("govuk-notification-banner__content").get(0)
      content.text mustBe messages("declaration.statisticalValue.notification.content")
    }

    "display section header" in {
      view.getElementById("section-header").text mustBe messages("declaration.section.5")
    }

    "display page title" in {
      view.getElementsByTag("h1").get(0).text mustBe messages("declaration.statisticalValue.title")
    }

    "display the expected label" in {
      view.getElementsByClass("govuk-label").get(0).text mustBe messages("declaration.statisticalValue.label")
    }

    "display the expected inset text" in {
      view.getElementsByClass("govuk-inset-text").get(0).text mustBe messages("declaration.statisticalValue.inset.text")
    }

    "display empty input for Statistical Value" in {
      view.getElementById("statisticalValue").attr("value") mustBe empty
    }

    "display the expected text section" in {
      val title = view.getElementsByClass("govuk-heading-s").get(0)
      title.tagName mustBe "h2"
      title.text mustBe messages("declaration.statisticalValue.section.header")

      val paragraphs = view.getElementsByClass("govuk-body")

      paragraphs.get(1).text mustBe messages("declaration.statisticalValue.section.text.1")

      val text2 = messages("declaration.statisticalValue.section.text.2", messages("declaration.statisticalValue.section.text.2.link"))
      paragraphs.get(2).text mustBe text2
      paragraphs.get(2).child(0) must haveHref(appConfig.hmrcExchangeRatesFor2021)
    }

    "display the expected guidance expander" in {
      val expander = view.getElementsByClass("govuk-details").first
      expander.tagName mustBe "details"

      expander.getElementsByClass("govuk-details__summary").text mustBe messages("declaration.statisticalValue.guidance.title")
      expander.getElementsByClass("govuk-body").iterator.asScala.toList.zipWithIndex.foreach { case (element, index) =>
        element.text mustBe messages(s"declaration.statisticalValue.guidance.text.${index + 1}")
      }
    }

    "display the expected tariff details" in {
      val expander = view.getElementsByClass("govuk-details").last
      expander.tagName mustBe "details"

      val tariffTitle = expander.getElementsByClass("govuk-details__summary-text")
      tariffTitle.text mustBe messages(s"tariff.expander.title.common")

      val tariffDetails = expander.getElementsByClass("govuk-details__text").first

      val expectedText =
        messages("tariff.declaration.item.statisticalValue.common.text", messages("tariff.declaration.item.statisticalValue.common.linkText.0"))
      val actualText = removeBlanksIfAnyBeforeDot(tariffDetails.text)
      actualText mustBe removeLineBreakIfAny(expectedText)

      tariffDetails.child(0) must haveHref(appConfig.tariffGuideUrl("urls.tariff.declaration.item.statisticalValue.common.0"))
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }

  "'Statistical Value' View with entered data" should {
    "display data in Statistical Value input" in {
      val itemType = StatisticalValue("12345")
      val view = createView(form.fill(itemType))

      assertViewDataEntered(view, itemType)
    }
  }

  def assertViewDataEntered(view: Document, itemType: StatisticalValue): Unit =
    view.getElementById("statisticalValue").attr("value") must equal(itemType.statisticalValue)
}
