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

package models.viewmodels.errors

import connectors.CodeListConnector
import controllers.section5.routes.ProcedureCodesController
import models.Pointer
import models.declaration.errors.ErrorInstance
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import views.html.components.gds.link

object CDS12119Interpreter extends ErrorInterpreter {
  def generateHtmlFor(error: ErrorInstance)(implicit messages: Messages, codeListConnector: CodeListConnector, link: link): Option[Html] = {

    val rows = error.fieldsInvolved.map { field =>
      val fieldName = messages(field.pointer.messageKey, field.pointer.sequenceArgs: _*)
      val description = field.description.getOrElse("")

      generateFieldTableRow(field.pointer, fieldName, description, field.changeLink)
    }

    val itemIdx = error.fieldsInvolved.headOption.flatMap(_.pointer.sequenceIndexes.headOption).getOrElse(0)
    val pointer = Pointer(s"declaration.items.$itemIdx.procedureCodes.procedureCode.current")

    val changeLink = errorChangeAction(
      ProcedureCodesController.displayPage(error.sourceDec.items(itemIdx).id),
      error.errorCode,
      Some(pointer),
      error.sourceDec.id,
      error.isAmendmentError
    )

    Some(
      HtmlContent(
        HtmlFormat.fill(
          List(
            errorHeader,
            errorTitle(error),
            contentHeader,
            HtmlFormat.fill(formattedErrorDescription(error.errorCode)),
            tableHeader(pointer, changeLink),
            HtmlFormat.fill(rows),
            fieldsTableFooter,
            errorFooter
          )
        )
      ).asHtml
    )
  }

  def tableHeader(fieldPointer: Pointer, changeLink: Html)(implicit messages: Messages) = Html(s"""<table class="govuk-table">
       |  <thead class="govuk-table__head">
       |    <tr class="govuk-table__row">
       |      <th scope="col" class="govuk-table__header"></th>
       |      <th scope="col" class="govuk-table__header" data-gtm-vis-has-fired-8267218_2200="1">${messages(
    "rejected.notification.description.heading"
  )}</th>
       |      <th scope="col" class="govuk-table__header" data-gtm-vis-has-fired-8267218_2200="1">${messages("site.change.header")}</th>
       |    </tr>
       |  </thead>
       |  <tbody class="govuk-table__body">
       |  <tr class="govuk-table__row ${fieldPointer.toString.replaceAll("\\.#?", "-")}">
       |      <td class="govuk-table__cell govuk-table__cell_break-word bold">${messages("declaration.summary.item.procedureCode")}</td>
       |      <td class="govuk-table__cell govuk-table__cell_break-word"></td>
       |      <td class="govuk-table__cell govuk-table__cell_break-word">${changeLink}</td>
       |    </tr>
       |""".stripMargin)

  def generateFieldTableRow(fieldPointer: Pointer, fieldName: String, description: String, call: Option[Html]) = Html(
    s"""<tr class="govuk-table__row ${fieldPointer.toString.replaceAll("\\.#?", "-")}">
       |  <td class="govuk-table__cell govuk-table__cell_break-word bold">${fieldName}</td>
       |  <td class="govuk-table__cell govuk-table__cell_break-word">${description}</td>
       |  <td class="govuk-table__cell govuk-table__cell_break-word">${call.getOrElse("")}</td>
       |</tr>
       |""".stripMargin
  )
}
