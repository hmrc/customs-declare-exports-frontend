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

package forms.section4

import forms.common.DeclarationPageBaseSpec
import models.declaration.InvoiceAndPackageTotals
import org.scalatest.OptionValues
import services.AlteredField
import services.AlteredField.constructAlteredField

class InvoiceAndPackageTotalsSpec extends DeclarationPageBaseSpec with OptionValues {

  "InvoiceAndPackageTotals.createDiff" should {
    val baseFieldPointer = InvoiceAndPackageTotals.pointer
    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val noOfItems = InvoiceAndPackageTotals()
        noOfItems.createDiff(noOfItems, InvoiceAndPackageTotals.pointer) mustBe Seq.empty[AlteredField]
      }

      "the original version's totalAmountInvoiced field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${InvoiceAndPackageTotals.totalAmountInvoicedPointer}"
        withClue("both versions have Some values but values are different") {
          val totalNumberOfItems = InvoiceAndPackageTotals(totalAmountInvoiced = Some("latest"))
          val originalValue = "original"
          totalNumberOfItems.createDiff(
            totalNumberOfItems.copy(totalAmountInvoiced = Some(originalValue)),
            InvoiceAndPackageTotals.pointer
          ) mustBe Seq(constructAlteredField(fieldPointer, originalValue, totalNumberOfItems.totalAmountInvoiced.get))
        }

        withClue("the original version's totalAmountInvoiced field is None but this one has Some value") {
          val totalNumberOfItems = InvoiceAndPackageTotals(totalAmountInvoiced = Some("latest"))
          val originalValue = None
          totalNumberOfItems.createDiff(totalNumberOfItems.copy(totalAmountInvoiced = originalValue), InvoiceAndPackageTotals.pointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, totalNumberOfItems.totalAmountInvoiced)
          )
        }

        withClue("the original version's totalAmountInvoiced field is Some but this one has None as its value") {
          val totalNumberOfItems = InvoiceAndPackageTotals(totalAmountInvoiced = None)
          val originalValue = "original"
          totalNumberOfItems.createDiff(
            totalNumberOfItems.copy(totalAmountInvoiced = Some(originalValue)),
            InvoiceAndPackageTotals.pointer
          ) mustBe Seq(constructAlteredField(fieldPointer, Some(originalValue), totalNumberOfItems.totalAmountInvoiced))
        }

        withClue("both versions have None values") {
          val totalNumberOfItems = InvoiceAndPackageTotals(totalAmountInvoiced = None)
          totalNumberOfItems.createDiff(totalNumberOfItems, InvoiceAndPackageTotals.pointer) mustBe Seq.empty
        }
      }

      "the original version's totalAmountInvoicedCurrency field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${InvoiceAndPackageTotals.totalAmountInvoicedCurrencyPointer}"
        withClue("both versions have Some values but values are different") {
          val totalNumberOfItems = InvoiceAndPackageTotals(totalAmountInvoicedCurrency = Some("latest"))
          val originalValue = "original"
          totalNumberOfItems.createDiff(
            totalNumberOfItems.copy(totalAmountInvoicedCurrency = Some(originalValue)),
            InvoiceAndPackageTotals.pointer
          ) mustBe Seq(constructAlteredField(fieldPointer, originalValue, totalNumberOfItems.totalAmountInvoicedCurrency.get))
        }

        withClue("the original version's totalAmountInvoicedCurrency field is None but this one has Some value") {
          val totalNumberOfItems = InvoiceAndPackageTotals(totalAmountInvoicedCurrency = Some("latest"))
          val originalValue = None
          totalNumberOfItems.createDiff(
            totalNumberOfItems.copy(totalAmountInvoicedCurrency = originalValue),
            InvoiceAndPackageTotals.pointer
          ) mustBe Seq(constructAlteredField(fieldPointer, originalValue, totalNumberOfItems.totalAmountInvoicedCurrency))
        }

        withClue("the original version's totalAmountInvoicedCurrency field is Some but this one has None as its value") {
          val totalNumberOfItems = InvoiceAndPackageTotals(totalAmountInvoicedCurrency = None)
          val originalValue = "original"
          totalNumberOfItems.createDiff(
            totalNumberOfItems.copy(totalAmountInvoicedCurrency = Some(originalValue)),
            InvoiceAndPackageTotals.pointer
          ) mustBe Seq(constructAlteredField(fieldPointer, Some(originalValue), totalNumberOfItems.totalAmountInvoicedCurrency))
        }

        withClue("both versions have None values") {
          val totalNumberOfItems = InvoiceAndPackageTotals(totalAmountInvoicedCurrency = None)
          totalNumberOfItems.createDiff(totalNumberOfItems, InvoiceAndPackageTotals.pointer) mustBe Seq.empty
        }
      }

      "the original version's exchangeRate field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${InvoiceAndPackageTotals.exchangeRatePointer}"
        withClue("both versions have Some values but values are different") {
          val totalNumberOfItems = InvoiceAndPackageTotals(exchangeRate = Some("latest"))
          val originalValue = "original"
          totalNumberOfItems.createDiff(totalNumberOfItems.copy(exchangeRate = Some(originalValue)), InvoiceAndPackageTotals.pointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, totalNumberOfItems.exchangeRate.get)
          )
        }

        withClue("the original version's exchangeRate field is None but this one has Some value") {
          val totalNumberOfItems = InvoiceAndPackageTotals(exchangeRate = Some("latest"))
          val originalValue = None
          totalNumberOfItems.createDiff(totalNumberOfItems.copy(exchangeRate = originalValue), InvoiceAndPackageTotals.pointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, totalNumberOfItems.exchangeRate)
          )
        }

        withClue("the original version's exchangeRate field is Some but this one has None as its value") {
          val totalNumberOfItems = InvoiceAndPackageTotals(exchangeRate = None)
          val originalValue = "original"
          totalNumberOfItems.createDiff(totalNumberOfItems.copy(exchangeRate = Some(originalValue)), InvoiceAndPackageTotals.pointer) mustBe Seq(
            constructAlteredField(fieldPointer, Some(originalValue), totalNumberOfItems.exchangeRate)
          )
        }

        withClue("both versions have None values") {
          val totalNumberOfItems = InvoiceAndPackageTotals(exchangeRate = None)
          totalNumberOfItems.createDiff(totalNumberOfItems, InvoiceAndPackageTotals.pointer) mustBe Seq.empty
        }
      }

      "the original version's totalPackage field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${InvoiceAndPackageTotals.totalPackagePointer}"
        withClue("both versions have Some values but values are different") {
          val totalNumberOfItems = InvoiceAndPackageTotals(totalPackage = Some("latest"))
          val originalValue = "original"
          totalNumberOfItems.createDiff(totalNumberOfItems.copy(totalPackage = Some(originalValue)), InvoiceAndPackageTotals.pointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, totalNumberOfItems.totalPackage.get)
          )
        }

        withClue("the original version's totalPackage field is None but this one has Some value") {
          val totalNumberOfItems = InvoiceAndPackageTotals(totalPackage = Some("latest"))
          val originalValue = None
          totalNumberOfItems.createDiff(totalNumberOfItems.copy(totalPackage = originalValue), InvoiceAndPackageTotals.pointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, totalNumberOfItems.totalPackage)
          )
        }

        withClue("the original version's totalPackage field is Some but this one has None as its value") {
          val totalNumberOfItems = InvoiceAndPackageTotals(totalPackage = None)
          val originalValue = "original"
          totalNumberOfItems.createDiff(totalNumberOfItems.copy(totalPackage = Some(originalValue)), InvoiceAndPackageTotals.pointer) mustBe Seq(
            constructAlteredField(fieldPointer, Some(originalValue), totalNumberOfItems.totalPackage)
          )
        }

        withClue("both versions have None values") {
          val totalNumberOfItems = InvoiceAndPackageTotals(totalPackage = None)
          totalNumberOfItems.createDiff(totalNumberOfItems, InvoiceAndPackageTotals.pointer) mustBe Seq.empty
        }
      }
    }
  }
}
