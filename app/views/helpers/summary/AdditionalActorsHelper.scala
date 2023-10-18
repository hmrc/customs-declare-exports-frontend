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

package views.helpers.summary

import controllers.declaration.routes.{AdditionalActorsAddController, AdditionalActorsSummaryController}
import forms.declaration.AdditionalActor
import models.declaration.Parties
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}

import javax.inject.Singleton

@Singleton
class AdditionalActorsHelper extends SummaryHelper {

  def section(parties: Parties, actionsEnabled: Boolean)(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    parties.declarationAdditionalActorsData.map { data =>
      val summaryListRows = data.actors.zipWithIndex.flatMap { case (actor, index) =>
        List(actorType(actor, index + 1, actionsEnabled), actorEori(actor, index + 1, actionsEnabled))
      }
      if (summaryListRows.flatten.isEmpty) headingOnNoActors(actionsEnabled)
      else heading("additional-actors", "parties.actors") +: summaryListRows
    }
      .getOrElse(List.empty)

  private def headingOnNoActors(actionsEnabled: Boolean)(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    List(
      Some(
        SummaryListRow(
          key("parties.actors"),
          valueKey("site.none"),
          classes = "additional-actors-heading",
          changeActors(AdditionalActorsAddController.displayPage, actionsEnabled)
        )
      )
    )

  private def actorType(actor: AdditionalActor, index: Int, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    actor.partyType.map { partyType =>
      SummaryListRow(
        key("parties.actors.type"),
        valueKey(s"declaration.summary.parties.actors.$partyType"),
        classes = s"""${actor.eori.fold("")(_ => "govuk-summary-list__row--no-border ")}additional-actor-$index-type""",
        changeActors(AdditionalActorsSummaryController.displayPage, actionsEnabled)
      )
    }

  private def actorEori(actor: AdditionalActor, index: Int, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    actor.eori.map { eori =>
      SummaryListRow(
        key("parties.actors.eori"),
        value(eori.value),
        classes = s"additional-actor-$index-eori",
        if (actor.partyType.isEmpty) changeActors(AdditionalActorsSummaryController.displayPage, actionsEnabled) else None
      )
    }

  private def changeActors(call: Call, actionsEnabled: Boolean)(implicit messages: Messages): Option[Actions] =
    changeLink(call, "parties.actors", actionsEnabled)
}
