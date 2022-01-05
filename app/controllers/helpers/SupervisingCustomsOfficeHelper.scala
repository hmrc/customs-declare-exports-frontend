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

package controllers.helpers

import controllers.declaration.routes._
import controllers.helpers.InlandOrBorderHelper.skipInlandOrBorder
import controllers.helpers.TransportSectionHelper.isPostalOrFTIModeOfTransport
import models.DeclarationType.{CLEARANCE, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.codes.AdditionalProcedureCode.NO_APC_APPLIES_CODE
import models.declaration.ProcedureCodesData
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.mvc.Call

object SupervisingCustomsOfficeHelper {

  private def isConditionForProcedureCodesDataVerified(data: ProcedureCodesData): Boolean =
    data.procedureCode.contains("1040") && data.additionalProcedureCodes.contains(NO_APC_APPLIES_CODE)

  /* The Supervising-Customs-Office page must be skipped if all items have
     - "1040" as Procedure code
     - "000" as unique Additional Procedure code

     If this condition is NOT verified the user can land on the Supervising-Customs-Office page.
   */
  def isConditionForAllProcedureCodesVerified(cachedModel: ExportsDeclaration): Boolean =
    cachedModel.items.nonEmpty && cachedModel.items.forall(_.procedureCodes.forall(isConditionForProcedureCodesDataVerified))

  def landOnOrSkipToNextPage(implicit request: JourneyRequest[_]): Mode => Call =
    if (isConditionForAllProcedureCodesVerified(request.cacheModel)) nextPage
    else SupervisingCustomsOfficeController.displayPage

  def nextPage(implicit request: JourneyRequest[_]): Mode => Call =
    request.declarationType match {
      case STANDARD | SUPPLEMENTARY =>
        if (skipInlandOrBorder(request.cacheModel)) InlandTransportDetailsController.displayPage
        else InlandOrBorderController.displayPage

      case CLEARANCE                               => nextPageOnClearance
      case SIMPLIFIED | DeclarationType.OCCASIONAL => ExpressConsignmentController.displayPage
    }

  private def nextPageOnClearance(implicit request: JourneyRequest[_]): Mode => Call = {
    val condition = isPostalOrFTIModeOfTransport(request.cacheModel.transportLeavingBorderCode)
    if (condition) ExpressConsignmentController.displayPage else DepartureTransportController.displayPage
  }
}
