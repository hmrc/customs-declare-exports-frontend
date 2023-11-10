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

import controllers.declaration.routes.AuthorisationProcedureCodeChoiceController
import forms.declaration.authorisationHolder.AuthorisationHolder
import models.declaration.Parties
import play.api.i18n.Messages
import services.view.HolderOfAuthorisationCodes
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}

import javax.inject.{Inject, Singleton}

@Singleton
class AuthorisationHoldersHelper @Inject() (holderOfAuthorisationCodes: HolderOfAuthorisationCodes) extends SummaryHelper {

  def section(parties: Parties, hasAdditionalActors: Boolean, actionsEnabled: Boolean)(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    parties.declarationHoldersData.map { data =>
      val summaryListRows = data.holders.zipWithIndex.flatMap { case (holder, index) =>
        List(holderTypeCode(holder, index + 1, actionsEnabled), holderEori(holder, index + 1, actionsEnabled))
      }
      if (summaryListRows.flatten.isEmpty) headingOnNoHolders(hasAdditionalActors, actionsEnabled)
      else heading("authorisation-holders", "parties.holders") +: summaryListRows
    }
      .getOrElse(List.empty)

  private def headingOnNoHolders(hasAdditionalActors: Boolean, actionsEnabled: Boolean)(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    List(
      Some(
        SummaryListRow(
          key = if (hasAdditionalActors) keyForEmptyAttrAfterAttrWithMultipleRows("parties.holders") else key("parties.holders"),
          valueKey("site.none"),
          classes = "authorisation-holders-heading",
          changeHolders(actionsEnabled)
        )
      )
    )

  private def holderTypeCode(holder: AuthorisationHolder, index: Int, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    holder.authorisationTypeCode.map { typeCode =>
      SummaryListRow(
        key("parties.holders.type"),
        value(holderOfAuthorisationCodes.codeDescription(messages.lang.toLocale, typeCode)),
        classes = s"""${holder.eori.fold("")(_ => "govuk-summary-list__row--no-border ")}authorisation-holder-$index-type""",
        changeHolders(actionsEnabled)
      )
    }

  private def holderEori(holder: AuthorisationHolder, index: Int, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    holder.eori.map { eori =>
      SummaryListRow(
        key("parties.holders.eori"),
        value(eori.value),
        classes = s"authorisation-holder-$index-eori",
        if (holder.authorisationTypeCode.isEmpty) changeHolders(actionsEnabled) else None
      )
    }

  private def changeHolders(actionsEnabled: Boolean)(implicit messages: Messages): Option[Actions] =
    changeLink(AuthorisationProcedureCodeChoiceController.displayPage, "parties.holders", actionsEnabled)
}
