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

package services.mapping.goodsshipment

import forms.declaration.TransactionType
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.declaration_ds.dms._2.GoodsShipmentTransactionNatureCodeType

object GoodsShipmentTransactionTypeBuilder {

  def build(implicit cacheMap: CacheMap): GoodsShipmentTransactionNatureCodeType =
    cacheMap
      .getEntry[TransactionType](TransactionType.formId)
      .filter(transactionType => isDefined(transactionType))
      .map(createTransactionNatureCode)
      .orNull

  private def isDefined(transactionType: TransactionType): Boolean =
    transactionType.documentTypeCode.nonEmpty &&
      transactionType.identifier.getOrElse("").nonEmpty

  private def createTransactionNatureCode(data: TransactionType): GoodsShipmentTransactionNatureCodeType = {

    val transactionType = new GoodsShipmentTransactionNatureCodeType()
    transactionType.setValue(data.documentTypeCode + data.identifier.getOrElse(""))

    transactionType
  }

}
