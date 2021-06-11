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

package services.view

import java.util.Locale
import java.util.Locale.ENGLISH

import scala.collection.immutable.ListMap

import config.AppConfig
import connectors.{CodeItem, CodeListConnector}
import javax.inject.{Inject, Singleton}

@Singleton
class HolderOfAuthorisationCodes @Inject()(appConfig: AppConfig, codeListConnector: CodeListConnector) {

  private val codesByLang = codeListConnector.loadCodesAsOrderedMap(
    appConfig.holderOfAuthorisationCodes,
    (codeItem: CodeItem, locale: Locale) => codeItem.code -> s"${codeItem.code} - ${codeItem.getDescriptionByLocale(locale)}"
  )

  def getCodes(locale: Locale): ListMap[String, String] =
    codesByLang.get(locale.getLanguage).getOrElse(codesByLang.getOrElse(ENGLISH.getLanguage, codesByLang.head._2))

  def getCodeDescription(locale: Locale, code: String): String =
    getCodes(locale).getOrElse(code, "")

  def asListOfAutoCompleteItems(locale: Locale): List[AutoCompleteItem] =
    getCodes(locale).map(t => AutoCompleteItem(t._2, t._1)).toList
}
