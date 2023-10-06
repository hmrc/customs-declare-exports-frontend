/*
 * Copyright 2023 HM Revenue & Customs
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
  val addATeamMember: String,
  val additionalDocumentsReferenceCodes: String,
  val additionalDocumentsUnionCodes: String,
  val aiCodes: String,
  val aiCodesForContainers: String,
  val cdsDeclarationSoftware: String,
  val cdsRegister: String,
  val cdsTariffCompletionGuide: String,
  val clearingGoodsFromToUK: String,
  val commodityCode0306310010: String,
  val commodityCode2208303000: String,
  val gvms: String,
  val commodityCodes: String,
  val eoriService: String,
  val exportingByPost: String,
  val manageYourEmailAddress: String,
  val someoneToDealWithCustomsOnYourBehalf: String,
  val specialProcedures: String,
  val takingCommercialGoodsOnYourPerson: String,
  val vatOnGoodsExportedFromUK: String,
  val vatRatingForStandardExport: String,
  val moveGoodsThroughPortsUsingGVMS: String,
  val january2022locations: String
)

object Guidance {
  def apply(loadConfig: String => String): Guidance =
    new Guidance(
      addATeamMember = loadConfig("guidance.addATeamMember"),
      additionalDocumentsReferenceCodes = loadConfig("guidance.additionalDocumentsReferenceCodes"),
      additionalDocumentsUnionCodes = loadConfig("guidance.additionalDocumentsUnionCodes"),
      aiCodes = loadConfig("guidance.aiCodes"),
      aiCodesForContainers = loadConfig("guidance.aiCodesForContainers"),
      cdsDeclarationSoftware = loadConfig("guidance.cdsDeclarationSoftware"),
      cdsRegister = loadConfig("guidance.cdsRegister"),
      cdsTariffCompletionGuide = loadConfig("guidance.cdsTariffCompletionGuide"),
      clearingGoodsFromToUK = loadConfig("guidance.clearingGoodsFromToUK"),
      commodityCode0306310010 = loadConfig("guidance.commodityCode0306310010"),
      commodityCode2208303000 = loadConfig("guidance.commodityCode2208303000"),
      gvms = loadConfig("guidance.gvms"),
      commodityCodes = loadConfig("guidance.commodityCodes"),
      eoriService = loadConfig("guidance.eoriService"),
      exportingByPost = loadConfig("guidance.exportingByPost"),
      manageYourEmailAddress = loadConfig("guidance.manageYourEmailAddress"),
      someoneToDealWithCustomsOnYourBehalf = loadConfig("guidance.someoneToDealWithCustomsOnYourBehalf"),
      specialProcedures = loadConfig("guidance.specialProcedures"),
      takingCommercialGoodsOnYourPerson = loadConfig("guidance.takingCommercialGoodsOnYourPerson"),
      vatOnGoodsExportedFromUK = loadConfig("guidance.vatOnGoodsExportedFromUK"),
      vatRatingForStandardExport = loadConfig("guidance.vatRatingForStandardExport"),
      moveGoodsThroughPortsUsingGVMS = loadConfig("guidance.moveGoodsThroughPortsUsingGVMS"),
      january2022locations = loadConfig("guidance.january2022locations")
    )
}
