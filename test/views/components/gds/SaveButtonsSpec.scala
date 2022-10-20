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
import models.declaration.submissions.EnhancedStatus
import models.declaration.submissions.EnhancedStatus.{ERRORS, EnhancedStatus, RECEIVED}
import play.twirl.api.Html
import views.declaration.spec.UnitViewSpec
import views.html.components.gds.saveButtons

class SaveButtonsSpec extends UnitViewSpec with Injector {

  private val page = instanceOf[saveButtons]

  private def createView(status: EnhancedStatus): Html =
    page()(journeyRequest(aDeclaration(withParentDeclarationEnhancedStatus(status))), messages)

  "Save buttons" should {

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
        val element = Option(createView(status).getElementById("save_and_return_to_summary"))
        assert(element.isEmpty)
      }
    }

    "NOT display the 'Save and return to summary' button when the declaration has errors but mode is ErrorFix" in {
      val element = Option(createView(ErrorFix, ERRORS).getElementById("save_and_return_to_summary"))
      assert(element.isEmpty)
    }

    "display the 'Save and return to summary' button when the declaration has errors and mode is NOT ErrorFix" in {
      Mode.modes.filterNot(_ == ErrorFix).foreach { mode =>
        val element = Option(createView(ERRORS).getElementById("save_and_return_to_summary"))
        assert(element.isDefined)
      }
    }

    "display the 'Save and return to summary' button when the Summary page has been visited" in {
      Mode.modes.foreach { mode =>
        val view = page()(journeyRequest(aDeclaration(withSummaryWasVisited())), messages)
        val element = Option(view.getElementById("save_and_return_to_summary"))
        assert(element.isDefined)
      }
    }

    "display Save and return to errors button appropriately" when {
      for (mode <- Mode.modes) s"$mode" in {
        val buttonsPage = createView(RECEIVED)
        val buttons = Option(buttonsPage.getElementById("save_and_return_to_errors"))

        mode match {
          case ErrorFix => buttons.isDefined mustBe true
          case _        => buttons.isEmpty mustBe true
        }
      }
    }
  }
}
