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

package helpers.views.declaration

trait PackageInformationMessages {

  val packageInformation: String = "supplementary.packageInformation"

  val title: String = packageInformation + ".title"
  val remove: String = packageInformation + ".remove"
  val tableHeading: String = packageInformation + ".table.heading"
  val tableMultipleHeading: String = packageInformation + ".table.multiple.heading"
  val typesOfPackages: String = packageInformation + ".typesOfPackages"
  val typesOfPackagesHint: String = packageInformation + ".typesOfPackages.hint"
  val typesOfPackagesError: String = packageInformation + ".typesOfPackages.error"
  val typesOfPackagesEmpty: String = packageInformation + ".typesOfPackages.empty"
  val numberOfPackages: String = packageInformation + ".numberOfPackages"
  val numberOfPackagesError: String = packageInformation + ".numberOfPackages.error"
  val numberOfPackagesEmpty: String = packageInformation + ".numberOfPackages.empty"
  val piGlobalDuplicate: String = packageInformation + ".global.duplicate"
  val piGlobalLimit: String = packageInformation + ".global.limit"
  val piGlobalUseAdd: String = packageInformation + ".global.useAdd"
  val piGlobalAddOne: String = packageInformation + ".global.addOne"
  val shippingMarks: String = packageInformation + ".shippingMarks"
  val shippingMarksHint: String = packageInformation + ".shippingMarks.hint"
  val shippingMarksError: String = packageInformation + ".shippingMarks.error"
  val shippingMarksEmpty: String = packageInformation + ".shippingMarks.empty"
}
