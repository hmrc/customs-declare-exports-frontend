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

package services

import connectors.{CodeLinkConnector, CodeListConnector}
import models.codes.{AdditionalProcedureCode, ProcedureCode}
import models.DeclarationType.{CLEARANCE, DeclarationType}

import java.util.Locale
import javax.inject.Inject

class ProcedureCodeService @Inject()(codeListConnector: CodeListConnector, codeLinkConnector: CodeLinkConnector) {

  def getProcedureCodesFor(journey: DeclarationType, isEidr: Boolean, locale: Locale): Seq[ProcedureCode] =
    journey match {
      case CLEARANCE if !isEidr => codeListConnector.getProcedureCodesForC21(locale)
      case _                    => codeListConnector.getProcedureCodes(locale)
    }

  def getAdditionalProcedureCodesFor(procedureCode: String, locale: Locale): Seq[AdditionalProcedureCode] = {
    val standardAdditionalProcedureCodes = codeLinkConnector.getValidAdditionalProcedureCodesForProcedureCode(procedureCode).map { codes =>
      codeListConnector
        .getAdditionalProcedureCodes(locale)
        .filter(apc => codes.contains(apc.code))
    }

    lazy val c21AdditionalProcedureCodes = codeLinkConnector.getValidAdditionalProcedureCodesForProcedureCodeC21(procedureCode).map { codes =>
      codeListConnector
        .getAdditionalProcedureCodesForC21(locale)
        .filter(apc => codes.contains(apc.code))
    }

    standardAdditionalProcedureCodes
      .orElse(c21AdditionalProcedureCodes)
      .getOrElse(Seq.empty[AdditionalProcedureCode])
  }
}
