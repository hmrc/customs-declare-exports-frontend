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

package views.general

import base.Injector
import views.common.UnitViewSpec
import views.html.general.unauthorisedAgent
import views.tags.ViewTest

@ViewTest
class UnauthorisedAgentViewSpec extends UnitViewSpec with Injector {

  val page = instanceOf[unauthorisedAgent]

  val view = page(displaySignOut = false)(request, messages)

  "UnauthorisedAgent Page view" should {

    "display the expected page header" in {
      view.getElementsByTag("h1").first must containMessage("unauthorisedAgent.heading")
    }

    "display the expected paragraphs" in {
      val paragraphs = view.getElementsByClass("govuk-body")

      paragraphs.get(0) must containMessage("unauthorisedAgent.paragraph.1")
      paragraphs.get(1) must containMessage("unauthorisedAgent.paragraph.2")
    }
  }
}
