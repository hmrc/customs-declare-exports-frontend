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
import config.AppConfig
import models.codes.{AdditionalProcedureCode, CommonCode, ProcedureCode}
import play.api.libs.json.{Json, OFormat}
import utils.JsonFile

import java.util.Locale
import java.util.Locale._
import javax.inject.{Inject, Singleton}

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

trait CodeListConnector {
  def getProcedureCodes(locale: Locale): Seq[ProcedureCode]
  def getProcedureCodesForC21(locale: Locale): Seq[ProcedureCode]
  def getAdditionalProcedureCodes(locale: Locale): Seq[AdditionalProcedureCode]
  def getAdditionalProcedureCodesForC21(locale: Locale): Seq[AdditionalProcedureCode]

  val WELSH = new Locale("cy", "GB", "");
}

@Singleton
class FileBasedCodeListConnector @Inject()(appConfig: AppConfig) extends CodeListConnector {

  private implicit val supportedLanguages = Seq(ENGLISH, WELSH)

  private def readCodesFromFile[T <: CommonCode](srcFile: String, factory: (CodeItem, Locale) => T)(implicit locals: Seq[Locale]) = {
    val codeList = JsonFile.getJsonArrayFromFile(srcFile, CodeItem.formats)

    locals.map { locale =>
      val procedureCodes = codeList.map(factory(_, locale))
      (locale -> procedureCodes)
    }.toMap
  }

  private val procedureCodeListsByLang = readCodesFromFile(
    appConfig.procedureCodesListFile,
    (codeItem: CodeItem, locale: Locale) => ProcedureCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private val procedureCodeForC21ListsByLang = readCodesFromFile(
    appConfig.procedureCodesForC21ListFile,
    (codeItem: CodeItem, locale: Locale) => ProcedureCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private val additionalProcedureCodeListsByLang = readCodesFromFile(
    appConfig.additionalProcedureCodes,
    (codeItem: CodeItem, locale: Locale) => AdditionalProcedureCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  private val additionalProcedureCodeForC21ListsByLang = readCodesFromFile(
    appConfig.additionalProcedureCodesForC21,
    (codeItem: CodeItem, locale: Locale) => AdditionalProcedureCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  def getProcedureCodes(locale: Locale): Seq[ProcedureCode] =
    procedureCodeListsByLang.getOrElse(locale, procedureCodeListsByLang.value.head._2)

  def getProcedureCodesForC21(locale: Locale): Seq[ProcedureCode] =
    procedureCodeForC21ListsByLang.getOrElse(locale, procedureCodeForC21ListsByLang.value.head._2)

  def getAdditionalProcedureCodes(locale: Locale): Seq[AdditionalProcedureCode] =
    additionalProcedureCodeListsByLang.getOrElse(locale, additionalProcedureCodeListsByLang.value.head._2)

  def getAdditionalProcedureCodesForC21(locale: Locale): Seq[AdditionalProcedureCode] =
    additionalProcedureCodeForC21ListsByLang.getOrElse(locale, additionalProcedureCodeForC21ListsByLang.value.head._2)
}
