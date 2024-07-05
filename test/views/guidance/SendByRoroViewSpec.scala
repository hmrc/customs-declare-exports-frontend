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

package views.guidance

import base.Injector
import tools.Stubs
import views.html.guidance.send_by_roro
import views.tags.ViewTest
import config.AppConfig
import views.common.UnitViewSpec

import scala.jdk.CollectionConverters.CollectionHasAsScala

@ViewTest
class SendByRoroViewSpec extends UnitViewSpec with Stubs with Injector {

  private val appConfig = instanceOf[AppConfig]
  private val sendByRoroPage = instanceOf[send_by_roro]
  private val view = sendByRoroPage()(request, messages)

  "'Send By Roro' view" should {

    "display page header" in {
      view.getElementsByTag("h1").first must containMessage("guidance.roro.title")
    }

    "display the expected sections" in {
      val section1 = view.getElementsByClass("govuk-heading-l")
      section1.size mustBe 1
      section1.text mustBe messages(s"guidance.roro.section.1.header")

      val otherSections = view.getElementsByClass("govuk-heading-m")
      otherSections.size mustBe 5

      otherSections.eachText.asScala.zipWithIndex.foreach { section =>
        val text = section._1
        val ix = section._2 + 2
        text mustBe messages(s"guidance.roro.section.${ix}.header")
      }
    }

    "display the expected paragraphs" in {
      val paragraphs = view.getElementsByClass("govuk-body")
      paragraphs.size mustBe 13
    }

    "display the expected bullet list" in {
      val bulletLists = view.getElementsByClass("govuk-list--bullet")
      bulletLists.size mustBe 4

      bulletLists.get(0).children.size mustBe 2
      bulletLists.get(1).children.size mustBe 10
      bulletLists.get(2).children.size mustBe 9
      bulletLists.get(3).children.size mustBe 4
    }

    "display the expected numbered list" in {
      val numberedList = view.getElementsByClass("govuk-list--number")
      numberedList.size mustBe 1

      numberedList.get(0).children.size mustBe 8

      val link = numberedList.get(0).getElementsByClass("govuk-link").first
      link must haveHref(appConfig.guidance.gvms)
    }
  }
}
