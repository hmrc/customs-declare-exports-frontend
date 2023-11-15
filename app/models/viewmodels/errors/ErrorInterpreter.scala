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

package models.viewmodels.errors

import connectors.CodeListConnector
import controllers.routes.SubmissionsController
import models.declaration.errors.ErrorInstance
import models.Pointer
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import views.html.components.gds.link

import scala.collection.immutable

trait ErrorInterpreter {
  def generateHtmlFor(error: ErrorInstance)(implicit messages: Messages, codeListConnector: CodeListConnector, link: link): Option[Html]

  def formattedErrorDescription(validationCode: String)(implicit messages: Messages, codeListConnector: CodeListConnector): List[Html] = {
    val description = codeListConnector
      .getDmsErrorCodesMap(messages.lang.toLocale)
      .get(validationCode)
      .map(dmsErrorCode => dmsErrorCode.description)
      .getOrElse(messages("error.unknown"))

    if (description.trim.isEmpty)
      List(HtmlFormat.empty)
    else {
      val desc = description.last match {
        case '.' | '?' | '!'           => description
        case _ if description.nonEmpty => s"$description."
        case _                         => ""
      }

      desc
        .split("\n")
        .toList
        .flatMap(text => List(Text(text).asHtml, Html("<br>")))
        .filter(_.body.nonEmpty)
        .dropRight(1)
    }
  }

  def fieldsTableHeader(implicit messages: Messages) = Html(s"""<table class="govuk-table">
      |  <thead class="govuk-table__head">
      |    <tr class="govuk-table__row">
      |      <th scope="col" class="govuk-table__header"></th>
      |      <th scope="col" class="govuk-table__header" data-gtm-vis-has-fired-8267218_2200="1">${messages(
    "rejected.notification.fieldTable.column.2.title"
  )}</th>
      |      <th scope="col" class="govuk-table__header" data-gtm-vis-has-fired-8267218_2200="1">${messages(
    "rejected.notification.fieldTable.column.3.title"
  )}</th>
      |      <th scope="col" class="govuk-table__header" data-gtm-vis-has-fired-8267218_2200="1"></th>
      |    </tr>
      |  </thead>
      |  <tbody class="govuk-table__body">
      |""".stripMargin)

  def generateFieldTableRow(fieldName: String, originalValue: Option[String], updatedValue: Option[String], call: Option[Html]) =
    s"""<tr class="govuk-table__row declaration-transport-meansOfTransportOnDepartureIDNumber">
       |  <td class="govuk-table__cell govuk-table__cell_break-word bold">${fieldName}</td>
       |  <td class="govuk-table__cell govuk-table__cell_break-word">${originalValue.getOrElse("-")}</td>
       |  <td class="govuk-table__cell govuk-table__cell_break-word">${updatedValue.getOrElse("-")}</td>
       |  <td class="govuk-table__cell govuk-table__cell_break-word">${call.getOrElse("")}</td>
       |</tr>
       |""".stripMargin

  val fieldsTableFooter = Html("""</tbody>
                            |</table>""".stripMargin)

  val errorHeader = Html("""<div class="govuk-summary-card">""")
  val contentHeader = Html("""<div class="govuk-summary-card__content"> <dl class="govuk-summary-list govuk-!-margin-bottom-3">""")
  val errorFooter = Html("</dl></div></div>")

  def generateFieldTable(error: ErrorInstance)(implicit messages: Messages): Seq[Html] =
    error.fieldsInvolved.map { field =>
      val fieldName = messages(field.pointer.messageKey, field.pointer.sequenceArgs: _*)
      Html(generateFieldTableRow(fieldName, field.originalValue, field.draftValue, field.changeLink))
    }

  def errorTitle(error: ErrorInstance)(implicit messages: Messages) = Html(s"""<div class="govuk-summary-card__title-wrapper">
       |<h3 class="govuk-summary-card__title">Error ${error.seqNbr} - ${messages(
    s"dmsError.${error.errorCode}.title"
  )} (${error.errorCode}) </h3></div>""".stripMargin)

  def errorChangeAction(call: Call, validationCode: String, pointer: Option[Pointer], decId: String, isAmendment: Boolean)(
    implicit messages: Messages,
    link: link
  ): Html = {
    val errorPattern = pointer.map(_.pattern).getOrElse("none")
    val errorMessage = messages(s"dmsError.${validationCode}.title")
    val url = SubmissionsController.amendErrors(decId, errorPattern, errorMessage, isAmendment, RedirectUrl(call.url)).url

    link(text = messages("site.change"), call = Call("GET", url), id = Some("item-header-action"))
  }
}

object DefaultInterpreter extends ErrorInterpreter {
  def generateHtmlFor(error: ErrorInstance)(implicit messages: Messages, codeListConnector: CodeListConnector, link: link): Option[Html] = {

    val specificErrorContent = error.errorCode match {
      case "CDS12062" => CDS12062Interpreter.generateHtmlFor(error)
      case "CDS12119" => CDS12119Interpreter.generateHtmlFor(error)
      case _          => defaultContent(error)
    }

    Some(specificErrorContent.getOrElse(HtmlFormat.empty))
  }

  private def defaultContent(error: ErrorInstance)(implicit messages: Messages, codeListConnector: CodeListConnector): Option[Html] = {

    val fieldRows = generateFieldTable(error)
    val fieldTable = if (fieldRows.isEmpty) Seq(HtmlFormat.empty) else fieldsTableHeader +: fieldRows :+ fieldsTableFooter

    Some(
      HtmlContent(
        HtmlFormat.fill(
          immutable.Seq(
            errorHeader,
            errorTitle(error),
            contentHeader,
            HtmlFormat.fill(formattedErrorDescription(error.errorCode)),
            HtmlFormat.fill(fieldTable),
            errorFooter
          )
        )
      ).asHtml
    )
  }
}
