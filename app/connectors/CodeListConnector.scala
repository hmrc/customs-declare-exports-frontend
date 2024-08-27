/*
 * Copyright 2024 HM Revenue & Customs
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

import org.apache.pekko.util.Helpers.Requiring
import com.google.inject.ImplementedBy
import config.AppConfig
import models.codes._
import play.api.libs.json.{Json, OFormat}
import play.api.Logging
import services.model.{CustomsOffice, OfficeOfExit, PackageType}
import services.DocumentType
import utils.JsonFile

import java.util.Locale
import java.util.Locale.ENGLISH
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

  def getAdditionalProcedureCodesMap(locale: Locale): ListMap[String, AdditionalProcedureCode]
  def getAdditionalProcedureCodesMapForC21(locale: Locale): ListMap[String, AdditionalProcedureCode]
  def getCountryCodes(locale: Locale): ListMap[String, Country]
  def getDmsErrorCodesMap(locale: Locale): ListMap[String, DmsErrorCode]
  def getHolderOfAuthorisationCodes(locale: Locale): ListMap[String, HolderOfAuthorisationCode]
  def getProcedureCodes(locale: Locale): ListMap[String, ProcedureCode]
  def getProcedureCodesForC21(locale: Locale): ListMap[String, ProcedureCode]
  def getPackageTypes(locale: Locale): ListMap[String, PackageType]
  def getOfficeOfExits(locale: Locale): ListMap[String, OfficeOfExit]
  def getCustomsOffices(locale: Locale): ListMap[String, CustomsOffice]

  def getDepCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getAirportsCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getCoaAirportsCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getMaritimeAndWharvesCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getItsfCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getRemoteItsfCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getExternalItsfCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getBorderInspectionPostsCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getApprovedDipositoriesCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getPlaceNamesGBCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getOtherLocationCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getCseCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getRailCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getActsCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getRoroCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getGvmsCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def allGoodsLocationCodes(locale: Locale): ListMap[String, GoodsLocationCode]
  def getDocumentTypes(locale: Locale): ListMap[String, DocumentType]
  def getCurrencyCodes(locale: Locale): ListMap[String, CurrencyCode]

}

@Singleton
class FileBasedCodeListConnector @Inject() (
  appConfig: AppConfig,
  goodsLocationCodesConnector: GoodsLocationCodesConnector,
  val jsonFileReader: JsonFile
) extends CodeListConnector with FileBasedCodeListFunctions {

  private lazy val standardOrCustomErrorDefinitionFile =
    if (appConfig.isUsingImprovedErrorMessages) {
      val pathParts = appConfig.dmsErrorCodes.split('.')

      val customFilePath = for {
        start <- pathParts.headOption
        end <- pathParts.lastOption
      } yield s"${start}-customised.${end}"

      customFilePath.getOrElse(appConfig.dmsErrorCodes)
    } else appConfig.dmsErrorCodes

  private val additionalProcedureCodeMapsByLang = loadCommonCodesAsOrderedMap(
    appConfig.additionalProcedureCodes,
    (codeItem: CodeItem, locale: Locale) => AdditionalProcedureCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )
  private val additionalProcedureCodeForC21MapsByLang = loadCommonCodesAsOrderedMap(
    appConfig.additionalProcedureCodesForC21,
    (codeItem: CodeItem, locale: Locale) => AdditionalProcedureCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )
  private val countryListByLang = loadCommonCodesAsOrderedMap(
    appConfig.countryCodes,
    (codeItem: CodeItem, locale: Locale) => Country(codeItem.getDescriptionByLocale(locale), codeItem.code)
  )
  private val dmsErrorCodeMapsByLang = loadCommonCodesAsOrderedMap(
    standardOrCustomErrorDefinitionFile,
    (codeItem: CodeItem, locale: Locale) => DmsErrorCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )
  private val holderOfAuthorisationCodeListsByLang = loadCommonCodesAsOrderedMap(
    appConfig.holderOfAuthorisationCodeFile,
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
  private val packageTypeCodeByLang = loadCommonCodesAsOrderedMap(
    appConfig.packageTypeCodeFile,
    (codeItem: CodeItem, locale: Locale) => PackageType(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )
  private val officeOfExitCodesByLang = loadCommonCodesAsOrderedMap(
    appConfig.officeOfExitsCodeFile,
    (codeItem: CodeItem, locale: Locale) => OfficeOfExit(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )
  private val customsOfficesCodesByLang = loadCommonCodesAsOrderedMap(
    appConfig.customsOfficesCodeFile,
    (codeItem: CodeItem, locale: Locale) => CustomsOffice(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )
  private val documentTypeCodesByLang = loadCommonCodesAsOrderedMap(
    appConfig.documentTypeCodeFile,
    (codeItem: CodeItem, locale: Locale) => DocumentType(codeItem.getDescriptionByLocale(locale), codeItem.code)
  )
  private val currencyCodesByLang = loadCommonCodesAsOrderedMap(
    appConfig.currencyCodesFile,
    (codeItem: CodeItem, locale: Locale) => CurrencyCode(codeItem.code, codeItem.getDescriptionByLocale(locale))
  )

  def getAdditionalProcedureCodesMap(locale: Locale): ListMap[String, AdditionalProcedureCode] =
    additionalProcedureCodeMapsByLang.getOrElse(locale.getLanguage, additionalProcedureCodeMapsByLang.value.head._2)

  def getAdditionalProcedureCodesMapForC21(locale: Locale): ListMap[String, AdditionalProcedureCode] =
    additionalProcedureCodeForC21MapsByLang.getOrElse(locale.getLanguage, additionalProcedureCodeForC21MapsByLang.value.head._2)

  def getCountryCodes(locale: Locale): ListMap[String, Country] =
    countryListByLang.getOrElse(locale.getLanguage, countryListByLang.value.head._2)

  def getDmsErrorCodesMap(locale: Locale): ListMap[String, DmsErrorCode] =
    dmsErrorCodeMapsByLang.getOrElse(locale.getLanguage, dmsErrorCodeMapsByLang.value.head._2)

  def getHolderOfAuthorisationCodes(locale: Locale): ListMap[String, HolderOfAuthorisationCode] =
    holderOfAuthorisationCodeListsByLang.getOrElse(locale.getLanguage, holderOfAuthorisationCodeListsByLang.value.head._2)

  def getProcedureCodes(locale: Locale): ListMap[String, ProcedureCode] =
    procedureCodeListsByLang.getOrElse(locale.getLanguage, procedureCodeListsByLang.value.head._2)

  def getProcedureCodesForC21(locale: Locale): ListMap[String, ProcedureCode] =
    procedureCodeForC21ListsByLang.getOrElse(locale.getLanguage, procedureCodeForC21ListsByLang.value.head._2)

  def getPackageTypes(locale: Locale): ListMap[String, PackageType] =
    packageTypeCodeByLang.getOrElse(locale.getLanguage, packageTypeCodeByLang.value.head._2)

  def getOfficeOfExits(locale: Locale): ListMap[String, OfficeOfExit] =
    officeOfExitCodesByLang.getOrElse(locale.getLanguage, officeOfExitCodesByLang.value.head._2)

  def getCustomsOffices(locale: Locale): ListMap[String, CustomsOffice] =
    customsOfficesCodesByLang.getOrElse(locale.getLanguage, customsOfficesCodesByLang.value.head._2)

  def getDocumentTypes(locale: Locale): ListMap[String, DocumentType] =
    documentTypeCodesByLang.getOrElse(locale.getLanguage, documentTypeCodesByLang.value.head._2)

  def getCurrencyCodes(locale: Locale): ListMap[String, CurrencyCode] =
    currencyCodesByLang.getOrElse(locale.getLanguage, currencyCodesByLang.value.head._2)

  override def getDepCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getDepCodes(locale)
  override def getAirportsCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getAirportsCodes(locale)
  override def getCoaAirportsCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getCoaAirportsCodes(locale)
  override def getMaritimeAndWharvesCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getMaritimeAndWharvesCodes(locale)
  override def getItsfCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getItsfCodes(locale)
  override def getRemoteItsfCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getRemoteItsfCodes(locale)
  override def getExternalItsfCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getExternalItsfCodes(locale)
  override def getBorderInspectionPostsCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getBorderInspectionPostsCodes(locale)
  override def getApprovedDipositoriesCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getApprovedDipositoriesCodes(locale)
  override def getPlaceNamesGBCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getPlaceNamesGBCodes(locale)
  override def getOtherLocationCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getOtherLocationCodes(locale)
  override def getCseCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getCseCodes(locale)
  override def getRailCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getRailCodes(locale)
  override def getActsCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getActsCodes(locale)
  override def getRoroCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getRoroCodes(locale)
  override def getGvmsCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getGvmsCodes(locale)
  override def allGoodsLocationCodes(locale: Locale): ListMap[String, GoodsLocationCode] =
    goodsLocationCodesConnector.getAllCodes(locale)
}

trait FileBasedCodeListFunctions extends Logging {

  type CodeMap[T <: CommonCode] = Map[String, ListMap[String, T]]

  val WELSH = new Locale("cy", "GB", "")
  val supportedLanguages = Seq(ENGLISH, WELSH)
  val jsonFileReader: JsonFile

  protected def loadCommonCodesAsOrderedMap[T <: CommonCode](srcFile: String, factory: (CodeItem, Locale) => T): CodeMap[T] = {
    logger.info(s"Loading CodeListConnector Reference data file '$srcFile'")
    val codeList = jsonFileReader.getJsonArrayFromFile(srcFile, CodeItem.formats)

    val langCodes = supportedLanguages.map { locale =>
      val commonCodeList = codeList
        .map(factory(_, locale))
        .map(commonCode => (commonCode.code, commonCode))

      locale.getLanguage -> ListMap(commonCodeList: _*)
    }

    ListMap(langCodes: _*)
  }
}
