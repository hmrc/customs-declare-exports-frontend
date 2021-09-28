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

package controllers.helpers

import forms.declaration.declarationHolder.DeclarationHolder
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}

object DeclarationHolderHelper {

  val DeclarationHolderFormGroupId: String = "declarationHolder"

  def declarationHolders(implicit request: JourneyRequest[_]): Seq[DeclarationHolder] = request.cacheModel.declarationHolders

  def form(implicit request: JourneyRequest[_]): Form[DeclarationHolder] =
    DeclarationHolder.form(request.eori, request.cacheModel.additionalDeclarationType)

  private val mutuallyExclusiveAuthorisationCodes = List("CSE", "EXRR")

  def validateMutuallyExclusiveAuthCodes(maybeHolder: Option[DeclarationHolder], holders: Seq[DeclarationHolder]): Option[FormError] =
    maybeHolder match {
      case Some(DeclarationHolder(Some(code), _, _)) if mutuallyExclusiveAuthorisationCodes.contains(code) =>
        val mustNotAlreadyContainCodes = mutuallyExclusiveAuthorisationCodes.filter(_ != code)

        if (!holders.map(_.authorisationTypeCode.getOrElse("")).containsSlice(mustNotAlreadyContainCodes)) None
        else Some(FormError(DeclarationHolderFormGroupId, s"declaration.declarationHolder.${code}.error.exclusive"))

      case _ => None
    }
}
