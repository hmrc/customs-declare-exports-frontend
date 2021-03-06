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

package connectors

import akka.util.Helpers.Requiring
import com.google.inject.ImplementedBy
import config.AppConfig
import models.codes.{AdditionalProcedureCode, CommonCode, HolderOfAuthorisationCode, ProcedureCode}
import play.api.libs.json.{Json, OFormat}
import utils.JsonFile
import java.util.Locale
import java.util.Locale._

import javax.inject.{Inject, Singleton}
import scala.collection.immutable.ListMap

case class CodeItem(code: String, en: String, cy: String) {
  def getDescriptionByLocale(locale: Locale): String =
    locale.getLanguage match {
      case "cy" => cy
      case _    => en
    }
}

object CodeItem {
  implicit val formats: OFormat[CodeItem] = Json.format[CodeItem]
}

@ImplementedBy(classOf[FileBasedCodeListConnector])
trait CodeListConnector {

  type CodeMap[T <: CommonCode] = Map[String, ListMap[String, T]]

  def getHolderOfAuthorisationCodes(locale: Locale): ListMap[String, HolderOfAuthorisationCode]
  def getProcedureCodes(locale: Locale): ListMap[String, ProcedureCode]
  def getProcedureCodesForC21(locale: Locale): ListMap[String, ProcedureCode]
  def getAdditionalProcedureCodesMap(locale: Locale): ListMap[String, AdditionalProcedureCode]
  def getAdditionalProcedureCodesMapForC21(locale: Locale): ListMap[String, AdditionalProcedureCode]

  val WELSH = new Locale("cy", "GB", "");
  val supportedLanguages = Seq(ENGLISH, WELSH)
}

@Singleton
class FileBasedCodeListConnector @Inject()(appConfig: AppConfig) extends CodeListConnector {

  private def loadCommonCodesAsOrderedMap[T <: CommonCode](srcFile: String, factory: (CodeItem, Locale) => T): CodeMap[T] = {
    val codeList = JsonFile.getJsonArrayFromFile(srcFile, CodeItem.formats)

    val langCodes = supportedLanguages.map { locale =>
      val commonCodeList = codeList
        .map(factory(_, locale))
        .map(commonCode => (commonCode.code, commonCode))

      (locale.getLanguage -> ListMap(commonCodeList: _*))
    }

    ListMap(langCodes: _*)
  }

  private val holderOfAuthorisationCodeListsByLang = loadCommonCodesAsOrderedMap(
    appConfig.holderOfAuthorisationCodes,
    (codeItem: CodeItem, locale: Locale) => HolderOfAuthorisationCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private val procedureCodeListsByLang = loadCommonCodesAsOrderedMap(
    appConfig.procedureCodesListFile,
    (codeItem: CodeItem, locale: Locale) => ProcedureCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private val procedureCodeForC21ListsByLang = loadCommonCodesAsOrderedMap(
    appConfig.procedureCodesForC21ListFile,
    (codeItem: CodeItem, locale: Locale) => ProcedureCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private val additionalProcedureCodeMapsByLang = loadCommonCodesAsOrderedMap(
    appConfig.additionalProcedureCodes,
    (codeItem: CodeItem, locale: Locale) => AdditionalProcedureCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private val additionalProcedureCodeForC21MapsByLang = loadCommonCodesAsOrderedMap(
    appConfig.additionalProcedureCodesForC21,
    (codeItem: CodeItem, locale: Locale) => AdditionalProcedureCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  def getHolderOfAuthorisationCodes(locale: Locale): ListMap[String, HolderOfAuthorisationCode] =
    holderOfAuthorisationCodeListsByLang.getOrElse(locale.getLanguage, holderOfAuthorisationCodeListsByLang.value.head._2)

  def getProcedureCodes(locale: Locale): ListMap[String, ProcedureCode] =
    procedureCodeListsByLang.getOrElse(locale.getLanguage, procedureCodeListsByLang.value.head._2)

  def getProcedureCodesForC21(locale: Locale): ListMap[String, ProcedureCode] =
    procedureCodeForC21ListsByLang.getOrElse(locale.getLanguage, procedureCodeForC21ListsByLang.value.head._2)

  def getAdditionalProcedureCodesMap(locale: Locale): ListMap[String, AdditionalProcedureCode] =
    additionalProcedureCodeMapsByLang.getOrElse(locale.getLanguage, additionalProcedureCodeMapsByLang.value.head._2)

  def getAdditionalProcedureCodesMapForC21(locale: Locale): ListMap[String, AdditionalProcedureCode] =
    additionalProcedureCodeForC21MapsByLang.getOrElse(locale.getLanguage, additionalProcedureCodeForC21MapsByLang.value.head._2)
}
