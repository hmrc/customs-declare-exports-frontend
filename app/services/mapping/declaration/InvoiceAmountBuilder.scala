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

package services.mapping.declaration

import forms.declaration.TotalNumberOfItems
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.declaration_ds.dms._2._

object InvoiceAmountBuilder {

  def build(implicit cacheMap: CacheMap): DeclarationInvoiceAmountType =
    cacheMap
      .getEntry[TotalNumberOfItems](TotalNumberOfItems.formId)
      .map(createInvoiceAmount)
      .orNull

  private def createInvoiceAmount(data: TotalNumberOfItems): DeclarationInvoiceAmountType = {
    val invoiceAmountType = new DeclarationInvoiceAmountType()

    data.totalAmountInvoiced.foreach { amount =>
      invoiceAmountType.setValue(new java.math.BigDecimal(amount))
      invoiceAmountType.setCurrencyID("GBP")
    }

    invoiceAmountType
  }
}
