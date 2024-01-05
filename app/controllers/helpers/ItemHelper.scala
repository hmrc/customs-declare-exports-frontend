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

import controllers.declaration.routes.{NactCodeSummaryController, ZeroRatedForVatController}
import forms.declaration.NatureOfTransaction
import forms.declaration.NatureOfTransaction.{BusinessPurchase, Sale}
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD}
import models.ExportsDeclaration
import play.api.mvc.Call

object ItemHelper {

  def cusCodeAndDangerousGoodsNextPage(declaration: ExportsDeclaration, itemId: String): Call = {
    val toZeroRatedForVatPage = eligibleForZeroVat(declaration) || isLowValueDeclaration(declaration, itemId)
    if (toZeroRatedForVatPage) ZeroRatedForVatController.displayPage(itemId) else NactCodeSummaryController.displayPage(itemId)
  }

  private def eligibleForZeroVat(declaration: ExportsDeclaration): Boolean =
    declaration.natureOfTransaction match {
      case Some(NatureOfTransaction(`Sale`) | NatureOfTransaction(`BusinessPurchase`)) => declaration.isType(STANDARD)
      case _                                                                           => false
    }

  val journeysOnLowValue = List(OCCASIONAL, SIMPLIFIED)

  private def isLowValueDeclaration(declaration: ExportsDeclaration, itemId: String): Boolean =
    journeysOnLowValue.contains(declaration.`type`) && declaration.isLowValueDeclaration(itemId)
}
