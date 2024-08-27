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

package views.drafts

import base.Injector
import controllers.routes.{ChoiceController, SavedDeclarationsController}
import play.twirl.api.Html
import tools.Stubs
import views.common.UnitViewSpec
import views.helpers.CommonMessages
import views.html.drafts.exit_and_complete_later
import views.tags.ViewTest

import java.time.{LocalDateTime, ZoneOffset}

@ViewTest
class ExitAndCompleteLaterViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  val declarationId = Some("declarationUuid")
  val date: String = LocalDateTime.of(2019, 1, 1, 1, 1).toInstant(ZoneOffset.UTC).toEpochMilli.toString

  private val page = instanceOf[exit_and_complete_later]

  private def createView(): Html = page(declarationId, date)(request, messages)

  "View" should {

    "display page title" in {
      createView().getElementsByTag("h1").first().text() mustBe messages("declaration.draft.title")
    }

    "render expiry date" in {
      val view = createView()
      view.getElementById("draft_confirmation-expiry") must containText(messages("declaration.draft.info", "1 January 2019"))
    }

    "render view declaration summary link" in {
      val link = createView().getElementById("view_declaration_summary")
      link must haveHref(SavedDeclarationsController.displayDeclaration(declarationId.get).url)
    }

    "render continue link" in {
      val link = createView().getElementById("draft_confirmation-continue_dec_link")
      link must haveHref(SavedDeclarationsController.displayDeclarations().url)
    }

    "render start new link" in {
      val link = createView().getElementById("draft_confirmation-create_dec_link")
      link must haveHref(ChoiceController.displayPage.url)
    }

    "render back to GOV.UK" in {
      val button = createView().getElementById("govuk-link")
      button must haveHref("https://www.gov.uk")
    }
  }
}
