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

import java.util.UUID

import forms.DeclarationPage
import play.api.data.Forms.{number, optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import services.PackageTypes
import utils.validators.forms.FieldValidator._

case class PackageInformation(id: String, typesOfPackages: Option[String], numberOfPackages: Option[Int], shippingMarks: Option[String]) {

  // overriding equals and hashcode so that we can test for duplicate entries while ignoring the id
  override def equals(obj: Any): Boolean = obj match {
    case PackageInformation(_, `typesOfPackages`, `numberOfPackages`, `shippingMarks`) => true
    case _                                                                             => false
  }
  override def hashCode(): Int = (typesOfPackages, numberOfPackages, shippingMarks).##

  def isEmpty: Boolean = typesOfPackages.isEmpty && numberOfPackages.isEmpty && shippingMarks.isEmpty

  def nonEmpty: Boolean = !isEmpty

  def typesOfPackagesText: Option[String] = typesOfPackages.map(types => PackageTypes.findByCode(types).asText())

}

object PackageInformation extends DeclarationPage {

  import scala.util.Random

  implicit val format = Json.format[PackageInformation]

  val formId = "PackageInformation"
  val limit = 99

  private val NumberOfPackagesLimitLower = 0
  private val NumberOfPackagesLimitUpper = 99999

  private def generateId: String = Random.alphanumeric.take(8).mkString.toLowerCase

  def form2Data(typesOfPackages: Option[String], numberOfPackages: Option[Int], shippingMarks: Option[String]): PackageInformation =
    new PackageInformation(generateId, typesOfPackages, numberOfPackages, shippingMarks)

  def data2Form(data: PackageInformation): Option[(Option[String], Option[Int], Option[String])] =
    Some((data.typesOfPackages, data.numberOfPackages, data.shippingMarks))

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
    )(form2Data)(data2Form)

  def form(): Form[PackageInformation] = Form(mapping)

}
