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
import play.api.data.{Form, Forms}
import play.api.libs.json.{JsValue, Json}
import services.PackageTypes
import utils.validators.forms.FieldValidator._

case class PackageInformation(typesOfPackages: Option[String], numberOfPackages: Option[Int], shippingMarks: Option[String]) {
  def toJson: JsValue = Json.toJson(this)(PackageInformation.format)

  def typesOfPackagesText: Option[String] =
    for {
      types <- typesOfPackages
      packageType <- PackageTypes.findByCode(types)
    } yield packageType.asText()
}

object PackageInformation extends DeclarationPage {

  private def fromValues(typesOfPackages: String, numberOfPackages: Int, shippingMarks: String): PackageInformation =
    PackageInformation(typesOfPackages = Some(typesOfPackages), numberOfPackages = Some(numberOfPackages), shippingMarks = Some(shippingMarks))

  private def toValues(packageInformation: PackageInformation): Option[(String, Int, String)] =
    for {
      typesOfPackages <- packageInformation.typesOfPackages
      numberOfPackages <- packageInformation.numberOfPackages
      shippingMarks <- packageInformation.shippingMarks
    } yield (typesOfPackages, numberOfPackages, shippingMarks)

  def fromJsonString(value: String): Option[PackageInformation] = Json.fromJson(Json.parse(value)).asOpt

  implicit val format = Json.format[PackageInformation]

  val formId = "PackageInformation"
  val limit = 99

  private val NumberOfPackagesLimitLower = 0
  private val NumberOfPackagesLimitUpper = 99999

  val mappingAllFieldsMandatory = Forms
    .mapping(
      "typesOfPackages" ->
        text()
          .verifying("supplementary.packageInformation.typesOfPackages.empty", nonEmpty)
          .verifying("supplementary.packageInformation.typesOfPackages.error", isEmpty or isContainedIn(PackageTypes.all.map(_.code))),
      "numberOfPackages" ->
        number()
          .verifying("supplementary.packageInformation.numberOfPackages.error", isInRange(NumberOfPackagesLimitLower, NumberOfPackagesLimitUpper)),
      "shippingMarks" ->
        text()
          .verifying("supplementary.packageInformation.shippingMarks.empty", nonEmpty)
          .verifying("supplementary.packageInformation.shippingMarks.characterError", isEmpty or isAlphanumericWithAllowedSpecialCharacters)
          .verifying("supplementary.packageInformation.shippingMarks.lengthError", isEmpty or noLongerThan(42))
    )(PackageInformation.fromValues)(PackageInformation.toValues)

  val mappingAllFieldsOptional = Forms
    .mapping(
      "typesOfPackages" -> optional(
        text()
          .verifying("supplementary.packageInformation.typesOfPackages.error", isContainedIn(PackageTypes.all.map(_.code)))
      ),
      "numberOfPackages" -> optional(
        number()
          .verifying("supplementary.packageInformation.numberOfPackages.error", isInRange(NumberOfPackagesLimitLower, NumberOfPackagesLimitUpper))
      ),
      "shippingMarks" -> optional(
        text()
          .verifying("supplementary.packageInformation.shippingMarks.characterError", isAlphanumericWithAllowedSpecialCharacters)
          .verifying("supplementary.packageInformation.shippingMarks.lengthError", noLongerThan(42))
      )
    )(PackageInformation.apply)(PackageInformation.unapply)
    .verifying("supplementary.packageInformation.empty", validatePackageInformation(_))

  private def validatePackageInformation(packageInformation: PackageInformation): Boolean =
    !(packageInformation.typesOfPackages.isEmpty && packageInformation.numberOfPackages.isEmpty && packageInformation.shippingMarks.isEmpty)

  def form(declarationType: DeclarationType): Form[PackageInformation] = declarationType match {
    case CLEARANCE => Form(mappingAllFieldsOptional)
    case _         => Form(mappingAllFieldsMandatory)
  }
}
