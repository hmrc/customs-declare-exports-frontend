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

import com.google.inject.ImplementedBy
import config.AppConfig
import models.codes.CommonCode
import play.api.libs.json.{Json, OFormat}
import utils.JsonFile

import javax.inject.{Inject, Singleton}

case class CodeLink(parentCode: String, childCodes: Seq[String])

object CodeLink {
  implicit val formats: OFormat[CodeLink] = Json.format[CodeLink]
}

@ImplementedBy(classOf[FileBasedCodeLinkConnector])
trait CodeLinkConnector {
  def getValidAdditionalProcedureCodesForProcedureCode(procedureCode: String): Option[Seq[String]]
  def getValidAdditionalProcedureCodesForProcedureCodeC21(procedureCode: String): Option[Seq[String]]
}

@Singleton
class FileBasedCodeLinkConnector @Inject()(appConfig: AppConfig) extends CodeLinkConnector {

  private def readCodeLinksFromFile[T <: CommonCode](srcFile: String): Map[String, Seq[String]] = {
    val codeLinks = JsonFile.getJsonArrayFromFile(srcFile, CodeLink.formats)

    codeLinks.map { codeLink =>
      (codeLink.parentCode -> codeLink.childCodes)
    }.toMap
  }

  private val procedureCodeToAdditionalProcedureCodes: Map[String, Seq[String]] =
    readCodeLinksFromFile(appConfig.procedureCodeToAdditionalProcedureCodesLinkFile)
  private val procedureCodeToAdditionalProcedureCodesC21: Map[String, Seq[String]] =
    readCodeLinksFromFile(appConfig.procedureCodeToAdditionalProcedureCodesC21LinkFile)

  def getValidAdditionalProcedureCodesForProcedureCode(procedureCode: String): Option[Seq[String]] =
    procedureCodeToAdditionalProcedureCodes.get(procedureCode)

  def getValidAdditionalProcedureCodesForProcedureCodeC21(procedureCode: String): Option[Seq[String]] =
    procedureCodeToAdditionalProcedureCodesC21.get(procedureCode)
}
