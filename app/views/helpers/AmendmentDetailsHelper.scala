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

import controllers.helpers.AmendmentInstance
import forms.section4.{NatureOfTransaction, PreviousDocumentsData}
import forms.section6._
import models.ExportsDeclaration
import models.declaration._
import play.api.Logging
import play.api.i18n.Messages
import play.twirl.api.Html
import views.helpers.AmendmentDetailsHelper._
import views.helpers.summary.SummaryHelper.classes

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

class AmendmentDetailsHelper extends Logging {

  def dateOfAmendment(timestamp: ZonedDateTime)(implicit messages: Messages): Html =
    Html(s"""
         |<h2 class="govuk-heading-s govuk-!-margin-top-6 govuk-!-margin-bottom-0">${messages("amendment.details.last.updated")}</h2>
         |<time class="govuk-body date-of-amendment" datetime="${ISO_OFFSET_DATE_TIME.format(timestamp)}">
         |  ${ViewDates.formatDateAtTime(timestamp)}
         |</time>
         |""".stripMargin)

  def reasonForAmend(reason: String)(implicit messages: Messages): Html =
    Html(s"""
         |<h2 class="govuk-heading-s govuk-!-margin-top-4 govuk-!-margin-bottom-0">${messages("amendment.details.reason.amendment")}</h2>
         |<span class="govuk-body reason-of-amendment">$reason</span>
         |""".stripMargin)

  def headingOfAmendments(implicit messages: Messages): Html =
    Html(s"""
         |<h2 class="govuk-heading-s govuk-!-margin-top-4 govuk-!-margin-bottom-3">${messages("amendment.details.heading.lists")}</h2>
         |""".stripMargin)

  private case class Section(id: String, amendmentFields: Seq[AmendmentInstance], sequenceId: String = "")

  def amendments(amendmentRows: Seq[AmendmentInstance])(implicit messages: Messages): Html =
    new Html(
      (sectionParties(amendmentRows) ++
        sectionRoutesAndLocations(amendmentRows) ++
        sectionTransaction(amendmentRows) ++
        sectionItems(amendmentRows) ++
        sectionTransport(amendmentRows))
        .filterNot(_.amendmentFields.isEmpty)
        .zipWithIndex
        .map { case (section, index) => cardOfAmendments(section, index == 0) }
    )

  private def section(sectionId: String, amendmentRows: Seq[AmendmentInstance]): Seq[Section] =
    List(Section(sectionId, amendmentRows.filter(_.pointer.pattern.startsWith(sectionId))))

  private def sectionParties(amendmentRows: Seq[AmendmentInstance]): Seq[Section] =
    section(parties, amendmentRows)

  private def sectionItems(amendmentRows: Seq[AmendmentInstance]): Seq[Section] =
    amendmentRows
      .filter(_.pointer.pattern.startsWith(items))
      .groupBy(_.pointer.sequenceArgs(0))
      .toList
      .sortBy(_._1)
      .map { itemDiffs =>
        Section(items, itemDiffs._2, itemDiffs._1.split('.').head)
      }

  // private lazy val routesAndLocationsIds = List(destinationCountryPointer, routingCountriesPointer, GoodsLocation.pointer, OfficeOfExit.pointerBase)

  private def sectionRoutesAndLocations(amendmentRows: Seq[AmendmentInstance]): Seq[Section] = {
    val alteredFields =
      amendmentRows.filter { ai =>
        val pattern = ai.pointer.pattern
        pattern.startsWith(locations) && !(pattern.contains("inlandModeOfTransportCode") || pattern.contains("warehouseIdentification"))
      }.filter { instance =>
        instance.originalValue.isDefined || instance.amendedValue.isDefined
      }

    List(Section(locations, alteredFields))
  }

  private lazy val transactionIds = List(InvoiceAndPackageTotals.pointer, NatureOfTransaction.pointerBase, PreviousDocumentsData.pointer)

  private def sectionTransaction(amendmentRows: Seq[AmendmentInstance]): Seq[Section] = {
    val alteredFields =
      amendmentRows.filter { instance =>
        val parts = instance.pointer.pattern.split('.')
        parts.length > 1 && transactionIds.contains(parts(1))
      }

    List(Section(transaction, alteredFields))
  }

  private lazy val transportIds = List(inlandModeOfTransport, supervisingCustomsOffice, warehouseIdentification)

  private def sectionTransport(amendmentRows: Seq[AmendmentInstance]): Seq[Section] = {
    val alteredFields =
      amendmentRows
        .filter(ai => ai.pointer.pattern.startsWith(transport) || transportIds.exists(ai.pointer.pattern.startsWith))

    List(Section(transport, alteredFields))
  }

  private def cardOfAmendments(section: Section, isFirstSection: Boolean)(implicit messages: Messages): Html =
    Html(s"""
         |<div class="govuk-summary-card${if (isFirstSection) " govuk-summary-card-margin-top-0" else ""}">
         |  <div class="govuk-summary-card__title-wrapper">
         |    <h2 class="govuk-summary-card__title ${classes(classMappings(section.id))}-card">
         |      ${messages(h2Mappings(section.id), section.sequenceId)}
         |    </h2>
         |  </div>
         |  <div class="govuk-summary-card__content">
         |    ${tableOfAmendments(section.amendmentFields)}
         |  </div>
         |</div>
         |""".stripMargin)

  private def tableOfAmendments(alteredFields: Seq[AmendmentInstance])(implicit messages: Messages): String =
    s"""
       |<table class="govuk-table">
       |  <thead class="govuk-table__head">
       |    <tr class="govuk-table__row">
       |      <th scope="col" class="govuk-table__header">${messages("amendment.details.description")}</th>
       |      <th scope="col" class="govuk-table__header">${messages("amendment.details.previous.value")}</th>
       |      <th scope="col" class="govuk-table__header">${messages("amendment.details.amended.value")}</th>
       |    </tr>
       |  </thead>
       |  <tbody class="govuk-table__body">
       |    ${alteredFields.map(getIndividualRow).mkString}
       |  </tbody>
       |</table>
       |""".stripMargin

  private def getIndividualRow(amendmentInstance: AmendmentInstance)(implicit messages: Messages): String =
    s"""<tr class="govuk-table__row ${amendmentInstance.pointer.pattern.replaceAll("\\.#?", "-")}">
       |  <td class="govuk-table__cell govuk-table__cell_break-word">${messages(amendmentInstance.fieldId)}</th>
       |  <td class="govuk-table__cell govuk-table__cell_break-word">${amendmentInstance.originalValue.getOrElse("")}</td>
       |  <td class="govuk-table__cell govuk-table__cell_break-word">${amendmentInstance.amendedValue.getOrElse("")}</td>
       |</tr>""".stripMargin
}

object AmendmentDetailsHelper {

  private val summary = "declaration.summary"

  private val declaration = ExportsDeclaration.pointer
  private val items = s"$declaration.${ExportItem.pointer}"
  private val locations = s"$declaration.${Locations.pointer}"
  private val parties = s"$declaration.${Parties.pointer}"
  private val transaction = "transaction"
  private val transport = s"$declaration.${Transport.pointer}"

  private val inlandModeOfTransport = s"$locations.${InlandModeOfTransportCode.pointer}"
  private val supervisingCustomsOffice = s"$locations.${SupervisingCustomsOffice.pointer}"
  private val warehouseIdentification = s"$locations.${WarehouseIdentification.pointer}"

  private val h2Mappings = Map(
    parties -> s"$summary.section.2",
    locations -> s"$summary.section.3",
    transaction -> s"$summary.section.4",
    items -> s"$summary.section.5.item",
    transport -> s"$summary.section.6"
  )

  private val classMappings = Map(parties -> 1, locations -> 2, transaction -> 3, items -> 4, transport -> 5)
}
