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

import models.{ExportsDeclaration, Pointer, PointerSection}
import models.PointerSectionType.{FIELD, SEQUENCE}
import models.declaration.ExportItem

import scala.util.Try

object PointerPatterns {
  val pointerToDucr = "declaration.consignmentReferences.ucr"

  val expandAdditionalActors = (p: Pointer, _: ExportsDeclaration, _: ExportsDeclaration) =>
    Seq(Pointer(p.sections :+ PointerSection("eori", FIELD)), Pointer(p.sections :+ PointerSection("type", FIELD)))

  val expandCusCode = (p: Pointer, _: ExportsDeclaration, _: ExportsDeclaration) => Seq(Pointer(p.sections :+ PointerSection("cusCode", FIELD)))

  val expandDetails = (p: Pointer, _: ExportsDeclaration, _: ExportsDeclaration) => Seq(Pointer(p.sections :+ PointerSection("eori", FIELD)))

  val expandAddressDetails = (p: Pointer, _: ExportsDeclaration, _: ExportsDeclaration) => {
    val baseSections = p.sections.take(3) ++ Seq(PointerSection("details", FIELD), PointerSection("address", FIELD))
    addAddressDetails(baseSections)
  }

  val expandCarrierDetails = (p: Pointer, _: ExportsDeclaration, _: ExportsDeclaration) => {
    val baseSections = p.sections :+ PointerSection("address", FIELD)
    addAddressDetails(baseSections)
  }

  val expandRepresentativeDetails = (p: Pointer, _: ExportsDeclaration, _: ExportsDeclaration) =>
    Seq(
      Pointer(p.sections ++ Seq(PointerSection("details", FIELD), PointerSection("eori", FIELD))),
      Pointer(p.sections :+ PointerSection("statusCode", FIELD))
    )

  private def addAddressDetails(baseSections: Seq[PointerSection]): Seq[Pointer] =
    Seq(
      Pointer(baseSections :+ PointerSection("fullName", FIELD)),
      Pointer(baseSections :+ PointerSection("addressLine", FIELD)),
      Pointer(baseSections :+ PointerSection("townOrCity", FIELD)),
      Pointer(baseSections :+ PointerSection("postCode", FIELD)),
      Pointer(baseSections :+ PointerSection("country", FIELD))
    )

  val expandPreviousDocuments = (p: Pointer, _: ExportsDeclaration, _: ExportsDeclaration) => {
    val baseSections = p.sections.take(2) :+ p.sections.last
    Seq(Pointer(baseSections ++ Seq(PointerSection("documentReference", FIELD))), Pointer(baseSections ++ Seq(PointerSection("documentType", FIELD))))
  }

  val expandPackageInformation = (p: Pointer, _: ExportsDeclaration, _: ExportsDeclaration) =>
    Seq(
      Pointer(p.sections :+ PointerSection("typesOfPackages", FIELD)),
      Pointer(p.sections :+ PointerSection("numberOfPackages", FIELD)),
      Pointer(p.sections :+ PointerSection("shippingMarks", FIELD))
    )

  val expandAdditionalInformation = (p: Pointer, _: ExportsDeclaration, _: ExportsDeclaration) => {
    val baseSections = p.sections.take(4) :+ p.sections(5)
    Seq(Pointer(baseSections ++ Seq(PointerSection("code", FIELD))), Pointer(baseSections ++ Seq(PointerSection("description", FIELD))))
  }

  val expandAdditionalDocument = (p: Pointer, _: ExportsDeclaration, _: ExportsDeclaration) => {
    val baseSections = p.sections.take(4) :+ p.sections(5)
    Seq(
      Pointer(baseSections ++ Seq(PointerSection("documentTypeCode", FIELD))),
      Pointer(baseSections ++ Seq(PointerSection("documentIdentifier", FIELD))),
      Pointer(baseSections ++ Seq(PointerSection("documentStatus", FIELD))),
      Pointer(baseSections ++ Seq(PointerSection("documentStatusReason", FIELD))),
      Pointer(baseSections ++ Seq(PointerSection("issuingAuthorityName", FIELD))),
      Pointer(baseSections ++ Seq(PointerSection("dateOfValidity", FIELD))),
      Pointer(baseSections ++ Seq(PointerSection("documentWriteOff", FIELD), PointerSection("measurementUnit", FIELD))),
      Pointer(baseSections ++ Seq(PointerSection("documentWriteOff", FIELD), PointerSection("documentQuantity", FIELD)))
    )
  }

  val expandItem = (p: Pointer, orig: ExportsDeclaration, amend: ExportsDeclaration) => {
    val maybeItemIdx = Try(p.sections(2).value.drop(0).toInt).toOption.map(_ - 1)

    def getMaxNumberOfElement(elementSelector: ExportItem => Option[Int]) =
      maybeItemIdx.flatMap { itemIdx =>
        val origSize = orig.items.lift(itemIdx).flatMap(elementSelector)
        val amendSize = amend.items.lift(itemIdx).flatMap(elementSelector)

        val maxSizes: Seq[Int] = Seq(origSize, amendSize).flatten
        if (maxSizes.length < 1) None else Some(maxSizes.max)
      }

    def getPackagePointers() = {
      val baseSections = p.sections ++ Seq(PointerSection("packageInformation", FIELD))
      val maybeMaxNoOfPackages = getMaxNumberOfElement((ei: ExportItem) => ei.packageInformation.map(_.size))

      maybeMaxNoOfPackages.fold(Seq.empty[Pointer]) { max =>
        (1 to max).flatMap { idx =>
          val pointerSequence = PointerSection(idx.toString, SEQUENCE)

          Seq(
            Pointer(baseSections ++ Seq(pointerSequence, PointerSection("typesOfPackages", FIELD))),
            Pointer(baseSections ++ Seq(pointerSequence, PointerSection("numberOfPackages", FIELD))),
            Pointer(baseSections ++ Seq(pointerSequence, PointerSection("shippingMarks", FIELD)))
          )
        }
      }
    }

    def getAdditionalInfoPointers() = {
      val baseSections = p.sections ++ Seq(PointerSection("additionalInformation", FIELD))
      val maybeMaxAdditionalInfo = getMaxNumberOfElement((ei: ExportItem) => ei.additionalInformation.map(_.items.size))

      maybeMaxAdditionalInfo.fold(Seq.empty[Pointer]) { max =>
        (1 to max).flatMap { idx =>
          val pointerSequence = PointerSection(idx.toString, SEQUENCE)

          Seq(
            Pointer(baseSections ++ Seq(pointerSequence, PointerSection("code", FIELD))),
            Pointer(baseSections ++ Seq(pointerSequence, PointerSection("description", FIELD)))
          )
        }
      }
    }

    def getAdditionalDocumentPointers() = {
      val baseSections = p.sections ++ Seq(PointerSection("additionalDocument", FIELD), PointerSection("documents", FIELD))
      val maybeMaxAdditionalDocs = getMaxNumberOfElement((ei: ExportItem) => ei.additionalDocuments.map(_.documents.size))

      maybeMaxAdditionalDocs.fold(Seq.empty[Pointer]) { max =>
        (1 to max).flatMap { idx =>
          val pointerSequence = PointerSection(idx.toString, SEQUENCE)

          Seq(
            Pointer(baseSections ++ Seq(pointerSequence, PointerSection("documentTypeCode", FIELD))),
            Pointer(baseSections ++ Seq(pointerSequence, PointerSection("documentIdentifier", FIELD))),
            Pointer(baseSections ++ Seq(pointerSequence, PointerSection("documentStatus", FIELD))),
            Pointer(baseSections ++ Seq(pointerSequence, PointerSection("documentStatusReason", FIELD))),
            Pointer(baseSections ++ Seq(pointerSequence, PointerSection("issuingAuthorityName", FIELD))),
            Pointer(baseSections ++ Seq(pointerSequence, PointerSection("dateOfValidity", FIELD))),
            Pointer(baseSections ++ Seq(pointerSequence, PointerSection("documentWriteOff", FIELD))),
            Pointer(baseSections ++ Seq(pointerSequence, PointerSection("documentQuantity", FIELD)))
          )
        }
      }
    }

    val packagePointers = getPackagePointers()
    val additionalInfoPointers = getAdditionalInfoPointers()
    val additionalDocumentPointers = getAdditionalDocumentPointers()

    Seq(
      Pointer(p.sections ++ Seq(PointerSection("procedureCodes", FIELD), PointerSection("procedure", FIELD), PointerSection("code", FIELD))),
      Pointer(p.sections ++ Seq(PointerSection("procedureCodes", FIELD), PointerSection("additionalProcedureCodes", FIELD))),
      Pointer(p.sections ++ Seq(PointerSection("statisticalValue", FIELD), PointerSection("statisticalValue", FIELD))),
      Pointer(p.sections ++ Seq(PointerSection("commodityDetails", FIELD))),
      Pointer(p.sections ++ Seq(PointerSection("commodityDetails", FIELD), PointerSection("descriptionOfGoods", FIELD))),
      Pointer(p.sections ++ Seq(PointerSection("nactExemptionCode", FIELD)))
    ) ++ packagePointers ++ Seq(
      Pointer(p.sections ++ Seq(PointerSection("commodityMeasure", FIELD), PointerSection("grossMass", FIELD))),
      Pointer(p.sections ++ Seq(PointerSection("commodityMeasure", FIELD), PointerSection("netMass", FIELD))),
      Pointer(p.sections ++ Seq(PointerSection("commodityMeasure", FIELD), PointerSection("supplementaryUnits", FIELD)))
    ) ++ additionalInfoPointers ++ additionalDocumentPointers
  }

  val expandContainers = (p: Pointer, orig: ExportsDeclaration, amend: ExportsDeclaration) => {
    val maybeContainerIdx = Try(p.sections(3).value.drop(0).toInt).toOption.map(_ - 1)

    val maybeMaxNoOfSeals = maybeContainerIdx.flatMap { containerIdx =>
      val origSize = orig.containers.lift(containerIdx).map(_.seals.size)
      val amendSize = amend.containers.lift(containerIdx).map(_.seals.size)

      val maxSizes: Seq[Int] = Seq(origSize, amendSize).flatten
      if (maxSizes.length < 1) None else Some(maxSizes.max)
    }

    val sealPointers = maybeMaxNoOfSeals.fold(Seq.empty[Pointer]) { max =>
      if (max > 0)
        Seq(Pointer(p.sections ++ Seq(PointerSection("seals", FIELD), PointerSection("ids", FIELD))))
      else
        Seq.empty[Pointer]
    }

    Seq(Pointer(p.sections :+ PointerSection("id", FIELD))) ++ sealPointers
  }

  private val expandDeclarationHolders = (p: Pointer, _: ExportsDeclaration, _: ExportsDeclaration) =>
    Seq(Pointer(p.sections :+ PointerSection("eori", FIELD)), Pointer(p.sections :+ PointerSection("authorisationTypeCode", FIELD)))

  val expandAdditionalFiscalReferences = (p: Pointer, _: ExportsDeclaration, _: ExportsDeclaration) =>
    Seq(Pointer(p.sections.take(3) ++ Seq(PointerSection("additionalFiscalReferences", FIELD), p.sections(5), PointerSection("roleCode", FIELD))))

  private val pointerExpansions: Map[String, (Pointer, ExportsDeclaration, ExportsDeclaration) => Seq[Pointer]] = Map(
    "declaration.parties.additionalActors.actors.$" -> expandAdditionalActors,
    "declaration.parties.consignorDetails" -> expandDetails,
    "declaration.parties.consignorDetails.address" -> expandAddressDetails,
    "declaration.parties.declarationHolders.holders.$" -> expandDeclarationHolders,
    "declaration.parties.exporterDetails" -> expandDetails,
    "declaration.parties.exporterDetails.address" -> expandAddressDetails,
    "declaration.parties.representativeDetails" -> expandRepresentativeDetails,
    "declaration.parties.carrierDetails.details" -> expandCarrierDetails,
    "declaration.previousDocuments.documents.$" -> expandPreviousDocuments,
    "declaration.items.$.packageInformation.$" -> expandPackageInformation,
    "declaration.items.$.additionalInformation.items.$" -> expandAdditionalInformation,
    "declaration.items.$.additionalDocument.documents.$" -> expandAdditionalDocument,
    "declaration.items.$.additionalFiscalReferencesData.references.$" -> expandAdditionalFiscalReferences,
    "declaration.items.$.cusCode" -> expandCusCode,
    "declaration.items.$" -> expandItem,
    "declaration.transport.containers.$" -> expandContainers
  )

  def expandPointer(pointer: Pointer, orig: ExportsDeclaration, amend: ExportsDeclaration): Seq[Pointer] =
    pointerExpansions.get(pointer.pattern).map(_(pointer, orig, amend)).getOrElse(Seq(pointer))
}
