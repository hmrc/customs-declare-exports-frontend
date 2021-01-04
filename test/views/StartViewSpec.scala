/*
 * Copyright 2021 HM Revenue & Customs
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

import base.Injector
import play.twirl.api.Html
import views.declaration.spec.UnitViewSpec
import views.html.start_page
import views.tags.ViewTest

@ViewTest
class StartViewSpec extends UnitViewSpec with Injector {

  private val startPage = instanceOf[start_page]
  private def createView(): Html = startPage()

  "Start Page view" should {

    "have proper messages for labels" in {

      messages must haveTranslationFor("startPage.title.sectionHeader")
      messages must haveTranslationFor("startPage.title")
      messages must haveTranslationFor("startPage.description")
      messages must haveTranslationFor("startPage.contents.header")
      messages must haveTranslationFor("startPage.useThisServiceTo.header")
      messages must haveTranslationFor("startPage.useThisServiceTo.listItem.1")
      messages must haveTranslationFor("startPage.useThisServiceTo.listItem.2")
      messages must haveTranslationFor("startPage.useThisServiceTo.listItem.3")
      messages must haveTranslationFor("startPage.useThisServiceTo.listItem.4")
      messages must haveTranslationFor("startPage.beforeYouStart.header")
      messages must haveTranslationFor("startPage.beforeYouStart.line.1")
      messages must haveTranslationFor("startPage.beforeYouStart.line.1.link")
      messages must haveTranslationFor("startPage.informationYouNeed.header")
      messages must haveTranslationFor("startPage.informationYouNeed.line.1")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.1")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.2")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.3")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.3.link")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.4")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.4.link")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.5")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.6")
      messages must haveTranslationFor("startPage.makeDeclaration.header")
      messages must haveTranslationFor("startPage.problemsWithServiceNotice")
      messages must haveTranslationFor("startPage.problemsWithServiceNotice.link")
      messages must haveTranslationFor("startPage.buttonName")
      messages must haveTranslationFor("startPage.afterDeclaringGoods.header")
      messages must haveTranslationFor("startPage.afterDeclaringGoods")
      messages must haveTranslationFor("startPage.afterDeclaringGoods.link")
      messages must haveTranslationFor("startPage.hmrcUrl")
      messages must haveTranslationFor("startPage.from")
      messages must haveTranslationFor("startPage.information")
      messages must haveTranslationFor("startPage.exploreTheTopic")
      messages must haveTranslationFor("startPage.importExport")
    }

    val view = createView()

    "display title" in {

      view.select("title").text() must include(messages("startPage.title"))
    }

    "display section header" in {

      view.getElementById("section-header") must containMessage("startPage.title.sectionHeader")
    }

    "display header" in {

      view.getElementById("title") must containMessage("startPage.title")
    }

    "display general description" in {

      view.getElementById("description") must containMessage("startPage.description")
    }

    "display Contents section" in {

      view.getElementsByClass("dashed-list-title").get(0) must containMessage("startPage.contents.header")

      val contentList = view.getElementsByClass("dashed-list").get(0)

      contentList must haveChildCount(4)
      contentList.child(0) must containMessage("startPage.beforeYouStart.header")
      contentList.child(1) must containMessage("startPage.informationYouNeed.header")
      contentList.child(2) must containMessage("startPage.makeDeclaration.header")
      contentList.child(3) must containMessage("startPage.afterDeclaringGoods.header")
    }

    "contain links in Contents section" in {

      val contentList = view.getElementsByClass("dashed-list").get(0)

      contentList.child(0).child(0) must haveHref("#before-you-start")
      contentList.child(1).child(0) must haveHref("#information-you-need")
      contentList.child(2).child(0) must haveHref("#make-declaration")
      contentList.child(3).child(0) must haveHref("#after-declaring-goods")
    }

    "display 'Use this service to' section" in {

      view.getElementsByClass("govuk-body").get(0) must containMessage("startPage.useThisServiceTo.header")

      val useThisServiceList = view.getElementsByClass("govuk-list--bullet").get(0)

      useThisServiceList must haveChildCount(4)
      useThisServiceList.child(0) must containMessage("startPage.useThisServiceTo.listItem.1")
      useThisServiceList.child(1) must containMessage("startPage.useThisServiceTo.listItem.2")
      useThisServiceList.child(2) must containMessage("startPage.useThisServiceTo.listItem.3")
      useThisServiceList.child(3) must containMessage("startPage.useThisServiceTo.listItem.4")
    }

    "display 'Before you start' section" in {

      view.getElementById("before-you-start") must containMessage("startPage.beforeYouStart.header")

      view.getElementById("before-you-start-element-1").text() must include(messages("startPage.beforeYouStart.line.1", "EORI"))
    }

    "display 'Information you need' section" in {

      view.getElementById("information-you-need") must containMessage("startPage.informationYouNeed.header")

      view.getElementsByClass("govuk-body").get(2) must containMessage("startPage.informationYouNeed.line.1")

      val informationYouNeedList = view.getElementsByClass("govuk-list--bullet").get(1)

      informationYouNeedList must haveChildCount(6)
      informationYouNeedList.child(0) must containMessage("startPage.informationYouNeed.listItem.1")
      informationYouNeedList.child(1) must containMessage("startPage.informationYouNeed.listItem.2")
      informationYouNeedList.child(2).text() must include(messages("startPage.informationYouNeed.listItem.3", "commodity codes"))
      informationYouNeedList.child(3).text() must include(messages("startPage.informationYouNeed.listItem.4", ""))
      informationYouNeedList.child(4) must containMessage("startPage.informationYouNeed.listItem.5")
      informationYouNeedList.child(5) must containMessage("startPage.informationYouNeed.listItem.6")
    }

    "display 'Make a declaration' section" in {

      view.getElementById("make-declaration") must containMessage("startPage.makeDeclaration.header")
    }

    "display problems with service notice" in {

      view.getElementsByClass("govuk-inset-text").get(1) must containMessage("startPage.problemsWithServiceNotice", "problems with this service ")
    }

    "display 'Start now' button" in {

      view.getElementsByClass("govuk-button").first() must containMessage("startPage.buttonName")
      view.getElementsByClass("govuk-button").first() must haveHref(controllers.routes.ChoiceController.displayPage())
    }

    "display 'After you’ve declared your goods' section" in {

      view.getElementById("after-declaring-goods") must containMessage("startPage.afterDeclaringGoods.header")

      view.getElementById("after-declaring-goods-element") must containMessage(
        "startPage.afterDeclaringGoods",
        "tell HMRC when your goods arrive at the UK port and leave the UK "
      )
    }

    "display link to go back to Contents section" in {

      view.getElementById("back-to-top") must containMessage("startPage.contents.header")
      view.getElementById("back-to-top").child(0) must haveHref("#contents")
    }

    "display explore the topic section" in {

      view.getElementById("explore-the-topic") must containMessage("startPage.exploreTheTopic")
      view.getElementsByClass("govuk-link govuk-!-font-size-16").first() must containMessage("startPage.importExport")
    }
  }
}
