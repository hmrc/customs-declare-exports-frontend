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

package forms.declaration

import play.api.data.Forms.{number, optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import services.PackageTypes
import utils.validators.forms.FieldValidator._

case class PackageInformation(typesOfPackages: String, numberOfPackages: Int, shippingMarks: String)

object PackageInformation {

  def require1Field[T](fs: (T => Option[_])*): T => Boolean =
    t => fs.exists(f => f(t).nonEmpty)

  implicit val format = Json.format[PackageInformation]

  val formId = "PackageInformation"
  val limit = 99

  //TODO Remove the last validation and inlined error and validate separately every field like mandatory field
  val mapping = Forms
    .mapping(
      "typesOfPackages" ->
        text()
          .verifying("supplementary.packageInformation.typesOfPackages.empty", nonEmpty)
          .verifying(
            "supplementary.packageInformation.typesOfPackages.error",
            isEmpty or isContainedIn(PackageTypes.all.map(_.code))
          ),
      "numberOfPackages" ->
        number()
          .verifying("supplementary.packageInformation.numberOfPackages.error", q => q > 0 && q <= 999999),
      "shippingMarks" ->
        text()
          .verifying("supplementary.packageInformation.shippingMarks.empty", nonEmpty)
          .verifying(
            "supplementary.packageInformation.shippingMarks.characterError",
            isEmpty or isAlphanumericWithAllowedSpecialCharacters
          )
          .verifying("supplementary.packageInformation.shippingMarks.lengthError", isEmpty or noLongerThan(42))
    )(PackageInformation.apply)(PackageInformation.unapply)

  val DUPLICATE_MSG_KEY = "supplementary.packageInformation.global.duplicate"
  val LIMIT_MSG_KEY = "supplementary.packageInformation.global.limit"
  val USE_ADD = "supplementary.packageInformation.global.useAdd"
  val ADD_ONE = "supplementary.packageInformation.global.addOne"

  def form(): Form[PackageInformation] = Form(mapping)
}
