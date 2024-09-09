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

package forms.section2.authorisationHolder

import config.featureFlags.MerchandiseInBagConfig
import controllers.helpers.AuthorisationHolderHelper.authorisationHolders
import models.ExportsDeclaration
import models.requests.JourneyRequest

object AuthorizationTypeCodes {

  val CSE = "CSE"
  val EXRR = "EXRR"
  val FP = "FP"
  val MIB = "MIB"
  val MOU = "MOU"

  def codesFilteredFromView(merchandiseInBagConfig: MerchandiseInBagConfig): List[String] =
    if (merchandiseInBagConfig.isMerchandiseInBagEnabled) List.empty
    else List("EORI", MIB)

  def isAuthCode(code: String)(implicit request: JourneyRequest[_]): Boolean =
    authorisationHolders.exists(_.authorisationTypeCode.exists(_ == code))

  def isAuthCode(declaration: ExportsDeclaration, code: String): Boolean =
    declaration.authorisationHolders.exists(_.authorisationTypeCode.exists(_ == code))

  def isAuthCode(declaration: ExportsDeclaration, codes: Seq[String]): Boolean =
    declaration.authorisationHolders.exists(_.authorisationTypeCode.exists(codes.contains))
}
