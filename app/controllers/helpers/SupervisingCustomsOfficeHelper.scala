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

import controllers.declaration.routes._
import controllers.helpers.TransportSectionHelper.isPostalOrFTIModeOfTransport
import forms.declaration.declarationHolder.AuthorizationTypeCodes.isAuthCode
import models.DeclarationType._
import models.codes.AdditionalProcedureCode.NO_APC_APPLIES_CODE
import models.declaration.ProcedureCodesData
import models.ExportsDeclaration
import play.api.mvc.Call
import services.TaggedAuthCodes

import javax.inject.{Inject, Singleton}

@Singleton
class SupervisingCustomsOfficeHelper @Inject() (inlandOrBorderHelper: InlandOrBorderHelper, taggedAuthCodes: TaggedAuthCodes) {

  private def isConditionForProcedureCodesDataVerified(data: ProcedureCodesData): Boolean =
    data.procedureCode.contains("1040") && data.additionalProcedureCodes.contains(NO_APC_APPLIES_CODE)

  /* The Supervising-Customs-Office page must be skipped if all items have
     - "1040" as Procedure code
     - "000" as unique Additional Procedure code
     And
     - "FP" is NOT entered as an Authorisation Code

     If this condition is NOT verified the user can land on the Supervising-Customs-Office page.
   */
  def isConditionForAllProcedureCodesVerified(cachedModel: ExportsDeclaration): Boolean =
    checkProcedureCodes(cachedModel) && !isAuthCode(cachedModel, taggedAuthCodes.codesOverridingInlandOrBorderSkip)

  def checkProcedureCodes(cachedModel: ExportsDeclaration): Boolean =
    cachedModel.items.nonEmpty &&
      cachedModel.items.forall(_.procedureCodes.forall(isConditionForProcedureCodesDataVerified))

  def landOnOrSkipToNextPage(declaration: ExportsDeclaration): Call =
    if (isConditionForAllProcedureCodesVerified(declaration)) nextPage(declaration)
    else SupervisingCustomsOfficeController.displayPage

  def nextPage(declaration: ExportsDeclaration): Call =
    declaration.`type` match {
      case STANDARD | SIMPLIFIED | SUPPLEMENTARY =>
        if (inlandOrBorderHelper.skipInlandOrBorder(declaration)) InlandTransportDetailsController.displayPage
        else InlandOrBorderController.displayPage

      case CLEARANCE  => nextPageOnClearance(declaration)
      case OCCASIONAL => ExpressConsignmentController.displayPage
    }

  private def nextPageOnClearance(declaration: ExportsDeclaration): Call = {
    val condition = isPostalOrFTIModeOfTransport(declaration.transportLeavingBorderCode)
    if (condition) ExpressConsignmentController.displayPage else DepartureTransportController.displayPage
  }
}
