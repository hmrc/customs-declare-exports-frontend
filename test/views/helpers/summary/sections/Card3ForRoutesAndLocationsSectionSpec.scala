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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.twirl.api.Html
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.helpers.summary.Card3ForRoutesAndLocations

class Card3ForRoutesAndLocationsSectionSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  val declaration = aDeclaration()

  val card3ForRoutesAndLocations = mock[Card3ForRoutesAndLocations]
  val card3ForRoutesAndLocationsSection = new Card3ForRoutesAndLocationsSection(card3ForRoutesAndLocations)

  when(card3ForRoutesAndLocations.eval(any(), any())(any()))
    .thenReturn(Html("content"))

  "Card3ForRoutesAndLocationsSection.eval" must {
    "return the html of the cya card" in {
      card3ForRoutesAndLocationsSection.eval(declaration) mustBe Html("content")
    }
  }

  "Card3ForRoutesAndLocationsSection.continueTo" when {
    onJourney(OCCASIONAL, SIMPLIFIED, CLEARANCE) { implicit request =>
      s"${request.declarationType}" must {
        "go to DeclarantExporterController" in {
          card3ForRoutesAndLocationsSection.continueTo mustBe routes.PreviousDocumentsSummaryController.displayPage
        }
      }
    }
    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      s"${request.declarationType}" must {
        "go to DeclarantExporterController" in {
          card3ForRoutesAndLocationsSection.continueTo mustBe routes.InvoiceAndExchangeRateChoiceController.displayPage
        }
      }
    }
  }

  "Card3ForRoutesAndLocationsSection.backLink" must {
    "go to OfficeOfExitController" in {
      card3ForRoutesAndLocationsSection.backLink(journeyRequest(aDeclarationAfter(declaration))) mustBe routes.OfficeOfExitController.displayPage
    }
  }

}
