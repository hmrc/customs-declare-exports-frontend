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

package connectors

import config.AppConfig
import models.codes.GoodsLocationCode

import java.util.Locale
import javax.inject.Inject

class GoodsLocationCodesConnector @Inject() (appConfig: AppConfig) extends FileBasedCodeListFunctions {

  val glcAirportsByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcAirports16a,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcCoaAirportsByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcCoaAirports16b,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcMaritimeAndWharvesByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcMaritimeAndWharves16c,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcItsfByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcItsf16d,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcRemoteItsfByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcRemoteItsf16e,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcExternalItsfByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcRemoteItsf16e,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcBorderInspectionPostsByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcBorderInspectionPosts16g,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcApprovedDipositoriesByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcApprovedDipositories16h,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcPlaceNamesGBByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcPlaceNamesGB16i,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcOtherLocationCodesByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcOtherLocationCodes16j,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcDepByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcDep16k,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcCseByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcCse16l,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcRailByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcRail16m,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcActsByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcActs16n,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  val glcRoroByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcRoro16r,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

}
