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
import config.AppConfig
import views.declaration.spec.UnitViewSpec
import views.html.guidance.entry
import views.tags.ViewTest

@ViewTest
class EntryViewSpec extends UnitViewSpec with Stubs with Injector {

  private val guidance = instanceOf[AppConfig].guidance

  private val entryPage = instanceOf[entry]
  private val view = entryPage()(request, messages)

  "'Entry' view" should {

    "display page header" in {
      view.getElementsByTag("h1").first must containMessage("guidance.entry.title")
    }

    "display all expected links" in {
      val links = view.getElementsByClass("govuk-link--no-visited-state")
      links.size mustBe 7

      links.get(0) must haveHref(guidance.someoneToDealWithCustomsOnYourBehalf)
      links.get(1) must haveHref(guidance.takingCommercialGoodsOnYourPerson)
      links.get(2) must haveHref(guidance.exportingByPost)
      links.get(3) must haveHref(controllers.routes.GuidanceController.sendByRoro.url)
      links.get(4) must haveHref(controllers.routes.GuidanceController.start.url)
      links.get(5) must haveHref(guidance.cdsDeclarationSoftware)
      links.get(6) must haveHref(guidance.addATeamMember)
    }

    "display all expected bullet lists" in {
      val bulletLists = view.getElementsByClass("govuk-list--bullet")
      bulletLists.size mustBe 2

      bulletLists.get(0).children.size mustBe 2
      bulletLists.get(1).children.size mustBe 2
    }
  }
}
