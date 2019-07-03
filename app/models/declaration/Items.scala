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

import forms.declaration._
import uk.gov.hmrc.http.cache.client.CacheMap

case class Items(
  totalNumberOfItems: Option[TotalNumberOfItems] = None,
  natureOfTransaction: Option[NatureOfTransaction] = None
) extends SummaryContainer {

  override def isEmpty: Boolean = totalNumberOfItems.isEmpty && natureOfTransaction.isEmpty
}

object Items {
  val id = "Items"

  def apply(cacheMap: CacheMap): Items =
    Items(
      totalNumberOfItems = cacheMap.getEntry[TotalNumberOfItems](TotalNumberOfItems.formId),
      natureOfTransaction = cacheMap.getEntry[NatureOfTransaction](NatureOfTransaction.formId)
    )
}
