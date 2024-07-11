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

package views.amendments

import base.{Injector, MockAuthAction}
import config.ExternalServicesConfig
import controllers.timeline.routes.DeclarationDetailsController
import views.common.UnitViewSpec
import views.helpers.CommonMessages
import views.html.amendments.unavailable_amendment_details
import views.tags.ViewTest

@ViewTest
class UnavailableAmendmentDetailsViewSpec extends UnitViewSpec with CommonMessages with Injector with MockAuthAction {

  private val page = instanceOf[unavailable_amendment_details]

  private val ducr = "8GB123456802352-101SHIP1"
  private val externalServicesConfig = instanceOf[ExternalServicesConfig]

  "UnavailableAmendmentDetails page" should {
    val view = page(request.cacheModel.id, Some(ducr))(buildVerifiedEmailRequest(request, exampleUser), messages)

    "display 'Back' button that links to /submissions/:id/information" in {
      val backButton = view.getElementById("back-link")
      backButton must containMessage(backCaption)
      backButton must haveHref(DeclarationDetailsController.displayPage(request.cacheModel.id).url)
    }

    "display page title" in {
      view.getElementsByTag("h1").text mustBe messages("amendment.details.unavailable.title")
    }

    "display a section header for the DUCR" in {
      view.getElementById("section-header").text mustBe s"DUCR: $ducr"
    }

    "display the expected paragraphs" in {
      val paragraphs = view.getElementsByClass("govuk-body")
      paragraphs.size mustBe 6

      paragraphs.get(1) must containMessage("amendment.details.unavailable.paragraph1")
      paragraphs.get(2) must containMessage("amendment.details.unavailable.paragraph2")

      paragraphs.get(3) must containMessage("amendment.details.unavailable.paragraph3")
      paragraphs.get(4) must containMessage("amendment.details.unavailable.paragraph4", messages("amendment.details.unavailable.paragraph4.link"))
      paragraphs.get(5) must containMessage("amendment.details.unavailable.timeline.link")
    }

    "display a link targeting the 'Movements' service" in {
      val linkToMovements = view.getElementById("movements-url")
      linkToMovements must containMessage("amendment.details.unavailable.paragraph4.link")
      linkToMovements must haveHref(externalServicesConfig.customsMovementsFrontendUrl)
    }

    "display a bottom link targeting the timeline page" in {
      val linkToTimeline = view.getElementById("timeline-url")
      linkToTimeline must containMessage("amendment.details.unavailable.timeline.link")
      linkToTimeline must haveHref(DeclarationDetailsController.displayPage(request.cacheModel.id).url)
    }
  }
}
