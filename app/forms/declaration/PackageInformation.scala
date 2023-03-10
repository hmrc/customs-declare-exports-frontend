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

package forms.declaration

import forms.DeclarationPage
import models.DeclarationMeta.sequenceIdPlaceholder
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.viewmodels.TariffContentKey
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.data.Forms.{number, optional, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.Json
import services.DiffTools.{combinePointers, compareIntDifference, compareStringDifference, ExportsDeclarationDiff}
import services.{DiffTools, PackageTypesService}
import utils.validators.forms.FieldValidator._

case class PackageInformation(
  sequenceId: Int,
  id: String,
  typesOfPackages: Option[String],
  numberOfPackages: Option[Int],
  shippingMarks: Option[String]
) extends DiffTools[PackageInformation] {

  def createDiff(original: PackageInformation, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(original.id, id, combinePointers(pointerString, PackageInformation.idPointer, sequenceId)),
      compareStringDifference(
        original.typesOfPackages,
        typesOfPackages,
        combinePointers(pointerString, PackageInformation.typesOfPackagesPointer, sequenceId)
      ),
      compareIntDifference(
        original.numberOfPackages,
        numberOfPackages,
        combinePointers(pointerString, PackageInformation.numberOfPackagesPointer, sequenceId)
      ),
      compareStringDifference(
        original.shippingMarks,
        shippingMarks,
        combinePointers(pointerString, PackageInformation.shippingMarksPointer, sequenceId)
      )
    ).flatten

  // overriding equals and hashcode so that we can test for duplicate entries while ignoring the id
  override def equals(obj: Any): Boolean = obj match {
    case PackageInformation(_, _, `typesOfPackages`, `numberOfPackages`, `shippingMarks`) => true
    case _                                                                                => false
  }
  override def hashCode(): Int = (typesOfPackages, numberOfPackages, shippingMarks).##

  def isEmpty: Boolean = typesOfPackages.isEmpty && numberOfPackages.isEmpty && shippingMarks.isEmpty

  def nonEmpty: Boolean = !isEmpty
}

object PackageInformation extends DeclarationPage with FieldMapping {
  import scala.util.Random

  implicit val format = Json.format[PackageInformation]

  val pointer: ExportsFieldPointer = "packageInformation"
  val idPointer: ExportsFieldPointer = "id"
  val shippingMarksPointer: ExportsFieldPointer = "shippingMarks"
  val numberOfPackagesPointer: ExportsFieldPointer = "numberOfPackages"
  val typesOfPackagesPointer: ExportsFieldPointer = "typesOfPackages"

  val formId = "PackageInformation"
  val limit = 99

  private val NumberOfPackagesLimitLower = 0
  private val NumberOfPackagesLimitUpper = 99999

  private def generateId: String = Random.alphanumeric.take(8).mkString.toLowerCase

  def form2Data(typesOfPackages: Option[String], numberOfPackages: Option[Int], shippingMarks: Option[String]): PackageInformation =
    new PackageInformation(sequenceIdPlaceholder, generateId, typesOfPackages, numberOfPackages, shippingMarks)

  def data2Form(data: PackageInformation): Option[(Option[String], Option[Int], Option[String])] =
    Some((data.typesOfPackages, data.numberOfPackages, data.shippingMarks))

  def mapping(implicit messages: Messages, packageTypesService: PackageTypesService): Mapping[PackageInformation] = Forms
    .mapping(
      "typesOfPackages" -> optional(
        text()
          .verifying("declaration.packageInformation.typesOfPackages.error", isContainedIn(packageTypesService.all.map(_.code)))
      ).verifying("declaration.packageInformation.typesOfPackages.empty", isPresent),
      "numberOfPackages" -> optional(
        number()
          .verifying("declaration.packageInformation.numberOfPackages.error", isInRange(NumberOfPackagesLimitLower, NumberOfPackagesLimitUpper))
      ).verifying("declaration.packageInformation.numberOfPackages.error", isPresent),
      "shippingMarks" -> optional(
        text()
          .verifying("declaration.packageInformation.shippingMark.characterError", isEmpty or isAlphanumericWithAllowedSpecialCharacters)
          .verifying("declaration.packageInformation.shippingMark.lengthError", isEmpty or noLongerThan(42))
      ).verifying("declaration.packageInformation.shippingMark.empty", isPresent)
    )(form2Data)(data2Form)

  def form(implicit messages: Messages, packageTypesService: PackageTypesService): Form[PackageInformation] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    decType match {
      case CLEARANCE =>
        Seq(
          TariffContentKey("tariff.declaration.item.packageInformation.1.clearance"),
          TariffContentKey("tariff.declaration.item.packageInformation.2.clearance"),
          TariffContentKey("tariff.declaration.item.packageInformation.3.clearance"),
          TariffContentKey("tariff.declaration.item.packageInformation.4.clearance")
        )
      case _ =>
        Seq(
          TariffContentKey("tariff.declaration.item.packageInformation.1.common"),
          TariffContentKey("tariff.declaration.item.packageInformation.2.common"),
          TariffContentKey("tariff.declaration.item.packageInformation.3.common")
        )
    }
}
