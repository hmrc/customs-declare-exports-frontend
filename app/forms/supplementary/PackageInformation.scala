/*
 * Copyright 2019 HM Revenue & Customs
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

package forms.supplementary

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.FormFieldValidator._

case class PackageInformation(
  typesOfPackages: Option[String],
  numberOfPackages: Option[String],
  supplementaryUnits: Option[String],
  shippingMarks: Option[String],
  netMass: Option[String],
  grossMass: Option[String]
)

object PackageInformation {

  implicit val format = Json.format[PackageInformation]

  val formId = "PackageInformation"

  val mapping = Forms.mapping(
    "typesOfPackages" -> optional(
      text()
        .verifying(
          "supplementary.packageInformation.typesOfPackages.error",
          isEmpty or (isAlphanumeric and hasSpecificLength(2))
        )
        .verifying("supplementary.packageInformation.typesOfPackages.empty", nonEmpty)),
    "numberOfPackages" -> optional(
      text()
        .verifying("supplementary.packageInformation.numberOfPackages.error", isEmpty or (isNumeric and noLongerThan(5)))
        .verifying("supplementary.packageInformation.numberOfPackages.empty", nonEmpty)),
    "supplementaryUnits" -> optional(
      text().verifying("supplementary.packageInformation.supplementaryUnits.error", validateDecimal(16)(6))
    ),
    "shippingMarks" -> optional(
      text()
        .verifying(
          "supplementary.packageInformation.shippingMarks.error",
          isEmpty or (isAlphanumeric and noLongerThan(42))
        )
        .verifying("supplementary.packageInformation.shippingMarks.empty", nonEmpty)),
    "netMass" -> optional(
      text()
        .verifying("supplementary.packageInformation.netMass.error", isEmpty or validateDecimal(11)(3))
        .verifying("supplementary.packageInformation.netMass.empty", nonEmpty)),
    "grossMass" -> optional(
      text()
        .verifying("supplementary.packageInformation.grossMass.error", isEmpty or validateDecimal(16)(6))
        .verifying("supplementary.packageInformation.grossMass.empty", nonEmpty))
  )(PackageInformation.apply)(PackageInformation.unapply)

  def form(): Form[PackageInformation] = Form(mapping)
}