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

import config.AppConfig
import controllers.declaration.routes._
import models.DeclarationType.SUPPLEMENTARY
import models.ExportsDeclaration
import models.declaration.DeclarationStatus.{COMPLETE, DRAFT}
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukInsetText, GovukSummaryList, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.insettext.InsetText
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import views.helpers.ViewDates.formatDateAtTime
import views.html.components.gds.link

import javax.inject.{Inject, Singleton}

@Singleton
class Card1ForReferences @Inject() (govukSummaryList: GovukSummaryList, govukInsetText: GovukInsetText, link: link, appConfig: AppConfig)
    extends SummaryHelper {

  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true)(implicit messages: Messages): Html = {
    val meta = declaration.declarationMeta

    val insets =
      if (!(meta.status == DRAFT && meta.parentDeclarationId.isDefined)) HtmlFormat.empty
      else
        govukInsetText(
          InsetText(
            content = HtmlContent(link(messages("declaration.summary.references.insets"), AdditionalDeclarationTypeController.displayPage)),
            classes = "start-here-insets"
          )
        )

    val heading = Html(s"""<h2 class="govuk-heading-m">${messages("declaration.summary.references")}</h2>""")

    val paragraph =
      if (meta.status == COMPLETE) HtmlFormat.empty
      else Html(s"""<p classes="govuk-body govuk-!-display-none-print change-links-paragraph">${messages("declaration.summary.amend.body")}</p>""")

    HtmlFormat.fill(List(insets, heading, paragraph, govukSummaryList(SummaryList(rows(declaration, actionsEnabled), card("references")))))
  }

  private def rows(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummaryListRow] =
    List(
      creationDate(declaration),
      expiryDate(declaration),
      declarationType(declaration),
      additionalDeclarationType(declaration),
      ducr(declaration, actionsEnabled),
      mrn(declaration, actionsEnabled),
      eidrDate(declaration, actionsEnabled),
      lrn(declaration, actionsEnabled),
      linkDucrToMucr(declaration, actionsEnabled),
      mucr(declaration, actionsEnabled)
    ).flatten

  private def creationDate(declaration: ExportsDeclaration)(implicit messages: Messages): Option[SummaryListRow] =
    Some(
      SummaryListRow(key("references.creation.date"), value(formatDateAtTime(declaration.declarationMeta.createdDateTime)), classes = "creation-date")
    )

  private def expiryDate(declaration: ExportsDeclaration)(implicit messages: Messages): Option[SummaryListRow] =
    Some(
      SummaryListRow(
        key("references.expiration.date"),
        value(formatDateAtTime(declaration.declarationMeta.updatedDateTime.plusSeconds(appConfig.draftTimeToLive.toSeconds))),
        classes = "expiration-date"
      )
    )

  private def declarationType(declaration: ExportsDeclaration)(implicit messages: Messages): Option[SummaryListRow] =
    Some(
      SummaryListRow(
        key("references.type"),
        value(messages(s"declaration.type.${declaration.`type`.toString.toLowerCase}")),
        classes = "declaration-type"
      )
    )

  private def additionalDeclarationType(declaration: ExportsDeclaration)(implicit messages: Messages): Option[SummaryListRow] =
    declaration.additionalDeclarationType.map { adt =>
      SummaryListRow(
        key("references.additionalType"),
        value(messages(s"declaration.summary.references.additionalType.${adt.toString}")),
        classes = "additional-declaration-type"
      )
    }

  private def ducr(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] = {
    def action: Option[Actions] =
      if (declaration.isAmendmentDraft) None
      else {
        val call =
          if (declaration.isType(SUPPLEMENTARY)) ConsignmentReferencesController.displayPage
          else DucrEntryController.displayPage

        changeLink(call, "references.ducr", actionsEnabled)
      }

    declaration.consignmentReferences.flatMap(_.ducr).map { ducr =>
      SummaryListRow(key("references.ducr"), value(ducr.ducr), classes = "ducr", action)
    }
  }

  private def mrn(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] = {
    def action: Option[Actions] =
      if (declaration.isAmendmentDraft) None
      else changeLink(ConsignmentReferencesController.displayPage, "references.mrn", actionsEnabled)

    declaration.consignmentReferences.flatMap(_.mrn).map { mrn =>
      SummaryListRow(key("references.mrn"), value(mrn.value), classes = "mrn", action)
    }
  }

  private def eidrDate(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] = {
    def action: Option[Actions] =
      if (declaration.isAmendmentDraft) None
      else changeLink(ConsignmentReferencesController.displayPage, "references.eidr", actionsEnabled)

    declaration.consignmentReferences.flatMap(_.eidrDateStamp).map { eidrDate =>
      SummaryListRow(key("references.eidr"), value(eidrDate), classes = "eidr-date", action)
    }
  }

  private def lrn(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] = {
    def label: String = if (declaration.isType(SUPPLEMENTARY)) "references.supplementary.lrn" else "references.lrn"

    def action: Option[Actions] =
      if (declaration.isAmendmentDraft) None
      else {
        val call =
          if (declaration.isType(SUPPLEMENTARY)) ConsignmentReferencesController.displayPage
          else LocalReferenceNumberController.displayPage

        changeLink(call, "references.lrn", actionsEnabled)
      }

    declaration.consignmentReferences.flatMap(_.lrn).map { lrn =>
      SummaryListRow(key(label), value(lrn.lrn), classes = "lrn", action)
    }
  }

  private def linkDucrToMucr(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] = {
    def action: Option[Actions] =
      if (declaration.isAmendmentDraft) None
      else changeLink(LinkDucrToMucrController.displayPage, "references.linkDucrToMucr", actionsEnabled)

    declaration.linkDucrToMucr.map { linkDucrToMucr =>
      SummaryListRow(key("references.linkDucrToMucr"), value(linkDucrToMucr.answer), classes = "link-ducr-to-mucr", action)
    }
  }

  private def mucr(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] = {
    def action: Option[Actions] =
      if (declaration.isAmendmentDraft) None
      else changeLink(MucrController.displayPage, "references.mucr", actionsEnabled)

    declaration.mucr.map { mucr =>
      SummaryListRow(key("references.mucr"), value(mucr.mucr), classes = "mucr", action)
    }
  }
}
