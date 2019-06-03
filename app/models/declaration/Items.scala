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

package models.declaration

import forms.MetadataPropertiesConvertable
import forms.declaration._
import uk.gov.hmrc.http.cache.client.CacheMap

case class Items(
  totalNumberOfItems: Option[TotalNumberOfItems] = None,
  transactionType: Option[TransactionType] = None
) extends SummaryContainer with MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Seq(
      totalNumberOfItems.map(_.toMetadataProperties()),
      transactionType.map(_.toMetadataProperties())
    ).flatten.fold(Map.empty)(_ ++ _)

  override def isEmpty: Boolean =
    totalNumberOfItems.isEmpty &&
      transactionType.isEmpty
}

object Items {
  val id = "Items"

  def apply(cacheMap: CacheMap): Items = Items(
    totalNumberOfItems = cacheMap.getEntry[TotalNumberOfItems](TotalNumberOfItems.formId),
    transactionType = cacheMap.getEntry[TransactionType](TransactionType.formId))
}
