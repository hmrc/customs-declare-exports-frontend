/*
 * Copyright 2019 HM Revenue & Customs
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

package views

import config.AppConfig
import org.mockito.Mockito.when
import play.twirl.api.Html
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.start_page
import views.tags.ViewTest

@ViewTest
class StartViewSpec extends UnitViewSpec with Stubs {

  private val appConfig = mock[AppConfig]
  when(appConfig.customsDeclarationsGoodsTakenOutOfEuUrl)
    .thenReturn("appConfig.customsDeclarationsGoodsTakenOutOfEuUrl")
  when(appConfig.commodityCodesUrl).thenReturn("appConfig.commodityCodesUrl")
  when(appConfig.relevantLicensesUrl).thenReturn("appConfig.relevantLicensesUrl")
  when(appConfig.serviceAvailabilityUrl).thenReturn("appConfig.serviceAvailabilityUrl")
  when(appConfig.customsMovementsFrontendUrl).thenReturn("appConfig.customsMovementsFrontendUrl")

  private val startPage = new start_page(mainTemplate, appConfig)
  private def createView(): Html = startPage()

  "Start Page view" should {

    "have proper messages for labels" in {
      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("startPage.title.sectionHeader")
      messages must haveTranslationFor("startPage.title")
      messages must haveTranslationFor("startPage.description")
      messages must haveTranslationFor("startPage.contents.header")
      messages must haveTranslationFor("startPage.useThisServiceTo.header")
      messages must haveTranslationFor("startPage.useThisServiceTo.listItem.1")
      messages must haveTranslationFor("startPage.useThisServiceTo.listItem.2")
      messages must haveTranslationFor("startPage.useThisServiceTo.listItem.3")
      messages must haveTranslationFor("startPage.useThisServiceTo.listItem.4")
      messages must haveTranslationFor("startPage.useThisServiceTo.notice")
      messages must haveTranslationFor("startPage.useThisServiceTo.notice.link")
      messages must haveTranslationFor("startPage.beforeYouStart.header")
      messages must haveTranslationFor("startPage.beforeYouStart.line.1")
      messages must haveTranslationFor("startPage.beforeYouStart.line.2")
      messages must haveTranslationFor("startPage.informationYouNeed.header")
      messages must haveTranslationFor("startPage.informationYouNeed.line.1")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.1")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.2")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.3.1")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.3.link")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.3.2")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.4")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.4.link")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.5")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.6")
      messages must haveTranslationFor("startPage.makeDeclaration.header")
      messages must haveTranslationFor("startPage.problemsWithServiceNotice")
      messages must haveTranslationFor("startPage.problemsWithServiceNotice.link")
      messages must haveTranslationFor("startPage.buttonName")
      messages must haveTranslationFor("startPage.afterDeclaringGoods.header")
      messages must haveTranslationFor("startPage.afterDeclaringGoods.line.1")
      messages must haveTranslationFor("startPage.afterDeclaringGoods.line.1.link")
    }

    val view = createView()

    "display title" in {
      view.select("title").text() mustBe "title.format"
    }

    "display section header" in {
      view.getElementById("section-header").text() mustBe "startPage.title.sectionHeader"
    }

    "display header" in {
      view.getElementById("title").text() mustBe "startPage.title"
    }

    "display general description" in {
      view.getElementById("description").text() mustBe "startPage.description"
    }

    "display Contents section" in {
      view.getElementById("contents").text() mustBe "startPage.contents.header"

      view.getElementById("contents-list") must haveChildCount(4)
      view.getElementById("contents-list").child(0).text() mustBe "startPage.beforeYouStart.header"
      view.getElementById("contents-list").child(1).text() mustBe "startPage.informationYouNeed.header"
      view.getElementById("contents-list").child(2).text() mustBe "startPage.makeDeclaration.header"
      view.getElementById("contents-list").child(3).text() mustBe "startPage.afterDeclaringGoods.header"
    }

    "contain links in Contents section" in {
      view.getElementById("contents-list").child(0).child(0) must haveHref("#before-you-start")
      view.getElementById("contents-list").child(1).child(0) must haveHref("#information-you-need")
      view.getElementById("contents-list").child(2).child(0) must haveHref("#make-declaration")
      view.getElementById("contents-list").child(3).child(0) must haveHref("#after-declaring-goods")
    }

    "display 'Use this service to' section" in {
      view.getElementById("use-this-service-to").text() mustBe "startPage.useThisServiceTo.header"

      view.getElementById("use-this-service-to-list") must haveChildCount(4)
      view.getElementById("use-this-service-to-list").child(0).text() mustBe "startPage.useThisServiceTo.listItem.1"
      view.getElementById("use-this-service-to-list").child(1).text() mustBe "startPage.useThisServiceTo.listItem.2"
      view.getElementById("use-this-service-to-list").child(2).text() mustBe "startPage.useThisServiceTo.listItem.3"
      view.getElementById("use-this-service-to-list").child(3).text() mustBe "startPage.useThisServiceTo.listItem.4"
    }

    "display notice in 'Use this service to' section" in {
      view.getElementById("use-this-service-to-notice").text() must include("startPage.useThisServiceTo.notice")
      view.getElementById("use-this-service-to-notice").text() must include("startPage.useThisServiceTo.notice.link")
    }

    "contain link to Customs Declarations Guidance in 'Use this service to' notice" in {
      view.getElementById("use-this-service-to-notice").child(0) must haveHref(
        appConfig.customsDeclarationsGoodsTakenOutOfEuUrl
      )
    }

    "display 'Before you start' section" in {
      view.getElementById("before-you-start").text() mustBe "startPage.beforeYouStart.header"

      view.getElementById("before-you-start-element-1").text() mustBe "startPage.beforeYouStart.line.1"
      view.getElementById("before-you-start-element-2").text() mustBe "startPage.beforeYouStart.line.2"
    }

    "display 'Information you need' section" in {
      view.getElementById("information-you-need").text() mustBe "startPage.informationYouNeed.header"

      view.getElementById("information-you-need-element-1").text() mustBe "startPage.informationYouNeed.line.1"

      view.getElementById("information-you-need-list") must haveChildCount(6)
      view.getElementById("information-you-need-list").child(0).text() mustBe "startPage.informationYouNeed.listItem.1"
      view.getElementById("information-you-need-list").child(1).text() mustBe "startPage.informationYouNeed.listItem.2"
      view.getElementById("information-you-need-list").child(2).text() must include(
        "startPage.informationYouNeed.listItem.3.1"
      )
      view.getElementById("information-you-need-list").child(2).text() must include(
        "startPage.informationYouNeed.listItem.3.link"
      )
      view.getElementById("information-you-need-list").child(2).text() must include(
        "startPage.informationYouNeed.listItem.3.2"
      )
      view.getElementById("information-you-need-list").child(3).text() must include(
        "startPage.informationYouNeed.listItem.4"
      )
      view.getElementById("information-you-need-list").child(3).text() must include(
        "startPage.informationYouNeed.listItem.4.link"
      )
      view.getElementById("information-you-need-list").child(4).text() mustBe "startPage.informationYouNeed.listItem.5"
      view.getElementById("information-you-need-list").child(5).text() mustBe "startPage.informationYouNeed.listItem.6"
    }

    "contain link to Commodity Codes in 'Information you need' section" in {
      view.getElementById("information-you-need-list").child(2).child(0) must haveHref(appConfig.commodityCodesUrl)
    }

    "contain link to 'Relevant licenses' in 'Information you need' section" in {
      view.getElementById("information-you-need-list").child(3).child(0) must haveHref(appConfig.relevantLicensesUrl)
    }

    "display 'Make a declaration' section" in {
      view.getElementById("make-declaration").text() mustBe "startPage.makeDeclaration.header"
    }

    "display problems with service notice" in {
      view.getElementById("problems-with-service-notice").text() must include("startPage.problemsWithServiceNotice")
      view.getElementById("problems-with-service-notice").text() must include(
        "startPage.problemsWithServiceNotice.link"
      )
    }

    "contain link to service availability in 'Report your arrival and departure' section" in {
      view.getElementById("problems-with-service-notice").child(0) must haveHref(appConfig.serviceAvailabilityUrl)
    }

    "display 'Start now' button" in {
      view.getElementById("button-start").text() mustBe "startPage.buttonName"
      view.getElementById("button-start") must haveHref(controllers.routes.ChoiceController.displayPage())
    }

    "display 'After you’ve declared your goods' section" in {
      view.getElementById("after-declaring-goods").text() mustBe "startPage.afterDeclaringGoods.header"

      view.getElementById("after-declaring-goods-element-1").text() must include("startPage.afterDeclaringGoods.line.1")
      view.getElementById("after-declaring-goods-element-1").text() must include(
        "startPage.afterDeclaringGoods.line.1.link"
      )
    }

    "contain link to Customs Movements Service in 'After you’ve declared your goods' section" in {
      view.getElementById("after-declaring-goods-element-1").child(0) must haveHref(
        appConfig.customsMovementsFrontendUrl
      )
    }

    "display link to go back to Contents section" in {
      view.getElementById("back-to-top").text() mustBe "startPage.contents.header"
      view.getElementById("back-to-top").child(0) must haveHref("#contents")
    }
  }

}
