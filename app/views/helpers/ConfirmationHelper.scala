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

import config.{AppConfig, ExternalServicesConfig}
import controllers.routes.{DeclarationDetailsController, FileUploadController}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.from
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.Submission
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.TableRow
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukPanel, GovukTable, GovukWarningText}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.panel.Panel
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import uk.gov.hmrc.govukfrontend.views.viewmodels.warningtext.WarningText
import views.dashboard.DashboardHelper.toDashboard
import views.helpers.ViewDates.formatTimeDate
import views.html.components.buttons.print_page_button
import views.html.components.exit_survey
import views.html.components.gds._

import javax.inject.{Inject, Singleton}

case class Confirmation(email: String, declarationType: String, submission: Option[Submission], locationCode: Option[String])

@Singleton
class ConfirmationHelper @Inject() (
  appConfig: AppConfig,
  externalServicesConfig: ExternalServicesConfig,
  exitSurvey: exit_survey,
  govukPanel: GovukPanel,
  govukTable: GovukTable,
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

  private def received(implicit confirmation: Confirmation, messages: Messages): Html =
    new Html(List(topSection, whatHappensNext, whatYouCanDoNow, bottomSection).flatten)

  private def accepted(implicit confirmation: Confirmation, messages: Messages): Html =
    new Html(List(topSection, List(body2), whatHappensNext, whatYouCanDoNow, bottomSection).flatten)

  private def cleared(implicit confirmation: Confirmation, messages: Messages): Html =
    new Html(List(topSection, whatYouCanDoNow, bottomSection).flatten)

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
        link(messages("declaration.confirmation.other.body.1.link"), toDashboard)
      )
    )
    val body2 = paragraph(messages("declaration.confirmation.other.body.2"))

    new Html(List(title, body1, body2))
  }

  private def topSection(implicit confirmation: Confirmation, messages: Messages): List[Html] = List(panel, table)
  private def bottomSection(implicit messages: Messages) = List(print_page_button(8, 4), sectionBreak, exitSurvey())

  private def table(implicit confirmation: Confirmation, messages: Messages): Html =
    govukTable(
      Table(rows =
        Seq(
          confirmation.submission
            .flatMap(_.ducr)
            .map(ducr =>
              Seq(
                TableRow(content = Text(messages(s"declaration.confirmation.ducr")), classes = "govuk-!-font-weight-bold"),
                TableRow(content = Text(ducr))
              )
            ),
          confirmation.submission
            .map(_.lrn)
            .map(lrn =>
              Seq(
                TableRow(content = Text(messages(s"declaration.confirmation.lrn")), classes = "govuk-!-font-weight-bold"),
                TableRow(content = Text(lrn))
              )
            ),
          confirmation.submission
            .flatMap(_.mrn)
            .map(mrn =>
              Seq(
                TableRow(content = Text(messages(s"declaration.confirmation.mrn")), classes = "govuk-!-font-weight-bold"),
                TableRow(content = Text(mrn))
              )
            )
        ).flatten
      )
    )

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

  private def panel(implicit confirmation: Confirmation, messages: Messages): Html =
    govukPanel(Panel(title = Text(messages(s"declaration.confirmation.$status.title"))))

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
    val title = heading(messages("declaration.confirmation.whatYouCanDoNow.heading"), "govuk-heading-m", "h2")
    val nonGvmsParagraph = confirmation.locationCode
      .filterNot(_.endsWith("GVM"))
      .map(_ =>
        paragraph(
          message = messages(
            "declaration.confirmation.whatYouCanDoNow.nonGvms.paragraph",
            link(
              messages("declaration.confirmation.whatYouCanDoNow.nonGvms.paragraph.link.1"),
              Call("GET", externalServicesConfig.customsMovementsFrontendUrl)
            ),
            link(
              messages("declaration.confirmation.whatYouCanDoNow.nonGvms.paragraph.link.2"),
              Call("GET", externalServicesConfig.customsMovementsFrontendUrl)
            )
          ),
          id = Some("non-gvms-paragraph")
        )
      )

    confirmation.submission.flatMap(_.latestEnhancedStatus) match {
      case Some(RECEIVED) =>
        val mrn = confirmation.submission.flatMap(_.mrn).getOrElse("")
        val paragraph1 = body2
        val paragraph2 = paragraph(
          messages(
            "declaration.confirmation.whatYouCanDoNow.paragraph.2",
            externalLink(messages("declaration.confirmation.whatYouCanDoNow.paragraph.2.link"), FileUploadController.startFileUpload(mrn).url)
          )
        )

        List(Some(title), nonGvmsParagraph, Some(paragraph1), Some(paragraph2)).flatten
      case Some(GOODS_ARRIVED) | Some(GOODS_ARRIVED_MESSAGE) if nonGvmsParagraph.isDefined => List(Some(title), nonGvmsParagraph).flatten
      case Some(GOODS_ARRIVED) | Some(GOODS_ARRIVED_MESSAGE) => List.empty
      case Some(CLEARED) if isArrived(confirmation) =>
        val body1 = paragraph(
          messages(
            "declaration.confirmation.cleared.body.1",
            link(messages("declaration.confirmation.declaration.details.link"), declarationDetailsRoute)
          )
        )
        val body2 = paragraph(messages(s"declaration.confirmation.cleared.body.2"))

        List(Some(title), nonGvmsParagraph, Some(body1), Some(body2)).flatten
    }
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

  private val sectionBreak = Html(s"""<hr class="govuk-section-break govuk-section-break--l govuk-section-break--visible">""")
}
