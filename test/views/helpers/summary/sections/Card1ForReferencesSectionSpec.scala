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

package views.helpers.summary.sections

import base.Injector
import controllers.declaration.routes
import models.DeclarationType._
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec

class Card1ForReferencesSectionSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  val declaration = aDeclaration()

  val card1ForReferencesSection = instanceOf[Card1ForReferencesSection]

  "Card1ForReferencesSection.continueTo" must {
    onClearance { implicit request =>
      "go to EntryIntoDeclarantsRecordsController" in {
        card1ForReferencesSection.continueTo mustBe routes.EntryIntoDeclarantsRecordsController.displayPage
      }
    }
    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY) { implicit request =>
      "go to DeclarantExporterController" in {
        card1ForReferencesSection.continueTo mustBe routes.DeclarantExporterController.displayPage
      }
    }
  }

  "Card1ForReferencesSection.backLink" must {
    "go to LinkDucrToMucrController" when {
      "mucr isEmpty" in {
        card1ForReferencesSection.backLink(journeyRequest(declaration)) mustBe routes.LinkDucrToMucrController.displayPage
      }
    }
    "go to MucrController" when {
      "mucr has been answered" in {
        card1ForReferencesSection.backLink(journeyRequest(aDeclaration(withMucr()))) mustBe routes.MucrController.displayPage
      }
    }
  }

}
