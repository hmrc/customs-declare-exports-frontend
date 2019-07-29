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

package services.cache

import java.util.UUID

import models.declaration.ProcedureCodesData

trait ExportsCacheItemBuilder {

  private def uuid: String = UUID.randomUUID().toString

  private val modelWithDefaults: ExportItem = ExportItem(id = uuid)

  private type CachedItemModifier = ExportItem => ExportItem

  def aCachedItem(modifiers: (CachedItemModifier)*): ExportItem =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  // ************************************************* Builders ********************************************************

  def withProcedureCodes(procedureCode: Option[String], additionalProcedureCodes: Seq[String]): CachedItemModifier =
    _.copy(procedureCodes = Some(ProcedureCodesData(procedureCode, additionalProcedureCodes)))
}
