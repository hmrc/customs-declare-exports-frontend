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

package connectors.exchange

import java.time.Instant

import forms.common.YesNoAnswer
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import models.DeclarationStatus.DeclarationStatus
import models.DeclarationType.DeclarationType
import models.ExportsDeclaration
import models.declaration._
import play.api.libs.json.{Format, Json}

case class TotalItemsExchange(
  totalAmountInvoiced: Option[String],
  totalAmountInvoicedCurrency: Option[String],
  exchangeRate: Option[String],
  totalPackage: Option[String]
)

object TotalItemsExchange {
  implicit val format: Format[TotalItemsExchange] = Json.format[TotalItemsExchange]
}

case class ExportsDeclarationExchange(
  id: Option[String] = None,
  status: DeclarationStatus,
  createdDateTime: Instant,
  updatedDateTime: Instant,
  sourceId: Option[String],
  `type`: DeclarationType,
  additionalDeclarationType: Option[AdditionalDeclarationType] = None,
  consignmentReferences: Option[ConsignmentReferences] = None,
  linkDucrToMucr: Option[YesNoAnswer] = None,
  mucr: Option[Mucr] = None,
  transport: Transport = Transport(),
  parties: Parties = Parties(),
  locations: Locations = Locations(),
  items: Seq[ExportItem] = Seq.empty[ExportItem],
  readyForSubmission: Boolean = false,
  totalNumberOfItems: Option[TotalItemsExchange] = None,
  previousDocuments: Option[PreviousDocumentsData] = None,
  natureOfTransaction: Option[NatureOfTransaction] = None
) {
  def toExportsDeclaration: ExportsDeclaration = ExportsDeclaration(
    id = id.get,
    status = status,
    createdDateTime = createdDateTime,
    updatedDateTime = updatedDateTime,
    sourceId = sourceId,
    `type` = `type`,
    additionalDeclarationType = additionalDeclarationType,
    consignmentReferences = consignmentReferences,
    linkDucrToMucr = linkDucrToMucr,
    mucr = mucr,
    transport = transport,
    parties = parties,
    locations = locations,
    items = items,
    readyForSubmission = readyForSubmission,
    totalNumberOfItems = totalNumberOfItems.flatMap { exchange =>
      (exchange.totalAmountInvoiced, exchange.totalAmountInvoicedCurrency, exchange.exchangeRate) match {
        case (None, None, None) => None
        case (totalAmountInvoiced, totalAmountInvoicedCurrency, exchangeRate) =>
          Some(TotalNumberOfItems(exchangeRate, totalAmountInvoiced, totalAmountInvoicedCurrency))
      }
    },
    totalPackageQuantity = totalNumberOfItems.map(exchange => TotalPackageQuantity(exchange.totalPackage)),
    previousDocuments = previousDocuments,
    natureOfTransaction = natureOfTransaction
  )
}

object ExportsDeclarationExchange {

  import play.api.libs.json._

  implicit val format: OFormat[ExportsDeclarationExchange] = Json.format[ExportsDeclarationExchange]

  private def buildDeclaration(declaration: ExportsDeclaration, idProvider: ExportsDeclaration => Option[String]): ExportsDeclarationExchange =
    ExportsDeclarationExchange(
      id = idProvider(declaration),
      status = declaration.status,
      createdDateTime = declaration.createdDateTime,
      updatedDateTime = declaration.updatedDateTime,
      sourceId = declaration.sourceId,
      `type` = declaration.`type`,
      additionalDeclarationType = declaration.additionalDeclarationType,
      consignmentReferences = declaration.consignmentReferences,
      linkDucrToMucr = declaration.linkDucrToMucr,
      mucr = declaration.mucr,
      transport = declaration.transport,
      parties = declaration.parties,
      locations = declaration.locations,
      items = declaration.items,
      readyForSubmission = declaration.readyForSubmission,
      totalNumberOfItems = if (declaration.totalNumberOfItems.isDefined || declaration.totalPackageQuantity.isDefined) {
        Some(
          TotalItemsExchange(
            declaration.totalNumberOfItems.flatMap(_.totalAmountInvoiced),
            declaration.totalNumberOfItems.flatMap(_.totalAmountInvoicedCurrency),
            declaration.totalNumberOfItems.flatMap(_.exchangeRate),
            declaration.totalPackageQuantity.flatMap(_.totalPackage)
          )
        )
      } else None,
      previousDocuments = declaration.previousDocuments,
      natureOfTransaction = declaration.natureOfTransaction
    )

  def apply(declaration: ExportsDeclaration): ExportsDeclarationExchange =
    buildDeclaration(declaration, declaration => Some(declaration.id))

  def withoutId(declaration: ExportsDeclaration): ExportsDeclarationExchange =
    buildDeclaration(declaration, _ => None)
}
