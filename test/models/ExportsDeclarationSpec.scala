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

package models

import base.{MockTaggedCodes, UnitSpec}
import forms.common.YesNoAnswer
import forms.declaration.countries.Country
import forms.declaration.{CommodityDetails, Mucr, NatureOfTransaction, PreviousDocumentsData}
import models.declaration._
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar
import services.AlteredField.constructAlteredField
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}
import services.{AlteredField, OriginalAndNewValues}

class ExportsDeclarationSpec
    extends UnitSpec with ExportsDeclarationBuilder with ExportsItemBuilder with MockitoSugar with MockTaggedCodes with OptionValues {

  "Update Item" should {
    "preserve item sequence" in {
      val declaration = aDeclaration(withItems(2))

      declaration.items.map(_.sequenceId) must be(Seq(1, 2))

      val firstId = declaration.items.head.id

      val updatedDeclaration = declaration.updatedItem(firstId, item => item.copy(procedureCodes = Some(ProcedureCodesData(Some("code"), Seq.empty))))

      updatedDeclaration.items.map(_.sequenceId) must be(Seq(1, 2))
    }
  }

  "Exports Declaration" should {

    "return correct value for isDeclarantExports" when {

      "declarant is an exporter" in {
        val declaration = aDeclaration(withType(DeclarationType.OCCASIONAL), withDeclarantIsExporter())

        declaration.isDeclarantExporter mustBe true
      }

      "declarant is not an exporter" in {
        val declaration = aDeclaration(withType(DeclarationType.OCCASIONAL), withDeclarantIsExporter("No"))

        declaration.isDeclarantExporter mustBe false
      }

      "user didn't answer on this question" in {
        val declaration = aDeclaration(withType(DeclarationType.OCCASIONAL))

        declaration.isDeclarantExporter mustBe false
      }

      "have originationCountry hard-coded to 'GB'" in {
        val declaration = aDeclaration()
        declaration.locations.originationCountry.value mustBe Country.GB
      }
    }
  }

  "ExportsDeclaration on isCommodityCodeOfItemPrefixedWith" should {
    val chemicalCodes = Seq(28, 29, 38)

    "return true" when {
      "CommodityCode contains one of the prefixed arguments" in {
        val commodityDetails = CommodityDetails(Some("3800123456"), None)
        val item = anItem(withCommodityDetails(commodityDetails))
        val declaration = aDeclaration(withType(DeclarationType.STANDARD), withItem(item))

        declaration.isCommodityCodeOfItemPrefixedWith(item.id, chemicalCodes) mustBe true
      }
    }

    "return false" when {
      "no prefixed arguments are supplied" in {
        val commodityDetails = CommodityDetails(Some("3100123456"), None)
        val item = anItem(withCommodityDetails(commodityDetails))
        val declaration = aDeclaration(withType(DeclarationType.STANDARD), withItem(item))

        declaration.isCommodityCodeOfItemPrefixedWith(item.id, Seq.empty[Int]) mustBe false
      }

      "CommodityCode is not defined" in {
        val commodityDetails = CommodityDetails(None, None)
        val item = anItem(withCommodityDetails(commodityDetails))
        val declaration = aDeclaration(withType(DeclarationType.STANDARD), withItem(item))

        declaration.isCommodityCodeOfItemPrefixedWith(item.id, chemicalCodes) mustBe false
      }

      "CommodityCode is empty" in {
        val commodityDetails = CommodityDetails(Some(""), None)
        val item = anItem(withCommodityDetails(commodityDetails))
        val declaration = aDeclaration(withType(DeclarationType.STANDARD), withItem(item))

        declaration.isCommodityCodeOfItemPrefixedWith(item.id, chemicalCodes) mustBe false
      }

      "CommodityCode contains none of the prefixed arguments" in {
        val commodityDetails = CommodityDetails(Some("3100123456"), None)
        val item = anItem(withCommodityDetails(commodityDetails))
        val declaration = aDeclaration(withType(DeclarationType.STANDARD), withItem(item))

        declaration.isCommodityCodeOfItemPrefixedWith(item.id, chemicalCodes) mustBe false
      }
    }
  }

  "ExportsDeclaration.createDiff" should {
    val baseFieldPointer = ExportsDeclaration.pointer
    val goodsItemQuantityFieldPointer = s"$baseFieldPointer.${ExportsDeclaration.goodsItemQuantityPointer}"

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val declaration = aDeclaration()
        declaration.createDiff(declaration) mustBe Seq.empty[AlteredField]
      }

      "the original version's MUCR field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Mucr.pointer}"
        withClue("both declarations have Some values but values are different") {
          val declaration = aDeclaration(withMucr(Mucr("latest")))
          val originalValue = Mucr("original")
          declaration.createDiff(declaration.copy(mucr = Some(originalValue))) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, declaration.mucr.get)
          )
        }

        withClue("the original version's MUCR field is None but this one has Some value") {
          val declaration = aDeclaration(withMucr(Mucr("latest")))
          val originalValue = None
          declaration.createDiff(declaration.copy(mucr = originalValue)) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, declaration.mucr)
          )
        }

        withClue("the original version's MUCR field is Some but this one has None as its value") {
          val declaration = aDeclaration()
          val originalValue = Some(Mucr("original"))
          declaration.createDiff(declaration.copy(mucr = originalValue)) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, declaration.mucr)
          )
        }
      }

      "the original version's transport field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Transport.pointer}.${Transport.expressConsignmentPointer}"
        val declaration = aDeclaration()
        val originalValue = Transport(expressConsignment = YesNoAnswer.Yes)
        declaration.createDiff(declaration.copy(transport = originalValue)) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue.expressConsignment, declaration.transport.expressConsignment)
        )
      }

      "the original version's parties field has a different value to this one" in {
        val fieldPointer = s"$baseFieldPointer.${Parties.pointer}.${Parties.isEntryIntoDeclarantsRecordsPointer}"
        val declaration = aDeclaration()
        val originalValue = Parties(isEntryIntoDeclarantsRecords = YesNoAnswer.Yes)
        declaration.createDiff(declaration.copy(parties = originalValue)) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue.isEntryIntoDeclarantsRecords, declaration.parties.isEntryIntoDeclarantsRecords)
        )
      }

      "the original version's locations field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Locations.pointer}.${Locations.originationCountryPointer}"
        val declaration = aDeclaration().copy(locations = aDeclaration().locations.copy(originationCountry = None))
        val originalValue = Locations(originationCountry = Some(Country(Some("GB"))))
        declaration.createDiff(declaration.copy(locations = originalValue)) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue.originationCountry, declaration.locations.originationCountry)
        )
      }

      "the original version's items field has had an existing item removed" in {
        val itemFieldPointer = s"${baseFieldPointer}.${ExportItem.pointer}.2"
        val originalItem1 = ExportItem("1", 1)
        val originalItem2 = ExportItem("2", 2)
        val originalDeclaration = aDeclaration(withItems(originalItem1, originalItem2))
        val amendedDeclaration = aDeclaration(withItems(originalItem1))

        amendedDeclaration.createDiff(originalDeclaration) mustBe Seq(
          AlteredField(itemFieldPointer, OriginalAndNewValues(Some(originalItem2), None)),
          AlteredField(goodsItemQuantityFieldPointer, OriginalAndNewValues(Some(2), Some(1)))
        )
      }

      "the original version's items field has had a new item added" in {
        val itemFieldPointer = s"${baseFieldPointer}.${ExportItem.pointer}.2"
        val originalItem = ExportItem("1", 1)
        val originalDeclaration = aDeclaration(withItems(originalItem))
        val newItem = ExportItem(s"2", 2)
        val amendedDeclaration = aDeclaration(withItems(originalItem, newItem))

        amendedDeclaration.createDiff(originalDeclaration) mustBe Seq(
          AlteredField(itemFieldPointer, OriginalAndNewValues(None, Some(newItem))),
          AlteredField(goodsItemQuantityFieldPointer, OriginalAndNewValues(Some(1), Some(2)))
        )
      }

      "the original version's totalNumberOfItems field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${InvoiceAndPackageTotals.pointer}"
        val declaration = aDeclaration()
        val originalValue = InvoiceAndPackageTotals(totalAmountInvoiced = Some("1"))
        declaration.createDiff(declaration.copy(totalNumberOfItems = Some(originalValue))) mustBe Seq(
          constructAlteredField(fieldPointer, Some(originalValue), declaration.totalNumberOfItems)
        )
      }

      "the original version's previousDocuments field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${PreviousDocumentsData.pointer}"
        val declaration = aDeclaration()
        val originalValue = PreviousDocumentsData(Seq.empty)
        declaration.createDiff(declaration.copy(previousDocuments = Some(originalValue))) mustBe Seq(
          constructAlteredField(fieldPointer, Some(originalValue), declaration.previousDocuments)
        )
      }

      "the original version's NatureOfTransaction field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${NatureOfTransaction.pointer}"
        withClue("both versions have Some values but values are different") {
          val declaration = aDeclaration(withNatureOfTransaction("latest"))
          val originalValue = NatureOfTransaction("original")
          declaration.createDiff(declaration.copy(natureOfTransaction = Some(originalValue))) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, declaration.natureOfTransaction.get)
          )
        }

        withClue("the original version's NatureOfTransaction field is None but this one has Some value") {
          val declaration = aDeclaration(withNatureOfTransaction("latest"))
          val originalValue = None
          declaration.createDiff(declaration.copy(natureOfTransaction = originalValue)) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, declaration.natureOfTransaction)
          )
        }

        withClue("the original version's NatureOfTransaction field is Some but this one has None as its value") {
          val declaration = aDeclaration()
          val originalValue = Some(NatureOfTransaction("original"))
          declaration.createDiff(declaration.copy(natureOfTransaction = originalValue)) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, declaration.natureOfTransaction)
          )
        }
      }
    }
  }
}
