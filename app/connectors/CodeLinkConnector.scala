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

package connectors

import com.google.inject.ImplementedBy
import config.AppConfig
import connectors.Tag.Tag
import models.codes.CommonCode
import play.api.libs.json.{Json, OFormat}
import utils.JsonFile

import javax.inject.{Inject, Singleton}

case class CodeLink(parentCode: String, childCodes: Seq[String])

object CodeLink {
  implicit val formats: OFormat[CodeLink] = Json.format[CodeLink]
}

object Tag extends Enumeration {
  type Tag = Value

  val CodesMutuallyExclusive, CodesNeedingSpecificHintText, CodesOverridingInlandOrBorderSkip, CodesRequiringDocumentation, CodesRestrictingZeroVat,
    CodesSkippingInlandOrBorder, CodesSkippingLocationOfGoods = Value
}

@ImplementedBy(classOf[FileBasedCodeLinkConnector])
trait CodeLinkConnector {
  def getHolderOfAuthorisationCodesForTag(tag: Tag): Seq[String]

  def getValidAdditionalProcedureCodesForProcedureCode(procedureCode: String): Option[Seq[String]]
  def getValidAdditionalProcedureCodesForProcedureCodeC21(procedureCode: String): Option[Seq[String]]
  def getValidProcedureCodesForTag(tag: Tag): Seq[String]

  def getAliasesForCountryCode(countryCode: String): Option[Seq[String]]
  def getShortNamesForCountryCode(countryCode: String): Option[Seq[String]]

  def getLocationTypesForGoodsLocationCode(goodsLocationCode: String): Option[Seq[String]]
}

@Singleton
class FileBasedCodeLinkConnector @Inject() (appConfig: AppConfig) extends CodeLinkConnector {

  private def readCodeLinksFromFile[T <: CommonCode](srcFile: String): Seq[(String, Seq[String])] = {
    val codeLinks = JsonFile.getJsonArrayFromFile(srcFile, CodeLink.formats)
    codeLinks.map { codeLink =>
      codeLink.parentCode -> codeLink.childCodes
    }
  }

  private def readCodeLinksFromFileAsMap[T <: CommonCode](srcFile: String): Map[String, Seq[String]] =
    readCodeLinksFromFile(srcFile).toMap

  private val taggedHolderOfAuthorisationCodes: Seq[(String, Seq[String])] =
    readCodeLinksFromFile(appConfig.taggedHolderOfAuthorisationCodeFile)

  private val procedureCodeToAdditionalProcedureCodes: Map[String, Seq[String]] =
    readCodeLinksFromFileAsMap(appConfig.procedureCodeToAdditionalProcedureCodesLinkFile)

  private val procedureCodeToAdditionalProcedureCodesC21: Map[String, Seq[String]] =
    readCodeLinksFromFileAsMap(appConfig.procedureCodeToAdditionalProcedureCodesC21LinkFile)

  private val procedureCodeLink: Seq[(String, Seq[String])] =
    readCodeLinksFromFile(appConfig.procedureCodesLinkFile)

  private val countryCodeToCountryAliases: Map[String, Seq[String]] =
    readCodeLinksFromFileAsMap(appConfig.countryCodeToAliasesLinkFile)

  private val countryCodeToShortName: Map[String, Seq[String]] =
    readCodeLinksFromFileAsMap(appConfig.countryCodeToShortNameLinkFile)

  private val goodsLocationCodeToLocationTypes: Map[String, Seq[String]] =
    readCodeLinksFromFileAsMap(appConfig.goodsLocationCodeToLocationTypeFile)

  def getHolderOfAuthorisationCodesForTag(tag: Tag): Seq[String] =
    taggedHolderOfAuthorisationCodes.filter(_._2.contains(tag.toString)).map(_._1)

  def getValidAdditionalProcedureCodesForProcedureCode(procedureCode: String): Option[Seq[String]] =
    procedureCodeToAdditionalProcedureCodes.get(procedureCode)

  def getValidAdditionalProcedureCodesForProcedureCodeC21(procedureCode: String): Option[Seq[String]] =
    procedureCodeToAdditionalProcedureCodesC21.get(procedureCode)

  def getValidProcedureCodesForTag(tag: Tag): Seq[String] =
    procedureCodeLink.filter(_._2.contains(tag.toString)).map(_._1)

  def getAliasesForCountryCode(countryCode: String): Option[Seq[String]] =
    countryCodeToCountryAliases.get(countryCode)

  def getShortNamesForCountryCode(countryCode: String): Option[Seq[String]] =
    countryCodeToShortName.get(countryCode)

  def getLocationTypesForGoodsLocationCode(goodsLocationCode: String): Option[Seq[String]] =
    goodsLocationCodeToLocationTypes.get(goodsLocationCode)
}
