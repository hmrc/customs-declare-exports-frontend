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
import services.PackageType
import utils.validators.forms.FieldValidator._

case class PackageInformation(
  typesOfPackages: Option[String],
  numberOfPackages: Option[Int],
  shippingMarks: Option[String]
)

object PackageInformation {

  def require1Field[T](fs: (T => Option[_])*): T => Boolean =
    t => fs.exists(f => f(t).nonEmpty)

  implicit val format = Json.format[PackageInformation]

  val formId = "PackageInformation"

  val mapping = Forms
    .mapping(
      "typesOfPackages" -> optional(
        text()
          .verifying(
            "supplementary.packageInformation.typesOfPackages.error",
            isEmpty or isContainedIn(PackageType.all.map(_.code))
          )
          .verifying("supplementary.packageInformation.typesOfPackages.empty", nonEmpty)
      ),
      "numberOfPackages" ->
        optional(
          number.verifying("supplementary.packageInformation.numberOfPackages.error", (q => q > 0 && q <= 999999))
        ),
      "shippingMarks" -> optional(
        text()
          .verifying(
            "supplementary.packageInformation.shippingMarks.characterError",
            isEmpty or isAlphanumericWithAllowedSpecialCharacters
          )
          .verifying("supplementary.packageInformation.shippingMarks.lengthError", isEmpty or noLongerThan(42))
          .verifying("supplementary.packageInformation.shippingMarks.empty", nonEmpty)
      )
    )(PackageInformation.apply)(PackageInformation.unapply)
    .verifying(
      "You must provide 6/9 item packaged, 6/10 Shipping Marks, 6/11 Number of Packages  for a package to be added",
      require1Field[PackageInformation](_.typesOfPackages, _.numberOfPackages, _.shippingMarks)
    )

  val DUPLICATE_MSG_KEY = "supplementary.packageInformation.global.duplicate"
  val LIMIT_MSG_KEY = "supplementary.packageInformation.global.limit"
  val USE_ADD = "supplementary.packageInformation.global.useAdd"
  val ADD_ONE = "supplementary.packageInformation.global.addOne"

  def form(): Form[PackageInformation] = Form(mapping)
}
