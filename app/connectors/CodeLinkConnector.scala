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
import play.api.Logging
import utils.JsonFile

import javax.inject.{Inject, Singleton}

case class CodeLink(parentCode: String, childCodes: Seq[String])

object CodeLink {
  implicit val formats: OFormat[CodeLink] = Json.format[CodeLink]
}

object Tag extends Enumeration {
  type Tag = Value

  val
  // Tags for Additional Document codes
  DocumentCodesRequiringAReason, StatusCodesRequiringAReason,

  // Tags for Authorisation codes
  CodesMutuallyExclusive, CodesNeedingSpecificHintText, CodesOverridingInlandOrBorderSkip, CodesRequiringDocumentation, CodesSkippingInlandOrBorder,
    CodesSkippingLocationOfGoods,

  // Tags for Procedure codes
  CodesRestrictingZeroVat,

  // Tags for Transport codes
  AircraftRegistrationNumber, EuropeanVesselIDNumber, FlightNumber, NameOfInlandWaterwayVessel, NameOfVessel, NotApplicable, ShipOrRoroImoNumber,
    VehicleRegistrationNumber, WagonNumber = Value
}

@ImplementedBy(classOf[FileBasedCodeLinkConnector])
trait CodeLinkConnector {
  def getHolderOfAuthorisationCodesForTag(tag: Tag): Seq[String]
  def getTransportCodeForTag(tag: Tag): (String, String)

  def getAdditionalDocumentStatusCodeForTag(tag: Tag): Seq[String]
  def getAdditionalDocumentCodesForTag(tag: Tag): Seq[String]

  def getValidProcedureCodesForTag(tag: Tag): Seq[String]
  def getValidAdditionalProcedureCodesForProcedureCode(procedureCode: String): Option[Seq[String]]
  def getValidAdditionalProcedureCodesForProcedureCodeC21(procedureCode: String): Option[Seq[String]]

  def getAliasesForCountryCode(countryCode: String): Option[Seq[String]]
  def getShortNamesForCountryCode(countryCode: String): Option[Seq[String]]
  def getDocumentTypesToExclude(code: String): Seq[String]
}

@Singleton
class FileBasedCodeLinkConnector @Inject() (appConfig: AppConfig, val jsonFileReader: JsonFile) extends CodeLinkConnector with Logging {

  private def readCodeLinksFromFile[T <: CommonCode](srcFile: String): Seq[(String, Seq[String])] = {
    logger.info(s"Loading CodeLinkConnector Reference data file '$srcFile'")

    val codeLinks = jsonFileReader.getJsonArrayFromFile(srcFile, CodeLink.formats)
    codeLinks.map { codeLink =>
      codeLink.parentCode -> codeLink.childCodes
    }
  }

  private def readCodeLinksFromFileAsMap[T <: CommonCode](srcFile: String): Map[String, Seq[String]] =
    readCodeLinksFromFile(srcFile).toMap

  private val taggedHolderOfAuthorisationCodes: Seq[(String, Seq[String])] =
    readCodeLinksFromFile(appConfig.taggedHolderOfAuthorisationCodeFile)

  private val taggedTransportCodes: Seq[(String, Seq[String])] =
    readCodeLinksFromFile(appConfig.taggedTransportCodeFile)

  private val procedureCodeToAdditionalProcedureCodes: Map[String, Seq[String]] =
    readCodeLinksFromFileAsMap(appConfig.procedureCodeToAdditionalProcedureCodesLinkFile)

  private val procedureCodeToAdditionalProcedureCodesC21: Map[String, Seq[String]] =
    readCodeLinksFromFileAsMap(appConfig.procedureCodeToAdditionalProcedureCodesC21LinkFile)

  private val procedureCodeLink: Seq[(String, Seq[String])] =
    readCodeLinksFromFile(appConfig.procedureCodesLinkFile)

  private val additionalDocumentCodeLink: Seq[(String, Seq[String])] =
    readCodeLinksFromFile(appConfig.additionalDocumentCodeLinkFile)

  private val additionalDocumentStatusCodeLink: Seq[(String, Seq[String])] =
    readCodeLinksFromFile(appConfig.additionalDocumentStatusCodeLinkFile)

  private val countryCodeToCountryAliases: Map[String, Seq[String]] =
    readCodeLinksFromFileAsMap(appConfig.countryCodeToAliasesLinkFile)

  private val countryCodeToShortName: Map[String, Seq[String]] =
    readCodeLinksFromFileAsMap(appConfig.countryCodeToShortNameLinkFile)

  private val documentTypeLink: Map[String, Seq[String]] =
    readCodeLinksFromFileAsMap(appConfig.countryCodeToShortNameLinkFile)

  def getAdditionalDocumentStatusCodeForTag(tag: Tag): Seq[String] =
    additionalDocumentStatusCodeLink.filter(_._2.contains(tag.toString)).map(_._1)

  def getAdditionalDocumentCodesForTag(tag: Tag): Seq[String] =
    additionalDocumentCodeLink.filter(_._2.contains(tag.toString)).map(_._1)

  def getHolderOfAuthorisationCodesForTag(tag: Tag): Seq[String] =
    taggedHolderOfAuthorisationCodes.filter(_._2.contains(tag.toString)).map(_._1)

  def getTransportCodeForTag(tag: Tag): (String, String) =
    taggedTransportCodes
      .filter(_._2.contains(tag.toString))
      .map { case (value, tags) =>
        tags.head -> value
      }
      .head

  def getValidProcedureCodesForTag(tag: Tag): Seq[String] =
    procedureCodeLink.filter(_._2.contains(tag.toString)).map(_._1)

  def getValidAdditionalProcedureCodesForProcedureCode(procedureCode: String): Option[Seq[String]] =
    procedureCodeToAdditionalProcedureCodes.get(procedureCode)

  def getValidAdditionalProcedureCodesForProcedureCodeC21(procedureCode: String): Option[Seq[String]] =
    procedureCodeToAdditionalProcedureCodesC21.get(procedureCode)

  def getAliasesForCountryCode(countryCode: String): Option[Seq[String]] =
    countryCodeToCountryAliases.get(countryCode)

  def getShortNamesForCountryCode(countryCode: String): Option[Seq[String]] =
    countryCodeToShortName.get(countryCode)

  def getDocumentTypesToExclude(code: String): Seq[String] =
    documentTypeLink.filter(_._2.contains(code)).map(_._1).toSeq
}
