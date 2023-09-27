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

import base.ExportsTestData.modifierForPC1040
import base.{JourneyTypeTestRunner, MockAuthAction, MockExportCacheService, UnitSpec}
import controllers.declaration.routes.{
  DepartureTransportController,
  ExpressConsignmentController,
  InlandOrBorderController,
  InlandTransportDetailsController
}
import controllers.helpers.TransportSectionHelper._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import models.DeclarationType.CLEARANCE
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}

class SupervisingCustomsOfficeHelperSpec
    extends UnitSpec with ExportsDeclarationBuilder with ExportsItemBuilder with JourneyTypeTestRunner with MockExportCacheService
    with MockAuthAction {

  private val supervisingCustomsOfficeHelper = instanceOf[SupervisingCustomsOfficeHelper]

  "SupervisingCustomsOfficeHelper on isConditionForAllProcedureCodesVerified" should {

    "return true" when {

      "a declaration contains a single item with ProcedureCodesData(1040-000)" in {
        val declaration = aDeclaration(modifierForPC1040)
        assert(supervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified(declaration))
      }

      "a declaration contains all items with ProcedureCodesData(1040-000)" in {
        val declaration = aDeclaration(modifierForPC1040, modifierForPC1040, modifierForPC1040)
        assert(supervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified(declaration))
      }
    }

    "return false" when {
      val condNotVerified = anItem(withProcedureCodes(Some("3171"), Seq("1CS")))

      "a declaration does not contain any item with ProcedureCodesData(1040-000)" in {
        val declaration = aDeclaration(withItem(condNotVerified))
        supervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified(declaration) mustBe false
      }

      "a declaration contains at least one items without ProcedureCodesData(1040-000)" in {
        val declaration = aDeclaration(modifierForPC1040, withItem(condNotVerified), modifierForPC1040)
        supervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified(declaration) mustBe false
      }

      "a declaration contains a single item with ProcedureCodesData(1040-000) and auth code FP" in {
        val declaration = aDeclaration(withDeclarationHolders(Some("FP")), modifierForPC1040)
        supervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified(declaration) mustBe false
      }

      "a declaration contains all items with ProcedureCodesData(1040-000) and auth code FP" in {
        val declaration =
          aDeclaration(withDeclarationHolders(Some("FP")), modifierForPC1040, modifierForPC1040, modifierForPC1040)
        supervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified(declaration) mustBe false
      }

      "a declaration does not contain items" in {
        val declaration = aDeclaration()
        supervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified(declaration) mustBe false
      }
    }
  }

  "SupervisingCustomsOfficeHelper on nextPage" when {

    additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
      s"AdditionalDeclarationType is ${additionalType}" should {
        "goto to InlandOrBorderController" in {
          val declaration = withRequest(additionalType).cacheModel
          supervisingCustomsOfficeHelper.nextPage(declaration) mustBe InlandOrBorderController.displayPage
        }
      }
    }

    List(SUPPLEMENTARY_EIDR).foreach { additionalType =>
      "AdditionalDeclarationType is SUPPLEMENTARY_EIDR" should {
        "goto to InlandTransportDetailsController" in {
          val declaration = withRequest(additionalType).cacheModel
          supervisingCustomsOfficeHelper.nextPage(declaration) mustBe InlandTransportDetailsController.displayPage
        }
      }
    }

    onJourney(CLEARANCE) { request =>
      postalOrFTIModeOfTransportCodes.foreach { modeOfTransportCode =>
        s"transportLeavingBoarderCode is ${modeOfTransportCode.value}" should {
          "goto ExpressConsignmentController" in {
            val declaration = aDeclaration(withType(request.declarationType), withBorderModeOfTransportCode(modeOfTransportCode))
            supervisingCustomsOfficeHelper.nextPage(declaration) mustBe ExpressConsignmentController.displayPage
          }
        }
      }

      nonPostalOrFTIModeOfTransportCodes.foreach { modeOfTransportCode =>
        s"transportLeavingBoarderCode is ${modeOfTransportCode}" should {
          "goto DepartureTransportController" in {
            val declaration = aDeclaration(withType(request.declarationType), withBorderModeOfTransportCode(Some(modeOfTransportCode)))
            supervisingCustomsOfficeHelper.nextPage(declaration) mustBe DepartureTransportController.displayPage
          }
        }
      }
    }
  }
}
