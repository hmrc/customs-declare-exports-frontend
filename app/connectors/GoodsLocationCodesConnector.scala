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

import akka.util.Helpers.Requiring
import config.AppConfig
import models.codes.GoodsLocationCode

import java.util.Locale
import javax.inject.Inject
import scala.collection.immutable.ListMap

class GoodsLocationCodesConnector @Inject() (appConfig: AppConfig) extends FileBasedCodeListFunctions {

  private lazy val glcAirportsByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcAirports16a,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcCoaAirportsByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcCoaAirports16b,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcMaritimeAndWharvesByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcMaritimeAndWharves16c,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcItsfByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcItsf16d,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcRemoteItsfByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcRemoteItsf16e,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcExternalItsfByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcExternalItsf16f,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcBorderInspectionPostsByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcBorderInspectionPosts16g,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcApprovedDipositoriesByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcApprovedDipositories16h,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcPlaceNamesGBByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcPlaceNamesGB16i,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcOtherLocationCodesByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcOtherLocationCodes16j,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcDepByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcDep16k,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcCseByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcCse16l,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcRailByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcRail16m,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcActsByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcActs16n,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcRoroByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcRoro16r,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private lazy val glcGvmsByLang = loadCommonCodesAsOrderedMap(
    appConfig.glcGvms16s,
    (codeItem: CodeItem, locale: Locale) => GoodsLocationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  def getDepCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcDepByLang.getOrElse(locale.getLanguage, glcDepByLang.value.head._2)

  def getAirportsCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcAirportsByLang.getOrElse(locale.getLanguage, glcAirportsByLang.value.head._2)

  def getCoaAirportsCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcCoaAirportsByLang.getOrElse(locale.getLanguage, glcCoaAirportsByLang.value.head._2)

  def getMaritimeAndWharvesCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcMaritimeAndWharvesByLang.getOrElse(locale.getLanguage, glcMaritimeAndWharvesByLang.value.head._2)

  def getItsfCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcItsfByLang.getOrElse(locale.getLanguage, glcItsfByLang.value.head._2)

  def getRemoteItsfCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcRemoteItsfByLang.getOrElse(locale.getLanguage, glcRemoteItsfByLang.value.head._2)

  def getExternalItsfCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcExternalItsfByLang.getOrElse(locale.getLanguage, glcExternalItsfByLang.value.head._2)

  def getBorderInspectionPostsCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcBorderInspectionPostsByLang.getOrElse(locale.getLanguage, glcBorderInspectionPostsByLang.value.head._2)

  def getApprovedDipositoriesCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcApprovedDipositoriesByLang.getOrElse(locale.getLanguage, glcApprovedDipositoriesByLang.value.head._2)

  def getPlaceNamesGBCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcPlaceNamesGBByLang.getOrElse(locale.getLanguage, glcPlaceNamesGBByLang.value.head._2)

  def getOtherLocationCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcOtherLocationCodesByLang.getOrElse(locale.getLanguage, glcOtherLocationCodesByLang.value.head._2)

  def getCseCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcCseByLang.getOrElse(locale.getLanguage, glcCseByLang.value.head._2)

  def getRailCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcRailByLang.getOrElse(locale.getLanguage, glcRailByLang.value.head._2)

  def getActsCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcActsByLang.getOrElse(locale.getLanguage, glcActsByLang.value.head._2)

  def getRoroCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcRoroByLang.getOrElse(locale.getLanguage, glcRoroByLang.value.head._2)

  def getGvmsCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    glcGvmsByLang.getOrElse(locale.getLanguage, glcGvmsByLang.value.head._2)

  def getAllCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    getDepCodes(locale) ++
      getAirportsCodes(locale) ++
      getCoaAirportsCodes(locale) ++
      getMaritimeAndWharvesCodes(locale) ++
      getItsfCodes(locale) ++
      getRemoteItsfCodes(locale) ++
      getExternalItsfCodes(locale) ++
      getBorderInspectionPostsCodes(locale) ++
      getApprovedDipositoriesCodes(locale) ++
      getPlaceNamesGBCodes(locale) ++
      getOtherLocationCodes(locale) ++
      getCseCodes(locale) ++
      getRailCodes(locale) ++
      getActsCodes(locale) ++
      getRoroCodes(locale) ++
      getGvmsCodes(locale)

}
