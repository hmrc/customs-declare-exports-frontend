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

import models.Mode
import models.Mode.{Change, ChangeAmend, Draft, Normal}
import play.twirl.api.Html
import views.declaration.spec.UnitViewSpec
import views.declaration.spec.UnitViewSpec.instanceOf
import views.html.components.gds.saveButtons

class SaveButtonsSpec extends UnitViewSpec {

  private val saveButtons = instanceOf[saveButtons]

  private def createButtons(mode: Mode): Html = saveButtons(mode)(messages)

  "Save buttons" should {

    "display Save and Continue button" in {
      val buttons = createButtons(Normal)
      Option(buttons.getElementsByAttributeValue("name", "SaveAndContinue")).isDefined
    }

    "display Save and come back later link" in {
      val buttons = createButtons(Normal)
      Option(buttons.getElementsByAttributeValue("name", "SaveAndReturn")).isDefined
    }

    for (mode <- Mode.modes) s"display Save and return to summary button appropriately in $mode mode" in {
      val buttonsPage = createButtons(mode)
      val buttons = Option(buttonsPage.getElementById("save_and_return_to_summary"))

      mode match {
        case Draft | ChangeAmend | Change => buttons.isDefined
        case _                            => buttons.isEmpty
      }
    }
  }
}