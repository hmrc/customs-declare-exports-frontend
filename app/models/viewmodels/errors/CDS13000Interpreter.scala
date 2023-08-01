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
import models.declaration.errors.ErrorInstance
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import views.html.components.gds.link

import scala.collection.immutable

object CDS13000Interpreter extends ErrorInterpreter {
  def generateHtmlFor(error: ErrorInstance)(implicit messages: Messages, codeListConnector: CodeListConnector, link: link): Option[Html] = {

    val rows = error.fieldsInvolved.map { field =>
      val fieldName = messages(field.pointer.messageKey, field.pointer.sequenceArgs: _*)
      val description = field.description.getOrElse("")

      generateFieldTableRow(fieldName, description, field.changeLink)
    }

    Some(
      HtmlContent(
        HtmlFormat.fill(
          immutable.Seq(
            errorHeader,
            errorTitle(error),
            contentHeader,
            HtmlFormat.fill(formattedErrorDescription(error.errorCode)),
            fieldsTableHeader,
            HtmlFormat.fill(rows),
            fieldsTableFooter,
            errorFooter
          )
        )
      ).asHtml
    )
  }

  override def fieldsTableHeader(implicit messages: Messages) = Html(
    s"""<table class="govuk-table">
       |  <thead class="govuk-table__head">
       |    <tr class="govuk-table__row">
       |      <th scope="col" class="govuk-table__header"></th>
       |      <th scope="col" class="govuk-table__header" data-gtm-vis-has-fired-8267218_2200="1">Description</th>
       |      <th scope="col" class="govuk-table__header" data-gtm-vis-has-fired-8267218_2200="1">${messages("site.change.header")}</th>
       |    </tr>
       |  </thead>
       |  <tbody class="govuk-table__body">
       |""".stripMargin)

  def generateFieldTableRow(fieldName: String, description: String, call: Option[Html]) = Html(
    s"""<tr class="govuk-table__row declaration-transport-meansOfTransportOnDepartureIDNumber">
       |  <td class="govuk-table__cell govuk-table__cell_break-word bold">${fieldName}</td>
       |  <td class="govuk-table__cell govuk-table__cell_break-word">${description}</td>
       |  <td class="govuk-table__cell govuk-table__cell_break-word">${call.getOrElse("")}</td>
       |</tr>
       |""".stripMargin)
}
