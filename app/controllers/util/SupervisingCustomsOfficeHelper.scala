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

package controllers.util

import scala.concurrent.Future

import controllers.declaration.{routes, ModelCacheable}
import models.codes.AdditionalProcedureCode.NO_APC_APPLIES_CODE
import models.declaration.ProcedureCodesData
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier

object SupervisingCustomsOfficeHelper {

  private def isConditionForProcedureCodesDataNotVerified(data: ProcedureCodesData): Boolean =
    data.procedureCode.forall(_ != "1040" || !data.additionalProcedureCodes.contains(NO_APC_APPLIES_CODE))

  /* The Supervising-Customs-Office page must be skipped if all items have
     - "1040" as Procedure code
     - "000" as unique Additional Procedure code

     If this condition is NOT verified the user can land on the Supervising-Customs-Office page.
   */
  def isConditionForAllProcedureCodesNotVerified(cachedModel: ExportsDeclaration): Boolean =
    cachedModel.items.forall(_.procedureCodes.forall(isConditionForProcedureCodesDataNotVerified))

  def landOnOrSkipToNextPage(implicit request: JourneyRequest[_]): Mode => Call =
    if (isConditionForAllProcedureCodesNotVerified(request.cacheModel)) routes.SupervisingCustomsOfficeController.displayPage
    else nextPage

  def nextPage(implicit request: JourneyRequest[_]): Mode => Call =
    request.declarationType match {
      case DeclarationType.SUPPLEMENTARY | DeclarationType.STANDARD => routes.InlandTransportDetailsController.displayPage
      case DeclarationType.SIMPLIFIED | DeclarationType.OCCASIONAL  => routes.ExpressConsignmentController.displayPage
      case DeclarationType.CLEARANCE                                => routes.DepartureTransportController.displayPage
    }

  def resetCache(modelCacheable: ModelCacheable)(implicit hc: HeaderCarrier, request: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    modelCacheable.updateExportsDeclarationSyncDirect(_.removeSupervisingCustomsOffice)
}
