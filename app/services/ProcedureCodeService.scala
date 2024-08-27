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

package services

import config.featureFlags.MerchandiseInBagConfig
import connectors.{CodeLinkConnector, CodeListConnector}
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.codes.{AdditionalProcedureCode, ProcedureCode}
import play.api.Logging

import java.util.Locale
import javax.inject.Inject

class ProcedureCodeService @Inject() (
  codeListConnector: CodeListConnector,
  codeLinkConnector: CodeLinkConnector,
  merchandiseInBagConfig: MerchandiseInBagConfig
) extends Logging {

  def getProcedureCodesFor(journey: DeclarationType, isEidr: Boolean, locale: Locale): Seq[ProcedureCode] =
    journey match {
      case CLEARANCE if !isEidr => codeListConnector.getProcedureCodesForC21(locale).values.toSeq
      case _                    => codeListConnector.getProcedureCodes(locale).values.toSeq
    }

  def getProcedureCodeFor(procedureCode: String, journey: DeclarationType, isEidr: Boolean, locale: Locale): Option[ProcedureCode] =
    journey match {
      case CLEARANCE if !isEidr => codeListConnector.getProcedureCodesForC21(locale).get(procedureCode)
      case _                    => codeListConnector.getProcedureCodes(locale).get(procedureCode)
    }

  def getAdditionalProcedureCodesFor(procedureCode: String, locale: Locale): Seq[AdditionalProcedureCode] = {

    val apcMapForLang = codeListConnector.getAdditionalProcedureCodesMap(locale)
    val standardAdditionalProcedureCodes = codeLinkConnector.getValidAdditionalProcedureCodesForProcedureCode(procedureCode).map { codes =>
      codes
        .map(lookupAdditionalProcedureCode(_, apcMapForLang))
        .filterNot(additionalProcedureCode => additionalProcedureCode.code == "1MB" && !merchandiseInBagConfig.isMerchandiseInBagEnabled)
    }

    lazy val apcC21MapForLang = codeListConnector.getAdditionalProcedureCodesMapForC21(locale)
    lazy val c21AdditionalProcedureCodes = codeLinkConnector.getValidAdditionalProcedureCodesForProcedureCodeC21(procedureCode).map { codes =>
      codes.map(lookupAdditionalProcedureCode(_, apcC21MapForLang))
    }

    standardAdditionalProcedureCodes
      .orElse(c21AdditionalProcedureCodes)
      .getOrElse(Seq.empty[AdditionalProcedureCode])
  }

  private def lookupAdditionalProcedureCode(code: String, apcMap: Map[String, AdditionalProcedureCode]): AdditionalProcedureCode =
    apcMap.getOrElse(
      code, {
        logger.warn(
          s"AdditionalProcedureCode ${code} is defined in the ProcedureCode->AdditionalProcedureCode mapping file but is not defined in the AdditionalProcedureCode definition file!"
        )
        AdditionalProcedureCode(code, "")
      }
    )
}
