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
import controllers.declaration.routes.{DeclarantExporterController, EntryIntoDeclarantsRecordsController}
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Call}
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Card, CardTitle}
import views.helpers.summary.Card1ForReferences
import views.html.components.gds.heading

import javax.inject.{Inject, Singleton}

@Singleton
class Card1ForReferencesSection @Inject() (card1ForReferences: Card1ForReferences, govukSummaryList: GovukSummaryList, heading: heading)
    extends ReferencesSectionCard {

  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true)(implicit messages: Messages): Html = {
    val card = Some(Card(Some(CardTitle(Text(messages(s"declaration.section.1")), classes = s"section-1-card"))))

    HtmlFormat.fill(
      List(
        heading(messages("declaration.summary.section", "1")),
        govukSummaryList(SummaryList(card1ForReferences.rows(declaration, actionsEnabled), card))
      )
    )
  }

  def backLink(implicit request: JourneyRequest[AnyContent]): Call =
    if (request.cacheModel.mucr.isEmpty) routes.LinkDucrToMucrController.displayPage
    else routes.MucrController.displayPage

  def continueTo(implicit request: JourneyRequest[AnyContent]): Call =
    if (request.declarationType == CLEARANCE) EntryIntoDeclarantsRecordsController.displayPage
    else DeclarantExporterController.displayPage

}
