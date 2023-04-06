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

package views.components.gds

import base.Injector
import models.declaration.submissions.EnhancedStatus
import models.declaration.submissions.EnhancedStatus.{ERRORS, EnhancedStatus, RECEIVED}
import models.requests.SessionHelper.errorFixModeSessionKey
import play.twirl.api.Html
import views.declaration.spec.UnitViewSpec
import views.html.components.gds.saveButtons

class SaveButtonsSpec extends UnitViewSpec with Injector {

  private val page = instanceOf[saveButtons]

  private def createView(status: EnhancedStatus, session: (String, String)*): Html =
    page()(journeyRequest(aDeclaration(withParentDeclarationEnhancedStatus(status)), session: _*), messages)

  "Save buttons" should {

    val session = errorFixModeSessionKey -> "true"

    "display' 'Save and Continue' button" in {
      val buttons = createView(RECEIVED)
      Option(buttons.getElementsByAttributeValue("name", "SaveAndContinue")).isDefined
    }

    "display 'Save and come back' later link" in {
      val buttons = createView(RECEIVED)
      Option(buttons.getElementsByAttributeValue("name", "SaveAndReturn")).isDefined
    }

    "NOT display the 'Save and return to summary' button when the declaration has no errors" in {
      EnhancedStatus.values.filterNot(_ == ERRORS).foreach { status =>
        Option(createView(status).getElementById("save_and_return_to_summary")).isEmpty
      }
    }

    "NOT display the 'Save and return to summary' button when the declaration has errors but in error-fix mode" in {
      val view = createView(ERRORS, session)
      Option(view.getElementById("save_and_return_to_summary")).isEmpty
    }

    "display the 'Save and return to summary' button when the declaration has errors and NOT in error-fix mode" in {
      Option(createView(ERRORS).getElementById("save_and_return_to_summary")).isDefined
    }

    "display the 'Save and return to summary' button when the Summary page has been visited" in {
      val view = page()(journeyRequest(aDeclaration(withSummaryWasVisited())), messages)
      Option(view.getElementById("save_and_return_to_summary")).isDefined
    }

    "display the 'Save and return to errors' button when in error-fix mode" in {
      val buttonsPage = createView(RECEIVED, session)
      Option(buttonsPage.getElementById("save_and_return_to_errors")).isDefined
    }

    "NOT display the 'Save and return to errors' button when NOT in error-fix mode" in {
      val buttonsPage = createView(RECEIVED)
      Option(buttonsPage.getElementById("save_and_return_to_errors")).isEmpty
    }
  }
}
