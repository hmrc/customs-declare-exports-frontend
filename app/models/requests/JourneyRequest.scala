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

package models.requests

import forms.section1.AdditionalDeclarationType.AdditionalDeclarationType
import models.DeclarationType.DeclarationType
import models.ExportsDeclaration
import models.responses.FlashKeys
import play.api.data.FormError

class JourneyRequest[+A](val authenticatedRequest: AuthenticatedRequest[A], val cacheModel: ExportsDeclaration)
    extends AuthenticatedRequest[A](authenticatedRequest, authenticatedRequest.user) {

  val declarationType: DeclarationType = cacheModel.`type`

  def isType(declarationTypes: DeclarationType*): Boolean = declarationTypes.contains(declarationType)

  def isAdditionalDeclarationType(adt: AdditionalDeclarationType): Boolean = cacheModel.isAdditionalDeclarationType(adt)

  def eori: String = authenticatedRequest.user.eori

  def submissionErrors: Seq[FormError] = {
    val fieldName = flash.get(FlashKeys.fieldName)
    val errorMessage = flash.get(FlashKeys.errorMessage)

    (fieldName, errorMessage) match {
      case (nameOpt, Some(messageKey)) => Seq(FormError(nameOpt.getOrElse(""), messageKey))
      case _                           => Seq.empty
    }
  }
}
