/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.section1.routes._
import controllers.section2.routes.{DeclarantExporterController, EntryIntoDeclarantsRecordsController}
import models.DeclarationType.{CLEARANCE, SUPPLEMENTARY}
import models.ExportsDeclaration
import models.declaration.DeclarationStatus.{COMPLETE, DRAFT}
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.Submission
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.GovukInsetText
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.insettext.InsetText
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key, SummaryListRow}
import views.helpers.EnhancedStatusHelper.asText
import views.helpers.ViewDates
import views.helpers.ViewDates.formatDateAtTime
import views.html.components.gds.{link, paragraphBody}
import views.html.summary.summary_card

import javax.inject.{Inject, Singleton}

@Singleton
class Card1ForReferences @Inject() (
  summaryCard: summary_card,
  govukInsetText: GovukInsetText,
  link: link,
  paragraph: paragraphBody,
  appConfig: AppConfig
) extends SummaryCard {

  // Called by the Final CYA page
  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true, hasErrors: Boolean = false, maybeSubmission: Option[Submission] = None)(
    implicit messages: Messages
  ): Html = {
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

    val heading = Html(s"""<h2 class="govuk-heading-m">${messages("declaration.summary.heading")}</h2>""")

    val hint =
      if (meta.status == COMPLETE) HtmlFormat.empty
      else paragraph(messages("declaration.summary.amend.body"), "govuk-body govuk-!-display-none-print change-links-paragraph")

    HtmlFormat.fill(List(insets, heading, hint, content(declaration, actionsEnabled, hasErrors, maybeSubmission)))
  }

  // Called by the Mini CYA page
  override def content(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    content(declaration, actionsEnabled, false, None)

  override def backLink(implicit request: JourneyRequest[_]): Call =
    if (request.declarationType == SUPPLEMENTARY) ConsignmentReferencesController.displayPage
    else if (request.cacheModel.mucr.isEmpty) LinkDucrToMucrController.displayPage
    else MucrController.displayPage

  override def continueTo(implicit request: JourneyRequest[_]): Call =
    if (request.declarationType == CLEARANCE) EntryIntoDeclarantsRecordsController.displayPage
    else DeclarantExporterController.displayPage

  private def content(declaration: ExportsDeclaration, actionsEnabled: Boolean, hasErrors: Boolean, maybeSubmission: Option[Submission])(
    implicit messages: Messages
  ): Html =
    summaryCard(card(1, hasErrors), rows(declaration, actionsEnabled, maybeSubmission))

  private def rows(declaration: ExportsDeclaration, actionsEnabled: Boolean, maybeSubmission: Option[Submission])(
    implicit messages: Messages
  ): Seq[SummarySection] =
    List(maybeSummarySection(
      List(creationDate(declaration), expiryDate(declaration), mrnOfSubmission(maybeSubmission)) ++
      notificationStatuses(maybeSubmission).getOrElse(List.empty) ++
      List(
        declarationType(declaration),
        additionalDeclarationType(declaration),
        declarantEori(declaration),
        ducr(declaration, actionsEnabled),
        mrn(declaration, actionsEnabled),
        eidrDate(declaration, actionsEnabled),
        lrn(declaration, actionsEnabled),
        linkDucrToMucr(declaration, actionsEnabled),
        mucr(declaration, actionsEnabled)
      )
    )).flatten

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

  private def mrnOfSubmission(maybeSubmission: Option[Submission])(implicit messages: Messages): Option[SummaryListRow] =
    maybeSubmission.flatMap { submission =>
      submission.mrn.map(mrn => SummaryListRow(key("references.submission.mrn"), value(mrn), classes = "submission-mrn"))
    }

  private def notificationStatuses(maybeSubmission: Option[Submission])(implicit messages: Messages): Option[Seq[Option[SummaryListRow]]] =
    for {
      submission <- maybeSubmission
      action <- submission.actions.find(_.requestType == SubmissionRequest)
      notifications <- action.notifications
    } yield notifications.map { notification =>
      Some(SummaryListRow(
        Key(Text(asText(notification.enhancedStatus))),
        valueHtml(ViewDates.formatDateAtTime(notification.dateTimeIssued)),
        classes = "notification-status"
      ))
    }

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

  private def declarantEori(declaration: ExportsDeclaration)(implicit messages: Messages): Option[SummaryListRow] =
    declaration.parties.declarantDetails.map { declarantDetails =>
      SummaryListRow(key("parties.declarant.eori"), value(declarantDetails.details.eori.fold("")(_.value)), classes = "declarant-eori")
    }

  private def ducr(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] = {
    def action: Option[Actions] =
      if (declaration.isAmendmentDraft) None
      else {
        val call = if (declaration.isType(SUPPLEMENTARY)) ConsignmentReferencesController.displayPage else DucrEntryController.displayPage
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
    def action: Option[Actions] =
      if (declaration.isAmendmentDraft) None
      else {
        val call = if (declaration.isType(SUPPLEMENTARY)) ConsignmentReferencesController.displayPage else LocalReferenceNumberController.displayPage
        changeLink(call, "references.lrn", actionsEnabled)
      }

    declaration.consignmentReferences.flatMap(_.lrn).map { lrn =>
      SummaryListRow(key("references.lrn"), value(lrn.lrn), classes = "lrn", action)
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
