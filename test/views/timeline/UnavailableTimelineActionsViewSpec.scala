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

package views.timeline

import base.{Injector, MockAuthAction}
import controllers.timeline.routes.DeclarationDetailsController
import views.common.UnitViewSpec
import views.html.timeline.unavailable_timeline_actions
import views.tags.ViewTest

@ViewTest
class UnavailableTimelineActionsViewSpec extends UnitViewSpec with Injector with MockAuthAction {

  val page = instanceOf[unavailable_timeline_actions]

  val verifiedRequest = buildVerifiedEmailRequest(request, exampleUser)
  val view = page("submissionId")(verifiedRequest, messages)

  "UnavailableTimelineActions Page view" should {

    "display a 'Back' button that links to the 'Timeline' page" in {
      val backButton = view.getElementById("back-link")
      backButton must containMessage("site.back")
      backButton must haveHref(DeclarationDetailsController.displayPage("submissionId"))
    }

    "display the expected page header" in {
      view.getElementsByTag("h1").first() must containMessage("declaration.details.unavailable.actions.header")
    }

    "display the expected body" in {
      val text = view.getElementsByClass("govuk-body").first.text
      text mustBe messages("declaration.details.unavailable.actions.paragraph")
    }
  }
}
