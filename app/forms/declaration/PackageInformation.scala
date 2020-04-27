/*
 * Copyright 2020 HM Revenue & Customs
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
import models.DeclarationType.{CLEARANCE, DeclarationType}
import play.api.data.Forms.{number, optional, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{JsValue, Json}
import services.PackageTypes
import utils.validators.forms.FieldValidator._

case class PackageInformation(typesOfPackages: Option[String], numberOfPackages: Option[Int], shippingMarks: Option[String]) {

  def isEmpty: Boolean = typesOfPackages.isEmpty && numberOfPackages.isEmpty && shippingMarks.isEmpty

  def nonEmpty: Boolean = !isEmpty

  def toJson: JsValue = Json.toJson(this)(PackageInformation.format)

  def typesOfPackagesText: Option[String] = typesOfPackages.map(types => PackageTypes.findByCode(types).asText())
}

object PackageInformation extends DeclarationPage {

  def fromJsonString(value: String): Option[PackageInformation] = Json.fromJson(Json.parse(value)).asOpt

  implicit val format = Json.format[PackageInformation]

  val formId = "PackageInformation"
  val limit = 99

  private val NumberOfPackagesLimitLower = 0
  private val NumberOfPackagesLimitUpper = 99999

  private val mappingTypesOfPackagesOptional: Mapping[Option[String]] = optional(
    text()
      .verifying("declaration.packageInformation.typesOfPackages.error", isContainedIn(PackageTypes.all.map(_.code)))
  )

  private val mappingTypesOfPackagesMandatory: Mapping[Option[String]] =
    mappingTypesOfPackagesOptional.verifying("declaration.packageInformation.typesOfPackages.empty", isPresent)

  private val mappingNumberOfPackagesOptional: Mapping[Option[Int]] = optional(
    number()
      .verifying("declaration.packageInformation.numberOfPackages.error", isInRange(NumberOfPackagesLimitLower, NumberOfPackagesLimitUpper))
  )

  private val mappingNumberOfPackagesMandatory: Mapping[Option[Int]] =
    mappingNumberOfPackagesOptional.verifying("error.number", isPresent)

  private val mappingShippingMarksOptional: Mapping[Option[String]] = optional(
    text()
      .verifying("declaration.packageInformation.shippingMarks.characterError", isEmpty or isAlphanumericWithAllowedSpecialCharacters)
      .verifying("declaration.packageInformation.shippingMarks.lengthError", isEmpty or noLongerThan(42))
  )

  private val mappingShippingMarksMandatory: Mapping[Option[String]] =
    mappingShippingMarksOptional.verifying("declaration.packageInformation.shippingMarks.empty", isPresent)

  val mappingAllFieldsMandatory = Forms
    .mapping(
      "typesOfPackages" -> mappingTypesOfPackagesMandatory,
      "numberOfPackages" -> mappingNumberOfPackagesMandatory,
      "shippingMarks" -> mappingShippingMarksMandatory
    )(PackageInformation.apply)(PackageInformation.unapply)

  val mappingAllFieldsOptional = Forms
    .mapping(
      "typesOfPackages" -> mappingTypesOfPackagesOptional,
      "numberOfPackages" -> mappingNumberOfPackagesOptional,
      "shippingMarks" -> mappingShippingMarksOptional
    )(PackageInformation.apply)(PackageInformation.unapply)
    .verifying("declaration.packageInformation.empty", _.nonEmpty)

  def form(declarationType: DeclarationType): Form[PackageInformation] = declarationType match {
    case CLEARANCE => Form(mappingAllFieldsOptional)
    case _         => Form(mappingAllFieldsMandatory)
  }
}
