/*
 * Copyright 2021 HM Revenue & Customs
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

package config

import javax.inject.Singleton

@Singleton
class Guidance(
  val additionalDocumentsReferenceCodes: String,
  val additionalDocumentsUnionCodes: String,
  val aiCodes: String,
  val aiCodesForContainers: String,
  val cdsTariffCompletionGuide: String,
  val clearingGoodsFromToUK: String,
  val commodityCode0306310010: String,
  val commodityCode2208303000: String,
  val commodityCode9306909000: String,
  val commodityCodes: String,
  val specialProcedures: String,
  val vatOnGoodsExportedFromUK: String,
  val vatRatingForStandardExport: String
)

object Guidance {
  def apply(loadConfig: String => String): Guidance =
    new Guidance(
      additionalDocumentsReferenceCodes = loadConfig("guidance.additionalDocumentsReferenceCodes"),
      additionalDocumentsUnionCodes = loadConfig("guidance.additionalDocumentsUnionCodes"),
      aiCodes = loadConfig("guidance.aiCodes"),
      aiCodesForContainers = loadConfig("guidance.aiCodesForContainers"),
      cdsTariffCompletionGuide = loadConfig("guidance.cdsTariffCompletionGuide"),
      clearingGoodsFromToUK = loadConfig("guidance.clearingGoodsFromToUK"),
      commodityCode0306310010 = loadConfig("guidance.commodityCode0306310010"),
      commodityCode2208303000 = loadConfig("guidance.commodityCode2208303000"),
      commodityCode9306909000 = loadConfig("guidance.commodityCode9306909000"),
      commodityCodes = loadConfig("guidance.commodityCodes"),
      specialProcedures = loadConfig("guidance.specialProcedures"),
      vatOnGoodsExportedFromUK = loadConfig("guidance.vatOnGoodsExportedFromUK"),
      vatRatingForStandardExport = loadConfig("guidance.vatRatingForStandardExport")
    )
}
