/*
 * Copyright 2022 HM Revenue & Customs
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
import models.ExportsDeclaration

case class Items(totalNumberOfItems: Option[TotalNumberOfItems] = None, natureOfTransaction: Option[NatureOfTransaction] = None)

object Items {
  val id = "Items"

  def apply(cacheData: ExportsDeclaration): Items =
    Items(totalNumberOfItems = cacheData.totalNumberOfItems, natureOfTransaction = cacheData.natureOfTransaction)
}
