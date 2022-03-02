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

package forms.declaration.declarationHolder

import controllers.helpers.DeclarationHolderHelper.declarationHolders
import models.ExportsDeclaration
import models.requests.JourneyRequest

object AuthorizationTypeCodes {

  val codeFilteredFromView = "EORI"
  val codeThatOverrideInlandOrBorderSkip = "FP"
  val codeThatSkipLocationOfGoods = "MOU"

  val mutuallyExclusiveAuthCodes = List("CSE", "EXRR")

  val authCodesThatSkipInlandOrBorder = mutuallyExclusiveAuthCodes

  def isAuthCode(code: String)(implicit request: JourneyRequest[_]): Boolean =
    declarationHolders.exists(_.authorisationTypeCode.exists(_ == code))

  def isAuthCode(declaration: ExportsDeclaration, code: String): Boolean =
    declaration.declarationHolders.exists(_.authorisationTypeCode.exists(_ == code))

  def isAuthCode(declaration: ExportsDeclaration, codes: Seq[String]): Boolean =
    declaration.declarationHolders.exists(_.authorisationTypeCode.exists(codes.contains))

  val codesRequiringDocumentation = Set(
    "OPO",
    "REX",
    "AEOC",
    "AEOS",
    "AEOF",
    "CVA",
    "CGU",
    "DPO",
    "TST",
    "RSS",
    "ACP",
    "SDE",
    "CCL",
    "EIR",
    "TEA",
    "TEAH",
    "CWP",
    "CW1",
    "CW2",
    "ACR",
    "ACE",
    "SSE",
    "TRD",
    "ETD",
    "FZ",
    "IPO",
    "CSE",
    "DEP",
    "EPSS",
    "EXW",
    "EXWH",
    "GGA",
    "MOU",
    "UKCS",
    "EUS",
    "ACT"
  )
}
