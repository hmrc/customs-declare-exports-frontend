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

import javax.inject.Inject
import models.ExportsDeclaration
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.declaration_ds.dms._2._

class InvoiceAmountBuilder @Inject()() extends ModifyingBuilder[ExportsDeclaration, Declaration] {

  override def buildThenAdd(model: ExportsDeclaration, declaration: Declaration): Unit =
    model.totalNumberOfItems
      .flatMap(_.totalAmountInvoiced)
      .map(createInvoiceAmount)
      .foreach(declaration.setInvoiceAmount)

  private def createInvoiceAmount(amount: String): DeclarationInvoiceAmountType = {
    val invoiceAmountType = new DeclarationInvoiceAmountType()

    invoiceAmountType.setValue(new java.math.BigDecimal(amount))
    invoiceAmountType.setCurrencyID("GBP")

    invoiceAmountType
  }
}
