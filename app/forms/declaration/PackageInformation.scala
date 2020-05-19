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
import play.api.data.Forms.{number, optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import services.PackageTypes
import utils.validators.forms.FieldValidator._

case class PackageInformation(typesOfPackages: Option[String], numberOfPackages: Option[Int], shippingMarks: Option[String]) {

  def id: String = s"${typesOfPackages.getOrElse("_")}.${numberOfPackages.getOrElse("_")}.${shippingMarks.getOrElse("_")}"

  def isEmpty: Boolean = typesOfPackages.isEmpty && numberOfPackages.isEmpty && shippingMarks.isEmpty

  def nonEmpty: Boolean = !isEmpty

  def typesOfPackagesText: Option[String] = typesOfPackages.map(types => PackageTypes.findByCode(types).asText())
}

object PackageInformation extends DeclarationPage {

  def fromId(value: String): PackageInformation = {
    def someString(value: String): Option[String] = if (value == "_") None else Some(value)
    def someInt(value: String): Option[Int] = if (value == "_") None else Some(value.toInt)
    value.split("\\.") match {
      case Array(t, n, m) => PackageInformation(someString(t), someInt(n), someString(m))
      case _              => PackageInformation(None, None, None)
    }
  }

  implicit val format = Json.format[PackageInformation]

  val formId = "PackageInformation"
  val limit = 99

  private val NumberOfPackagesLimitLower = 0
  private val NumberOfPackagesLimitUpper = 99999

  val mapping = Forms
    .mapping(
      "typesOfPackages" -> optional(
        text()
          .verifying("declaration.packageInformation.typesOfPackages.error", isContainedIn(PackageTypes.all.map(_.code)))
      ).verifying("declaration.packageInformation.typesOfPackages.empty", isPresent),
      "numberOfPackages" -> optional(
        number()
          .verifying("declaration.packageInformation.numberOfPackages.error", isInRange(NumberOfPackagesLimitLower, NumberOfPackagesLimitUpper))
      ).verifying("error.number", isPresent),
      "shippingMarks" -> optional(
        text()
          .verifying("declaration.packageInformation.shippingMarks.characterError", isEmpty or isAlphanumericWithAllowedSpecialCharacters)
          .verifying("declaration.packageInformation.shippingMarks.lengthError", isEmpty or noLongerThan(42))
      ).verifying("declaration.packageInformation.shippingMarks.empty", isPresent)
    )(PackageInformation.apply)(PackageInformation.unapply)

  def form(): Form[PackageInformation] = Form(mapping)

}
