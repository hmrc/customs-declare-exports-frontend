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

trait TotalNumberOfItemsMessages {

  val totalNumberOfItems: String = "supplementary.totalNumberOfItems"

  val tnoiTitle: String = totalNumberOfItems + ".title"
  val tnoiHint: String = totalNumberOfItems + ".hint"
  val tnoiEmpty: String = totalNumberOfItems + ".empty"
  val tnoiError: String = totalNumberOfItems + ".error"

  val totalPackageQuantity: String = "supplementary.totalPackageQuantity"

  val tpqHint: String = totalPackageQuantity + ".hint"
  val tpqEmpty: String = totalPackageQuantity + ".empty"
  val tpqError: String = totalPackageQuantity + ".error"

  val totalAmountInvoiced: String = "supplementary.totalAmountInvoiced"

  val taiHint: String = totalAmountInvoiced + ".hint"
  val taiEmpty: String = totalAmountInvoiced + ".empty"
  val taiError: String = totalAmountInvoiced + ".error"

  val exchangeRate: String = "supplementary.exchangeRate"

  val erHint: String = exchangeRate + ".hint"
  val erEmpty: String = exchangeRate + ".empty"
  val erError: String = exchangeRate + ".error"
}
