/*
 * Copyright 2022 HM Revenue & Customs
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

import config.AppConfig
import controllers.routes.{DeclarationDetailsController, FileUploadController, SubmissionsController}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.from
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.Submission
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukPanel, GovukWarningText}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.panel.Panel
import uk.gov.hmrc.govukfrontend.views.viewmodels.warningtext.WarningText
import views.helpers.ViewDates.formatTimeDate
import views.html.components.exit_survey
import views.html.components.gds._

import javax.inject.{Inject, Singleton}

case class Confirmation(email: String, declarationType: String, submission: Option[Submission])

@Singleton
class ConfirmationHelper @Inject() (
  appConfig: AppConfig,
  exitSurvey: exit_survey,
  govukPanel: GovukPanel,
  govukWarningText: GovukWarningText,
  heading: heading,
  link: link,
  externalLink: externalLink,
  pageTitle: pageTitle,
  paragraph: paragraphBody
) {

  def content(confirmation: Confirmation)(implicit messages: Messages): Html =
    confirmation.submission.flatMap(_.latestEnhancedStatus) match {
      case Some(RECEIVED)                                                        => received(confirmation, messages)
      case Some(GOODS_ARRIVED) | Some(GOODS_ARRIVED_MESSAGE)                     => accepted(confirmation, messages)
      case Some(CLEARED) if isArrived(confirmation)                              => cleared(confirmation, messages)
      case Some(ADDITIONAL_DOCUMENTS_REQUIRED) | Some(UNDERGOING_PHYSICAL_CHECK) => needsDocuments(confirmation, messages)
      case _                                                                     => other(confirmation, messages)
    }

  def title(confirmation: Confirmation): String =
    confirmation.submission.flatMap(_.latestEnhancedStatus) match {
      case Some(RECEIVED)                                                        => "declaration.confirmation.received.title"
      case Some(GOODS_ARRIVED) | Some(GOODS_ARRIVED_MESSAGE)                     => "declaration.confirmation.accepted.title"
      case Some(CLEARED) if isArrived(confirmation)                              => "declaration.confirmation.cleared.title"
      case Some(ADDITIONAL_DOCUMENTS_REQUIRED) | Some(UNDERGOING_PHYSICAL_CHECK) => "declaration.confirmation.needsDocument.title"
      case _                                                                     => "declaration.confirmation.other.title"
    }

  private def accepted(implicit confirmation: Confirmation, messages: Messages): Html =
    new Html(List(panel, body, whatHappensNext, List(exitSurvey())).flatten)

  private def cleared(implicit confirmation: Confirmation, messages: Messages): Html =
    new Html(List(panel, bodyForCleared, List(exitSurvey())).flatten)

  private def isArrived(confirmation: Confirmation): Boolean =
    AdditionalDeclarationType.isArrived(from(confirmation.declarationType))

  private def needsDocuments(implicit confirmation: Confirmation, messages: Messages): Html = {
    val title = pageTitle(messages("declaration.confirmation.needsDocument.title"))
    val warning = govukWarningText(
      WarningText(iconFallbackText = messages("site.warning"), content = Text(messages("declaration.confirmation.needsDocument.warning")))
    )

    new Html(List(title, warning, body1, body2))
  }

  private def other(implicit confirmation: Confirmation, messages: Messages): Html = {
    val title = pageTitle(messages("declaration.confirmation.other.title"))
    val body1 = paragraph(
      messages(
        s"declaration.confirmation.other.body.1",
        confirmation.submission.flatMap(_.ducr).fold("")(ducr => s" ${messages("declaration.confirmation.body.1.ducr", toBold(ducr))}"),
        confirmation.submission.map(_.lrn).fold("")(lrn => s" ${messages("declaration.confirmation.body.1.lrn", toBold(lrn))}"),
        link(messages("declaration.confirmation.other.body.1.link"), SubmissionsController.displayListOfSubmissions())
      )
    )
    val body2 = paragraph(messages("declaration.confirmation.other.body.2"))

    new Html(List(title, body1, body2))
  }

  private def received(implicit confirmation: Confirmation, messages: Messages): Html =
    new Html(List(panel, List(body1), whatHappensNext, whatYouCanDoNow, List(exitSurvey())).flatten)

  private def body(implicit confirmation: Confirmation, messages: Messages): List[Html] =
    List(body1, body2)

  private def body1(implicit confirmation: Confirmation, messages: Messages): Html =
    paragraph(
      messages(
        "declaration.confirmation.body.1",
        confirmation.submission.flatMap(_.ducr).fold("")(ducr => s" ${messages("declaration.confirmation.body.1.ducr", toBold(ducr))}"),
        confirmation.submission.map(_.lrn).fold("")(lrn => s" ${messages("declaration.confirmation.body.1.lrn", toBold(lrn))}"),
        confirmation.submission.flatMap(_.mrn).getOrElse("")
      )
    )

  private def body2(implicit confirmation: Confirmation, messages: Messages): Html =
    paragraph(
      messages(
        s"declaration.confirmation${docOrCtl}.body.2",
        link(messages("declaration.confirmation.declaration.details.link"), declarationDetailsRoute)
      )
    )

  private def bodyForCleared(implicit confirmation: Confirmation, messages: Messages): List[Html] = {
    val body1 = paragraph(
      messages(
        "declaration.confirmation.cleared.body.1",
        confirmation.submission.flatMap(_.ducr).fold("")(ducr => s" ${messages("declaration.confirmation.body.1.ducr", toBold(ducr))}"),
        confirmation.submission.map(_.lrn).fold("")(lrn => s" ${messages("declaration.confirmation.body.1.lrn", toBold(lrn))}")
      )
    )

    val body2 = paragraph(
      messages(
        "declaration.confirmation.cleared.body.2",
        link(messages("declaration.confirmation.declaration.details.link"), declarationDetailsRoute)
      )
    )

    val body3 = paragraph(messages(s"declaration.confirmation.cleared.body.3"))

    List(body1, heading(messages("declaration.confirmation.cleared.heading"), "govuk-heading-m", "h2"), body2, body3)
  }

  private def panel(implicit confirmation: Confirmation, messages: Messages): List[Html] = {
    val mrn = confirmation.submission.flatMap(_.mrn).getOrElse("")
    List(
      govukPanel(
        Panel(title = Text(messages(s"declaration.confirmation.$status.title")), content = HtmlContent(messages("declaration.confirmation.mrn", mrn)))
      )
    )
  }

  private def whatHappensNext(implicit confirmation: Confirmation, messages: Messages): List[Html] = {
    val next1 = paragraph(
      messages(
        s"""declaration.confirmation.$accOrRcv.next.1""",
        s"""<span class="govuk-!-font-weight-bold">${confirmation.email}</span>""",
        link(messages("declaration.confirmation.declaration.details.link"), declarationDetailsRoute)
      )
    )

    val acceptanceTime = confirmation.submission.flatMap { submission =>
      if (submission.latestEnhancedStatus == Some(RECEIVED)) None
      else submission.enhancedStatusLastUpdated.map(formatTimeDate(_))
    }

    val next2Args =
      List(acceptanceTime, Some(link(messages("declaration.confirmation.next.2.link"), Call("GET", appConfig.nationalClearanceHub)))).flatten

    val next2 = paragraph(messages(s"declaration.confirmation.$accOrRcv.next.2", next2Args: _*))

    List(heading(messages("declaration.confirmation.what.happens.next"), "govuk-heading-m", "h2"), next1, next2)
  }

  private def whatYouCanDoNow(implicit confirmation: Confirmation, messages: Messages): List[Html] = {
    val mrn = confirmation.submission.flatMap(_.mrn).getOrElse("")
    val title = heading(messages("declaration.confirmation.whatYouCanDoNow.heading"), "govuk-heading-m", "h2")
    val paragraph1 = body2
    val paragraph2 = paragraph(
      messages(
        "declaration.confirmation.whatYouCanDoNow.paragraph.2",
        externalLink(messages("declaration.confirmation.whatYouCanDoNow.paragraph.2.link"), FileUploadController.startFileUpload(mrn).url)
      )
    )

    List(title, paragraph1, paragraph2)
  }

  private def accOrRcv(implicit confirmation: Confirmation): String =
    confirmation.submission.flatMap(_.latestEnhancedStatus) match {
      case Some(RECEIVED) => "received"
      case _              => "accepted"
    }

  private def docOrCtl(implicit confirmation: Confirmation): String =
    confirmation.submission.flatMap(_.latestEnhancedStatus) match {
      case Some(ADDITIONAL_DOCUMENTS_REQUIRED) | Some(UNDERGOING_PHYSICAL_CHECK) => ".needsDocument"
      case _                                                                     => ""
    }

  private def declarationDetailsRoute(implicit confirmation: Confirmation): Call =
    DeclarationDetailsController.displayPage(confirmation.submission.map(_.uuid).getOrElse(""))

  private def status(implicit confirmation: Confirmation): String =
    confirmation.submission.flatMap(_.latestEnhancedStatus) match {
      case Some(RECEIVED) => "received"
      case Some(CLEARED)  => "cleared"
      case _              => "accepted"
    }

  private def toBold(value: String): String =
    s"""<span class="govuk-!-font-weight-bold">${value}</span>"""
}
