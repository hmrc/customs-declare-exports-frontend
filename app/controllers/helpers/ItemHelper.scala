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

package controllers.helpers

import controllers.declaration.routes.{PackageInformationSummaryController, StatisticalValueController}
import forms.declaration.NatureOfTransaction
import forms.declaration.NatureOfTransaction.{BusinessPurchase, Sale}
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.mvc.Call

object ItemHelper {

  def nextPageAfterNactCodePages(itemId: String)(implicit request: JourneyRequest[_]): Call =
    request.declarationType match {
      case SUPPLEMENTARY | STANDARD => StatisticalValueController.displayPage(itemId)

      case SIMPLIFIED | OCCASIONAL if request.cacheModel.isLowValueDeclaration(itemId) =>
        StatisticalValueController.displayPage(itemId)

      case SIMPLIFIED | OCCASIONAL =>
        PackageInformationSummaryController.displayPage(itemId)
    }

  def skipZeroRatedForVatPage(declaration: ExportsDeclaration, itemId: String): Boolean =
    !eligibleForZeroVat(declaration) && !isLowValueDeclaration(declaration, itemId)

  private def eligibleForZeroVat(declaration: ExportsDeclaration): Boolean =
    declaration.natureOfTransaction match {
      case Some(NatureOfTransaction(BusinessPurchase) | NatureOfTransaction(Sale)) => declaration.isType(STANDARD)
      case _                                                                       => false
    }

  private def isLowValueDeclaration(declaration: ExportsDeclaration, itemId: String): Boolean =
    occasionalAndSimplified.contains(declaration.`type`) && declaration.isLowValueDeclaration(itemId)
}
