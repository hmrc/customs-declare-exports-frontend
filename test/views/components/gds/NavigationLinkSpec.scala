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

package views.components.gds

import base.Injector
import controllers.routes.SecureMessagingController.displayInbox
import views.declaration.spec.UnitViewSpec
import views.html.components.gds.navigationLink

class NavigationLinkSpec extends UnitViewSpec with Injector {

  val navigationLink = instanceOf[navigationLink]
  val content = "Some text"

  "navigationLink" should {

    "display a span element" when {
      "notAsLink is true" in {
        val elements = navigationLink(notAsLink = true, displayInbox, content).getElementsByTag("span")
        elements.size mustBe 1
        elements.first.className mustBe "navigation-non-link"
        elements.first.text mustBe content
      }
    }

    "display a link element" when {
      "notAsLink is false" in {
        val elements = navigationLink(notAsLink = false, displayInbox, content).getElementsByTag("a")
        elements.size mustBe 1
        elements.first must haveHref(displayInbox.url)
      }
    }
  }
}
