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

trait ItemSummaryMessages {

  val prefix: String = "supplementary"

  val title: String = prefix + ".items"
  val header: String = prefix + ".items"
  val noItemsAddedHeader: String = prefix + ".itemsAdd.title"
  val oneItemAddedHeader: String = prefix + ".itemsAdd.titleWithItem"
  val manyItemsAddedHeader: String = prefix + ".itemsAdd.titleWithItems"
  val hint: String = prefix + ".itemsAdd.title.hint"
  val tableItemNumber: String = prefix + ".itemsSummary.itemNumber"
  val tableProcedureCode: String = prefix + ".itemsSummary.procedureCode"
  val tableCommodityCode: String = prefix + ".itemsSummary.commodityCode"
  val tablePackageCount: String = prefix + ".itemsSummary.noOfPackages"
  val changeItem: String = "site.change"
  val removeItem: String = "site.remove"
  val addItem: String = "site.add.item"
  val addAnotherItem: String = "site.add.anotherItem"
  val continue: String = "site.save_and_continue"
}
