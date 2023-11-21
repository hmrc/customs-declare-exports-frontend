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

package controllers.declaration

import controllers.actions.{AuthAction, JourneyAction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.helpers.ActionItemBuilder.lastUrlPlaceholder
import views.helpers.summary._
import views.html.declaration.summary.sections.section_summary

import javax.inject.Inject

class SectionSummaryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  mcc: MessagesControllerComponents,
  section_summary: section_summary,
  card1ForReferences: Card1ForReferences,
  card2ForParties: Card2ForParties,
  card3ForRoutesAndLocations: Card3ForRoutesAndLocations,
  card4ForTransactions: Card4ForTransactions
) extends FrontendController(mcc) with I18nSupport {

  def displayPage(sectionNumber: Int): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    def sectionSummary(summaryCard: SummaryCard): Result =
      Ok(Html(section_summary(summaryCard).toString.replace(s"?$lastUrlPlaceholder", "")))

    sectionNumber match {
      case 1 => sectionSummary(card1ForReferences)
      case 2 => sectionSummary(card2ForParties)
      case 3 => sectionSummary(card3ForRoutesAndLocations)
      case 4 => sectionSummary(card4ForTransactions)
      case _ => Redirect(routes.SummaryController.displayPage)
    }
  }
}
