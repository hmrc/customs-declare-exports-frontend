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

package views.helpers

import controllers.timeline.routes.DeclarationDetailsController
import forms.section1.AdditionalDeclarationType.{arrivedTypes, from, preLodgedTypes, SUPPLEMENTARY_EIDR, SUPPLEMENTARY_SIMPLIFIED}
import forms.section1.AdditionalDeclarationType
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.Submission
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, SummaryList, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukPanel, GovukSummaryList, GovukWarningText}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.panel.Panel
import uk.gov.hmrc.govukfrontend.views.viewmodels.warningtext.WarningText
import views.helpers.ConfirmationHelper.getConfirmationPageMessageKey
import views.html.components.print_page_button
import views.html.components.exit_survey
import views.html.components.gds._

import javax.inject.{Inject, Singleton}

case class Confirmation(email: String, declarationType: String, submission: Submission, locationCode: Option[String])

@Singleton
class ConfirmationHelper @Inject() (
  exitSurvey: exit_survey,
  govukPanel: GovukPanel,
  govukSummaryList: GovukSummaryList,
  govukWarningText: GovukWarningText,
  heading: heading,
  link: link,
  paragraph: paragraphBody
) {

  def content(confirmation: Confirmation)(implicit request: Request[_], messages: Messages): Html =
    confirmation.submission.latestEnhancedStatus match {
      case Some(RECEIVED) | Some(GOODS_ARRIVED) | Some(GOODS_ARRIVED_MESSAGE)    => submitted(request, confirmation, messages)
      case Some(CLEARED) if isArrived(confirmation)                              => submitted(request, confirmation, messages)
      case Some(ADDITIONAL_DOCUMENTS_REQUIRED) | Some(UNDERGOING_PHYSICAL_CHECK) => actionRequired(request, confirmation, messages)
      case _                                                                     => pendingNotification(request, confirmation, messages)
    }

  def title(confirmation: Confirmation): String =
    confirmation.submission.latestEnhancedStatus match {
      case Some(RECEIVED) | Some(GOODS_ARRIVED) | Some(GOODS_ARRIVED_MESSAGE)    => "declaration.confirmation.submitted.title"
      case Some(CLEARED) if isArrived(confirmation)                              => "declaration.confirmation.submitted.title"
      case Some(ADDITIONAL_DOCUMENTS_REQUIRED) | Some(UNDERGOING_PHYSICAL_CHECK) => "declaration.confirmation.actionRequired.title"
      case _                                                                     => "declaration.confirmation.pendingNotification.title"
    }

  private def submitted(implicit request: Request[_], confirmation: Confirmation, messages: Messages): Html = {
    def whatHappensNext(implicit messages: Messages): List[Html] = {
      val title = heading(messages("declaration.confirmation.whatHappensNext"), "govuk-heading-m", "h2")
      val body = paragraph(messages("declaration.confirmation.submitted.whatHappensNext.paragraph"))

      List(Some(title), Some(body)).flatten
    }

    def checkDetails(implicit confirmation: Confirmation, messages: Messages): List[Html] = {
      val title = heading(messages("declaration.confirmation.checkDetails.title"), "govuk-heading-m", "h2")
      val paragraph0 = paragraph(messages("declaration.confirmation.submitted.checkDetails.paragraph"))
      val link1 =
        link(text = messages("declaration.confirmation.checkDetails.link"), call = declarationDetailsRoute, classes = Some("govuk-link govuk-body"))

      List(Some(title), Some(paragraph0), Some(link1)).flatten
    }

    new Html(List(topSection, whatHappensNext, checkDetails, bottomSection).flatten)
  }

  private def isArrived(confirmation: Confirmation): Boolean =
    AdditionalDeclarationType.isArrived(from(confirmation.declarationType))

  private def actionRequired(implicit request: Request[_], confirmation: Confirmation, messages: Messages): Html = {
    val warning = govukWarningText(
      WarningText(iconFallbackText = Some(messages("site.warning")), content = Text(messages("declaration.confirmation.actionRequired.warning")))
    )

    val title = heading(messages("declaration.confirmation.whatHappensNext"), "govuk-heading-m", "h2")
    val body1 = paragraph(messages("declaration.confirmation.actionRequired.paragraph1"))
    val body2 = paragraph(messages("declaration.confirmation.actionRequired.paragraph2"))
    val link1 =
      link(text = messages("declaration.confirmation.checkDetails.link"), call = declarationDetailsRoute, classes = Some("govuk-link govuk-body"))

    new Html(topSection ::: List(title, warning, body1, body2, link1) ::: bottomSection)
  }

  private def pendingNotification(implicit request: Request[_], confirmation: Confirmation, messages: Messages): Html = {
    val title = heading(messages("declaration.confirmation.whatHappensNext"), "govuk-heading-m", "h2")
    val body1 = paragraph(messages("declaration.confirmation.pendingNotification.paragraph1"))
    val body2 = paragraph(messages("declaration.confirmation.pendingNotification.paragraph2"))
    val link1 =
      link(text = messages("declaration.confirmation.checkDetails.link"), call = declarationDetailsRoute, classes = Some("govuk-link govuk-body"))

    new Html(topSection ::: List(title, body1, body2, link1) ::: bottomSection)
  }

  private def topSection(implicit confirmation: Confirmation, messages: Messages): List[Html] = List(panel(title(confirmation)), summaryList)
  private def bottomSection(implicit request: Request[_], messages: Messages): List[Html] = List(print_page_button(8, 4), sectionBreak, exitSurvey())

  private def summaryList(implicit confirmation: Confirmation, messages: Messages): Html =
    govukSummaryList(
      SummaryList(
        classes = "govuk-!-margin-bottom-6",
        rows = Seq(
          Some(
            SummaryListRow(
              key = Key(content = Text(messages("declaration.confirmation.additionalType"))),
              value = Value(content = Text(messages(getConfirmationPageMessageKey(confirmation.declarationType))))
            )
          ),
          confirmation.submission.ducr.map { ducr =>
            SummaryListRow(key = Key(content = Text(messages("declaration.confirmation.ducr"))), value = Value(content = Text(ducr)))
          },
          Some(
            SummaryListRow(
              key = Key(content = Text(messages("declaration.confirmation.lrn"))),
              value = Value(content = Text(confirmation.submission.lrn))
            )
          ),
          confirmation.submission.mrn.map { mrn =>
            SummaryListRow(key = Key(content = Text(messages("declaration.confirmation.mrn"))), value = Value(content = Text(mrn)))
          }
        ).flatten
      )
    )

  private def panel(title: String)(implicit messages: Messages): Html =
    if (title == "declaration.confirmation.submitted.title")
      govukPanel(Panel(classes = classForACs, title = Text(messages(title))))
    else
      govukPanel(Panel(classes = classForACs, title = Text(messages(title)), attributes = Map("style" -> "background: #f3f2f1; color: #0b0c0c;")))

  private def declarationDetailsRoute(implicit confirmation: Confirmation): Call =
    DeclarationDetailsController.displayPage(confirmation.submission.uuid)

  private val sectionBreak = Html(s"""<hr class="govuk-section-break govuk-section-break--l govuk-section-break--visible">""")

  // Used in the Acceptance tests as common identifier for the different versions of the
  // confirmation page while waiting to switch from the holding page to the confirmation page.
  private val classForACs = "confirmation-content"
}

object ConfirmationHelper {

  val js = "js"
  val Disabled = "disabled"
  val Enabled = "enabled"

  def getConfirmationPageMessageKey(declarationType: String): String = {
    val decType = from(declarationType) match {
      case Some(declarationType) if preLodgedTypes.contains(declarationType) => "prelodged"
      case Some(declarationType) if arrivedTypes.contains(declarationType)   => "arrived"
      case Some(SUPPLEMENTARY_EIDR)                                          => "eidr"
      case Some(SUPPLEMENTARY_SIMPLIFIED)                                    => "simplified"
    }
    s"declaration.confirmation.$decType"
  }
}
