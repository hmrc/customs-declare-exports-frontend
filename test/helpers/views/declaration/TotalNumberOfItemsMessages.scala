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

  val valueOfItems: String = "supplementary.valueOfItems"

  val totalNoOfItemsTitle: String = "supplementary.totalNumberOfItems.title"
  val totalNoOfItemsHint: String = "supplementary.totalNumberOfItems.hint"
  val totalNoOfItemsEmpty: String = "supplementary.totalNumberOfItems.empty"
  val totalNoOfItemsError: String = "supplementary.totalNumberOfItems.error"

  val totalPackageQuantity: String = "supplementary.totalPackageQuantity"

  val totalPackageQuantityHint: String = "supplementary.totalPackageQuantity.hint"
  val totalPackageQuantityEmpty: String = "supplementary.totalPackageQuantity.empty"
  val totalPackageQuantityError: String = "supplementary.totalPackageQuantity.error"

  val totalAmountInvoiced: String = "supplementary.totalAmountInvoiced"

  val totalAmountInvoicedHint: String = "supplementary.totalAmountInvoiced.hint"
  val totalAmountInvoicedError: String = "supplementary.totalAmountInvoiced.error"

  val exchangeRate: String = "supplementary.exchangeRate"

  val exchangeRateHint: String = "supplementary.exchangeRate.hint"
  val exchangeRateError: String = "supplementary.exchangeRate.error"
}
