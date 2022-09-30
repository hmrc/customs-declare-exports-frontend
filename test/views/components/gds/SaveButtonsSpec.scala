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

import base.MockAuthAction
import models.Mode
import models.Mode.{Change, Draft, ErrorFix, Normal}
import models.declaration.submissions.EnhancedStatus.{ERRORS, EnhancedStatus, RECEIVED}
import models.requests.JourneyRequest
import play.api.mvc.AnyContent
import play.twirl.api.Html
import views.declaration.spec.UnitViewSpec
import views.html.components.gds.saveButtons

class SaveButtonsSpec extends UnitViewSpec with MockAuthAction {

  private val saveButtons = instanceOf[saveButtons]
  private def request(status: EnhancedStatus) =
    new JourneyRequest[AnyContent](getAuthenticatedRequest(""), aDeclaration(withParentDeclarationEnhancedStatus(status)))

  private def createButtons(mode: Mode, status: EnhancedStatus): Html = saveButtons(mode)(request(status), messages)

  "Save buttons" should {

    "display Save and Continue button" in {
      val buttons = createButtons(Normal, RECEIVED)
      Option(buttons.getElementsByAttributeValue("name", "SaveAndContinue")).isDefined
    }

    "display Save and come back later link" in {
      val buttons = createButtons(Normal, RECEIVED)
      Option(buttons.getElementsByAttributeValue("name", "SaveAndReturn")).isDefined
    }

    "display Save and return to summary button in draft or change mode or when parent dec has errors" when {
      for {
        mode <- Mode.modes
        status <- List(RECEIVED, ERRORS)
      } s" mode is $mode and status is $status" in {
        val buttonsPage = createButtons(mode, status)
        val buttons = Option(buttonsPage.getElementById("save_and_return_to_summary"))

        mode match {
          case _ if status == ERRORS => buttons.isDefined mustBe true
          case Draft | Change        => buttons.isDefined mustBe true
          case _                     => buttons.isEmpty mustBe true
        }
      }
    }

    "display Save and return to errors button appropriately" when {
      for (mode <- Mode.modes) s"$mode" in {
        val buttonsPage = createButtons(mode, RECEIVED)
        val buttons = Option(buttonsPage.getElementById("save_and_return_to_errors"))

        mode match {
          case ErrorFix => buttons.isDefined mustBe true
          case _        => buttons.isEmpty mustBe true
        }
      }
    }
  }
}
