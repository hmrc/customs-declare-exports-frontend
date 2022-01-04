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

package views.components.gds

import base.Injector
import controllers.routes.{SecureMessagingController, SubmissionsController}
import views.declaration.spec.UnitViewSpec
import views.html.components.gds.navigationBanner

class NavigationBannerSpec extends UnitViewSpec with Injector {

  val navigationBanner = instanceOf[navigationBanner]

  "navigationBanner" should {

    "display the two expected link elements" when {
      "the input parameters are left to their default (true)" in {
        val elements = navigationBanner().getElementsByTag("a")
        elements.size mustBe 2

        elements.first must containMessage("navigation.link.declarations")
        elements.first must haveHref(SubmissionsController.displayListOfSubmissions())

        elements.last must containMessage("navigation.link.messages")
        elements.last must haveHref(SecureMessagingController.displayInbox)
      }
    }

    "display one styled span element and one link element" when {
      "the input parameter 'withLinkToSubmissions' is false" in {
        val view = navigationBanner(withLinkToSubmissions = false)

        val span = view.getElementsByTag("span")
        span.size mustBe 1
        span.first.className mustBe "navigation-non-link"
        span.first must containMessage("navigation.link.declarations")

        val link = view.getElementsByTag("a")
        link.size mustBe 1
        link.first must containMessage("navigation.link.messages")
        link.first must haveHref(SecureMessagingController.displayInbox)
      }
    }

    "display one link element and one styled span element" when {
      "the input parameter 'withLinkToMessageInbox' is false" in {
        val view = navigationBanner(withLinkToMessageInbox = false)

        val link = view.getElementsByTag("a")
        link.size mustBe 1
        link.first must containMessage("navigation.link.declarations")
        link.first must haveHref(SubmissionsController.displayListOfSubmissions())

        val span = view.getElementsByTag("span")
        span.size mustBe 1
        span.first.className mustBe "navigation-non-link"
        span.first must containMessage("navigation.link.messages")
      }
    }
  }
}
