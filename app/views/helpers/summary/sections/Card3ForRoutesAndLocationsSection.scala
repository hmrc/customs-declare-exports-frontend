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

import controllers.declaration.routes
import models.DeclarationType._
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.Html
import views.helpers.summary.Card3ForRoutesAndLocations

import javax.inject.{Inject, Singleton}

@Singleton
class Card3ForRoutesAndLocationsSection @Inject() (card3ForRoutesAndLocations: Card3ForRoutesAndLocations) extends SectionCard {

  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true)(implicit messages: Messages): Html =
    card3ForRoutesAndLocations.eval(declaration)

  def backLink(implicit request: JourneyRequest[_]): Call =
    routes.OfficeOfExitController.displayPage

  def continueTo(implicit request: JourneyRequest[_]): Call =
    request.declarationType match {
      case SUPPLEMENTARY | STANDARD            => routes.InvoiceAndExchangeRateChoiceController.displayPage
      case OCCASIONAL | SIMPLIFIED | CLEARANCE => routes.PreviousDocumentsSummaryController.displayPage
    }
}
