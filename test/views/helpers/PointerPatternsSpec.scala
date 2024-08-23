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

import forms.common.YesNoAnswer.Yes
import forms.section6.Seal
import models.Pointer
import models.declaration.Container
import services.cache.ExportsTestHelper
import views.common.UnitViewSpec
import views.helpers.PointerRecordSpec.{additionalInformationCode, additionalInformationDescription, containerId, documentIdentifier, documentTypeCode, numberOfPackages, sealName, shippingMarks, typeOfPackage}

class PointerPatternsSpec extends UnitViewSpec with ExportsTestHelper {

  private val additionalActorsPointer = Pointer("declaration.parties.additionalActors.actors.#1")
  private val consignorDetailsPointer = Pointer("declaration.parties.consignorDetails")
  private val consignorAddressDetailsPointer = Pointer("declaration.parties.consignorDetails.address")
  private val declarationHoldersPointer = Pointer("declaration.parties.declarationHolders.holders.#1")
  private val exporterDetailsPointer = Pointer("declaration.parties.exporterDetails.address")
  private val representativeDetailsPointer = Pointer("declaration.parties.representativeDetails")
  private val carrierDetailsPointer = Pointer("declaration.parties.carrierDetails.details")
  private val previousDocumentsPointer = Pointer("declaration.previousDocuments.documents.#1")
  private val packageInformationPointer = Pointer("declaration.items.#1.packageInformation.#1")
  private val additionalInformationPointer = Pointer("declaration.items.#1.additionalInformation.items.#1")
  private val additionalDocumentPointer = Pointer("declaration.items.$.additionalDocument.documents.#1")
  private val additionalFiscalReferencesPointer = Pointer("declaration.items.$.additionalFiscalReferencesData.references.#1")
  private val cusCodePointer = Pointer("declaration.items.#1.cusCode")
  private val itemsPointer = Pointer("declaration.items.#1")
  private val containerPointer = Pointer("declaration.transport.containers.#1")


  val populatedDec = aDeclaration(
    withContainerData(Container(1, containerId, Seq(Seal(1, sealName)))),
    withItem(anItem(
      withAdditionalInformation(additionalInformationCode, additionalInformationDescription),
      withAdditionalDocuments(Yes, withAdditionalDocument(documentTypeCode, documentIdentifier)),
      withPackageInformation(typeOfPackage.code, numberOfPackages, shippingMarks)
    )
  ))

  val emptyeDec = aDeclaration()

  "PointerPatterns" can {
    "expand single parent pointers into a sequence of their child pointers, so" should {
      s"expand '$additionalActorsPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(additionalActorsPointer, emptyeDec, emptyeDec)

        expandedPointers.map(_.pattern) must equal(Seq(
          s"${additionalActorsPointer.pattern}.eori",
          s"${additionalActorsPointer.pattern}.type"
        ))
      }

      s"expand '$consignorDetailsPointer' into its child element" in {
        val expandedPointers = PointerPatterns.expandPointer(consignorDetailsPointer, emptyeDec, emptyeDec)

        expandedPointers.map(_.pattern) must equal(Seq(
          s"${consignorDetailsPointer.pattern}.eori"
        ))
      }

      s"expand '$consignorAddressDetailsPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(consignorAddressDetailsPointer, emptyeDec, emptyeDec)

        checkForAddressPointers(Pointer(consignorAddressDetailsPointer.sections.dropRight(1)), expandedPointers)
      }

      s"expand '$declarationHoldersPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(declarationHoldersPointer, emptyeDec, emptyeDec)

        expandedPointers.map(_.pattern) must equal(Seq(
          s"${declarationHoldersPointer.pattern}.eori",
          s"${declarationHoldersPointer.pattern}.authorisationTypeCode"
        ))
      }

      s"expand '$exporterDetailsPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(exporterDetailsPointer, emptyeDec, emptyeDec)

        checkForAddressPointers(Pointer(exporterDetailsPointer.sections.dropRight(1)), expandedPointers)
      }

      s"expand '$representativeDetailsPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(representativeDetailsPointer, emptyeDec, emptyeDec)

        expandedPointers.map(_.pattern) must equal(Seq(
          s"${representativeDetailsPointer.pattern}.details.eori",
          s"${representativeDetailsPointer.pattern}.statusCode"
        ))
      }

      s"expand '$carrierDetailsPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(carrierDetailsPointer, emptyeDec, emptyeDec)

        checkForAddressPointers(Pointer(carrierDetailsPointer.sections.dropRight(1)), expandedPointers)
      }

      s"expand '$previousDocumentsPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(previousDocumentsPointer, emptyeDec, emptyeDec)
        val base = "declaration.previousDocuments.$"

        expandedPointers.map(_.pattern) must equal(Seq(
          s"$base.documentReference",
          s"$base.documentType"
        ))
      }

      s"expand '$packageInformationPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(packageInformationPointer, emptyeDec, emptyeDec)

        expandedPointers.map(_.pattern) must equal(Seq(
          s"${packageInformationPointer.pattern}.typesOfPackages",
          s"${packageInformationPointer.pattern}.numberOfPackages",
          s"${packageInformationPointer.pattern}.shippingMarks"
        ))
      }

      s"expand '$additionalInformationPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(additionalInformationPointer, emptyeDec, emptyeDec)
        val base = "declaration.items.$.additionalInformation.$"

        expandedPointers.map(_.pattern) must equal(Seq(
          s"$base.code",
          s"$base.description"
        ))
      }

      s"expand '$additionalDocumentPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(additionalDocumentPointer, emptyeDec, emptyeDec)
        val base = "declaration.items.$.additionalDocument.$"

        expandedPointers.map(_.pattern) must equal(Seq(
          s"$base.documentTypeCode",
          s"$base.documentIdentifier",
          s"$base.documentStatus",
          s"$base.documentStatusReason",
          s"$base.issuingAuthorityName",
          s"$base.dateOfValidity",
          s"$base.documentWriteOff.measurementUnit",
          s"$base.documentWriteOff.documentQuantity"
        ))
      }

      s"expand '$additionalFiscalReferencesPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(additionalFiscalReferencesPointer, emptyeDec, emptyeDec)

        expandedPointers.map(_.pattern) must equal(Seq(
          "declaration.items.$.additionalFiscalReferences.$.roleCode"
        ))
      }

      s"expand '$cusCodePointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(cusCodePointer, emptyeDec, emptyeDec)

        expandedPointers.map(_.pattern) must equal(Seq(
          s"${cusCodePointer.pattern}.cusCode"
        ))
      }

      s"expand '$itemsPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(itemsPointer, emptyeDec, populatedDec)
        val itemsPointerBase = itemsPointer.pattern

        expandedPointers.map(_.pattern) must equal(Seq(
          s"$itemsPointerBase.procedureCodes.procedure.code",
          s"$itemsPointerBase.procedureCodes.additionalProcedureCodes",
          s"$itemsPointerBase.statisticalValue.statisticalValue",
          s"$itemsPointerBase.commodityDetails",
          s"$itemsPointerBase.commodityDetails.descriptionOfGoods",
          s"$itemsPointerBase.nactExemptionCode",
          s"$itemsPointerBase.packageInformation.$$.typesOfPackages",
          s"$itemsPointerBase.packageInformation.$$.numberOfPackages",
          s"$itemsPointerBase.packageInformation.$$.shippingMarks",
          s"$itemsPointerBase.commodityMeasure.grossMass",
          s"$itemsPointerBase.commodityMeasure.netMass",
          s"$itemsPointerBase.commodityMeasure.supplementaryUnits",
          s"$itemsPointerBase.additionalInformation.$$.code",
          s"$itemsPointerBase.additionalInformation.$$.description",
          s"$itemsPointerBase.additionalDocument.documents.$$.documentTypeCode",
          s"$itemsPointerBase.additionalDocument.documents.$$.documentIdentifier",
          s"$itemsPointerBase.additionalDocument.documents.$$.documentStatus",
          s"$itemsPointerBase.additionalDocument.documents.$$.documentStatusReason",
          s"$itemsPointerBase.additionalDocument.documents.$$.issuingAuthorityName",
          s"$itemsPointerBase.additionalDocument.documents.$$.dateOfValidity",
          s"$itemsPointerBase.additionalDocument.documents.$$.documentWriteOff",
          s"$itemsPointerBase.additionalDocument.documents.$$.documentQuantity"
        ))
      }

      s"expand '$containerPointer' into its child elements" in {
        val expandedPointers = PointerPatterns.expandPointer(containerPointer, emptyeDec, populatedDec)

        expandedPointers.map(_.pattern) must equal(Seq(
          s"${containerPointer.pattern}.id",
          s"${containerPointer.pattern}.seals.ids"
        ))
      }
    }
  }

  private def checkForAddressPointers(basePointer: Pointer, expandedPointers: Seq[Pointer]) = {
    expandedPointers.map(_.pattern) must equal(Seq(
      s"${basePointer.pattern}.details.address.fullName",
      s"${basePointer.pattern}.details.address.addressLine",
      s"${basePointer.pattern}.details.address.townOrCity",
      s"${basePointer.pattern}.details.address.postCode",
      s"${basePointer.pattern}.details.address.country",
    ))
  }
}
