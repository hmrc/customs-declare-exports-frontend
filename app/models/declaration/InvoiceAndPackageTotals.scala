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

package models.declaration

import models.AmendmentRow.{forAddedValue, forRemovedValue}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.{AmendmentOp, FieldMapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}

case class InvoiceAndPackageTotals(
  totalAmountInvoiced: Option[String] = None,
  totalAmountInvoicedCurrency: Option[String] = None,
  agreedExchangeRate: Option[String] = None,
  exchangeRate: Option[String] = None,
  totalPackage: Option[String] = None
) extends DiffTools[InvoiceAndPackageTotals] with AmendmentOp {

  def createDiff(original: InvoiceAndPackageTotals, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(
        original.totalAmountInvoiced,
        totalAmountInvoiced,
        combinePointers(pointerString, InvoiceAndPackageTotals.totalAmountInvoicedPointer, sequenceId)
      ),
      compareStringDifference(
        original.totalAmountInvoicedCurrency,
        totalAmountInvoicedCurrency,
        combinePointers(pointerString, InvoiceAndPackageTotals.totalAmountInvoicedCurrencyPointer, sequenceId)
      ),
      compareStringDifference(
        original.exchangeRate,
        exchangeRate,
        combinePointers(pointerString, InvoiceAndPackageTotals.exchangeRatePointer, sequenceId)
      ),
      compareStringDifference(
        original.totalPackage,
        totalPackage,
        combinePointers(pointerString, InvoiceAndPackageTotals.totalPackagePointer, sequenceId)
      )
    ).flatten

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    totalAmountInvoiced.fold("")(forAddedValue(pointer, messages("declaration.summary.transaction.itemAmount"), _)) +
      totalAmountInvoicedCurrency.fold("")(forAddedValue(pointer, messages("declaration.summary.transaction.currencyCode"), _)) +
      exchangeRate.fold("")(forAddedValue(pointer, messages("declaration.summary.transaction.exchangeRate"), _)) +
      totalPackage.fold("")(forAddedValue(pointer, messages("declaration.summary.transaction.totalNoOfPackages"), _))

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    totalAmountInvoiced.fold("")(forRemovedValue(pointer, messages("declaration.summary.transaction.itemAmount"), _)) +
      totalAmountInvoicedCurrency.fold("")(forRemovedValue(pointer, messages("declaration.summary.transaction.currencyCode"), _)) +
      exchangeRate.fold("")(forRemovedValue(pointer, messages("declaration.summary.transaction.exchangeRate"), _)) +
      totalPackage.fold("")(forRemovedValue(pointer, messages("declaration.summary.transaction.totalNoOfPackages"), _))
}

object InvoiceAndPackageTotals extends FieldMapping {
  implicit val format: OFormat[InvoiceAndPackageTotals] = Json.format[InvoiceAndPackageTotals]

  val pointer: ExportsFieldPointer = "totalNumberOfItems"
  val totalAmountInvoicedPointer: ExportsFieldPointer = "totalAmountInvoiced"
  val totalAmountInvoicedCurrencyPointer: ExportsFieldPointer = "totalAmountInvoicedCurrency"
  val exchangeRatePointer: ExportsFieldPointer = "exchangeRate"
  val totalPackagePointer: ExportsFieldPointer = "totalPackage"
}
