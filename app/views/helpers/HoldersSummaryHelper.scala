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

package views.helpers

import controllers.declaration.routes.AuthorisationProcedureCodeChoiceController
import forms.declaration.declarationHolder.DeclarationHolder
import models.ExportsDeclaration
import play.api.i18n.Messages
import play.twirl.api.Html
import services.view.HolderOfAuthorisationCodes
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow, Value}
import views.helpers.ActionItemBuilder.actionSummaryItem
import views.html.components.gds.linkContent

import javax.inject.{Inject, Singleton}

@Singleton
class HoldersSummaryHelper @Inject() (
  govukSummaryList: GovukSummaryList,
  linkContent: linkContent,
  holderOfAuthorisationCodes: HolderOfAuthorisationCodes
) {
  def section(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Html = {
    val summaryListRows = declaration.declarationHolders.zipWithIndex.flatMap { case (holder, index) =>
      List(holderTypeCode(holder, index + 1, actionsEnabled), holderEori(holder, index + 1, actionsEnabled)).flatten
    }
    val noHolders = summaryListRows.length == 0

    govukSummaryList(
      SummaryList(
        rows = if (noHolders) headingOnNoHolders(actionsEnabled) else heading(actionsEnabled) +: summaryListRows,
        classes = s"""${if (noHolders) "" else "govuk-!-margin-top-4 "}govuk-!-margin-bottom-9 authorisation-holders-summary"""
      )
    )
  }

  private def heading(actionsEnabled: Boolean)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(Text(messages("declaration.summary.parties.holders")), classes = "govuk-heading-s"),
      classes = "authorisation-holder-heading",
      actions = if (actionsEnabled) Some(Actions(items = List(ActionItem()))) else None
    )

  private def headingOnNoHolders(actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummaryListRow] =
    List(
      SummaryListRow(
        Key(Text(messages("declaration.summary.parties.holders")), classes = "govuk-heading-s"),
        Value(Text(messages("site.none"))),
        classes = "authorisation-holder-heading",
        actions = changeHolder(None, actionsEnabled)
      )
    )

  private def holderTypeCode(holder: DeclarationHolder, index: Int, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    holder.authorisationTypeCode.map { typeCode =>
      SummaryListRow(
        Key(Text(messages("declaration.summary.parties.holders.type"))),
        Value(Text(holderOfAuthorisationCodes.codeDescription(messages.lang.toLocale, typeCode))),
        classes = s"govuk-summary-list__row--no-border authorisation-holder-type-$index",
        actions = changeHolder(Some(holder), actionsEnabled)
      )
    }

  private def holderEori(holder: DeclarationHolder, index: Int, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    holder.eori.map { eori =>
      SummaryListRow(
        Key(Text(messages("declaration.summary.parties.holders.eori"))),
        Value(Text(eori.value)),
        classes = s"authorisation-holder-eori-$index",
        actions = if (holder.authorisationTypeCode.isEmpty) changeHolder(Some(holder), actionsEnabled) else None
      )
    }

  private def changeHolder(maybeHolder: Option[DeclarationHolder], actionsEnabled: Boolean)(implicit messages: Messages): Option[Actions] =
    if (!actionsEnabled) None
    else {
      val hiddenText = maybeHolder.fold(messages("declaration.summary.parties.holders.empty.change")) { holder =>
        messages("declaration.summary.parties.holders.change", holder.authorisationTypeCode.getOrElse(""), holder.eori.getOrElse(""))
      }
      val content = HtmlContent(linkContent(messages("site.change")))
      val actionItem = actionSummaryItem(AuthorisationProcedureCodeChoiceController.displayPage.url, content, Some(hiddenText))

      Some(Actions(items = List(actionItem)))
    }
}
