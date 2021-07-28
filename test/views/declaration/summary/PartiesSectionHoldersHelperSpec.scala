/*
 * Copyright 2021 HM Revenue & Customs
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

package views.declaration.summary

import base.JourneyTypeTestRunner
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType._
import models.Mode

class PartiesSectionHoldersHelperSpec extends JourneyTypeTestRunner {

  private val partiesSectionHoldersHelper = new PartiesSectionHoldersHelper

  "PartiesSectionHoldersHelper on changeLinkUrl" when {

    onJourney(OCCASIONAL) { request =>
      "return DeclarationHolderRequiredController call" in {
        val exportsDeclaration = aDeclaration(withType(request.declarationType))

        val result = partiesSectionHoldersHelper.changeLinkTarget(exportsDeclaration)

        result(Mode.Normal) mustBe controllers.declaration.routes.DeclarationHolderRequiredController.displayPage(Mode.Normal)
      }
    }

    onJourney(CLEARANCE) { request =>
      "it is EntryIntoDeclarantsRecords" should {
        "return AuthorisationProcedureCodeChoiceController call" in {
          val exportsDeclaration = aDeclaration(withType(request.declarationType), withEntryIntoDeclarantsRecords(YesNoAnswers.yes))

          val result = partiesSectionHoldersHelper.changeLinkTarget(exportsDeclaration)

          result(Mode.Normal) mustBe controllers.declaration.routes.AuthorisationProcedureCodeChoiceController.displayPage(Mode.Normal)
        }
      }

      "it is NOT EntryIntoDeclarantsRecords" should {
        "return DeclarationHolderRequiredController call" in {
          val exportsDeclaration = aDeclaration(withType(request.declarationType))

          val result = partiesSectionHoldersHelper.changeLinkTarget(exportsDeclaration)

          result(Mode.Normal) mustBe controllers.declaration.routes.DeclarationHolderRequiredController.displayPage(Mode.Normal)
        }
      }
    }

    onJourney(STANDARD, SIMPLIFIED, SUPPLEMENTARY) { request =>
      "return AuthorisationProcedureCodeChoiceController call" in {
        val exportsDeclaration = aDeclaration(withType(request.declarationType))

        val result = partiesSectionHoldersHelper.changeLinkTarget(exportsDeclaration)

        result(Mode.Normal) mustBe controllers.declaration.routes.AuthorisationProcedureCodeChoiceController.displayPage(Mode.Normal)
      }
    }
  }
}
