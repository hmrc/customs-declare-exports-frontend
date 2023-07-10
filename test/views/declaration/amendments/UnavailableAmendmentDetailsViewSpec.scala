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

package views.declaration.amendments

import base.{Injector, MockAuthAction}
import controllers.routes.DeclarationDetailsController
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.amendments.unavailable_amendment_details
import views.tags.ViewTest

@ViewTest
class UnavailableAmendmentDetailsViewSpec extends UnitViewSpec with CommonMessages with Injector with MockAuthAction {

  private val page = instanceOf[unavailable_amendment_details]

  "UnavailableAmendmentDetails page" should {

    val view = page(request.cacheModel.id)(buildVerifiedEmailRequest(request, exampleUser), messages)

    "display 'Back' button that links to /submissions/:id/information" in {
      val backButton = view.getElementById("back-link")
      backButton must containMessage(backCaption)
      backButton must haveHref(DeclarationDetailsController.displayPage(request.cacheModel.id).url)
    }

    "display page title" in {
      view.getElementsByTag("h1").text mustBe messages("amendment.details.unavailable.header")
    }

    "display the expected body" in {
      view.getElementsByClass("govuk-body").text mustBe messages("amendment.details.unavailable.paragraph")
    }
  }
}
