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
import config.AppConfig
import views.html.guidance.complete_declaration
import views.common.UnitViewSpec
import views.tags.ViewTest

import scala.jdk.CollectionConverters.CollectionHasAsScala

@ViewTest
class CompleteDeclarationViewSpec extends UnitViewSpec with Injector {

  private val appConfig = instanceOf[AppConfig]
  private val guidance = appConfig.guidance

  private val completeDeclarationPage = instanceOf[complete_declaration]
  private val view = completeDeclarationPage()(request, messages)

  "'Complete Declaration' view" should {

    "display page header" in {
      view.getElementsByTag("h1").first must containMessage("guidance.completeDeclaration.title")
    }

    "display all expected sections" in {
      val sections = view.getElementsByClass("govuk-heading-l")
      sections.size mustBe 5

      sections.eachText.asScala.zipWithIndex.foreach { section =>
        val text = section._1
        val ix = section._2 + 1
        text mustBe messages(s"guidance.completeDeclaration.section.${ix}.header")
      }
    }

    "display all expected section 3's sub-sections" in {
      val subs = view.getElementsByClass("govuk-heading-s")
      subs.size mustBe 6

      subs.eachText.asScala.zipWithIndex.foreach { sub =>
        val text = sub._1
        val ix = sub._2 + 1
        text mustBe messages(s"guidance.completeDeclaration.section.3.sub.${ix}")
      }
    }

    "display all expected links" in {
      val links = view.getElementsByClass("govuk-link--no-visited-state")
      links.size mustBe 14

      links.get(0) must haveHref(guidance.cdsTariffCompletionGuide)
      links.get(1) must haveHref(guidance.commodityCodes)
      links.get(2) must haveHref(guidance.commodityCodes)
      links.get(3) must haveHref(guidance.vatRatingForStandardExport)
      links.get(4) must haveHref(guidance.vatOnGoodsExportedFromUK)
      links.get(5) must haveHref(guidance.commodityCode2208303000)
      links.get(6) must haveHref(guidance.aiCodes)
      links.get(7) must haveHref(guidance.aiCodesForContainers)
      links.get(8) must haveHref(guidance.additionalDocumentsUnionCodes)
      links.get(9) must haveHref(guidance.additionalDocumentsReferenceCodes)
      links.get(10) must haveHref(guidance.commodityCode0306310010)
      links.get(11) must haveHref(guidance.clearingGoodsFromToUK)
      links.get(12) must haveHref(guidance.cdsTariffCompletionGuide)
      links.get(13) must haveHref(guidance.specialProcedures)
    }

    "display all expected bullet lists" in {
      val bulletLists = view.getElementsByClass("govuk-list--bullet")
      bulletLists.size mustBe 4

      bulletLists.get(0).children.size mustBe 7
      bulletLists.get(1).children.size mustBe 4
      bulletLists.get(2).children.size mustBe 3
      bulletLists.get(3).children.size mustBe 2
    }
  }
}
