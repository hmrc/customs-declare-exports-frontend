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

package views.declaration

import java.time.{LocalDateTime, ZoneOffset}

import base.Injector
import models.responses.FlashKeys
import play.api.mvc.Flash
import play.twirl.api.Html
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.draft_declaration_page
import views.tags.ViewTest

@ViewTest
class DraftDeclarationViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val page = instanceOf[draft_declaration_page]
  private def createView(flash: (String, String)*): Html = page()(request, Flash(Map(flash: _*)), messages)

  "View" should {
    "render expiry date" when {
      "present in flash" in {
        val date = LocalDateTime.of(2019, 1, 1, 1, 1).toInstant(ZoneOffset.UTC)
        val view = createView(FlashKeys.expiryDate -> date.toEpochMilli.toString)
        view.getElementById("draft_confirmation-expiry") must containText("1 January 2019")
      }

      "missing from flash" in {
        Option(createView().getElementById("draft_confirmation-expiry")) mustBe None
      }
    }

    "render continue link" in {
      val link = createView().getElementById("draft_confirmation-continue_dec_link")
      link must haveHref(controllers.routes.SavedDeclarationsController.displayDeclarations().url)
    }

    "render start new link" in {
      val link = createView().getElementById("draft_confirmation-create_dec_link")
      link must haveHref(controllers.routes.ChoiceController.displayPage().url)
    }

    "render back to GOV.UK" in {
      val button = createView().getElementById("govuk-link")
      button must haveHref("https://www.gov.uk")
    }
  }
}
