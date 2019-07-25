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

import java.util

import forms.declaration.TotalNumberOfItems
import javax.inject.Inject
import services.cache.ExportsCacheModel
import services.mapping.declaration.CurrencyExchangeBuilder.createCurrencyExchange
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.CurrencyExchange

import scala.collection.JavaConverters._

class CurrencyExchangeBuilder @Inject()(){
  def buildThenAdd(model: ExportsCacheModel, declaration: Declaration): Unit = {
    val currencyExchanges: Seq[CurrencyExchange] = model.totalNumberOfItems
      .filter(_.exchangeRate.isDefined)
      .map(createCurrencyExchange)
      .getOrElse(Seq.empty)
    declaration.getCurrencyExchange.addAll(currencyExchanges.toList.asJava)
  }
}

object CurrencyExchangeBuilder {

  def build(implicit cacheMap: CacheMap): util.List[CurrencyExchange] =
    cacheMap
      .getEntry[TotalNumberOfItems](TotalNumberOfItems.formId)
      .filter(_.exchangeRate.isDefined)
      .map(createCurrencyExchange)
      .map(_.toList.asJava)
      .orNull

  private def createCurrencyExchange(data: TotalNumberOfItems): Seq[CurrencyExchange] = {
    val currencyExchange = new CurrencyExchange()

    data.exchangeRate.foreach(rate => currencyExchange.setRateNumeric(new java.math.BigDecimal(rate)))

    Seq(currencyExchange)
  }
}
