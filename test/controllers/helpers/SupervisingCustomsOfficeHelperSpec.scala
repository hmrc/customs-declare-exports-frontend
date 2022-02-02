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

import base.ExportsTestData.itemWithPC
import base.{JourneyTypeTestRunner, MockAuthAction, MockExportCacheService, UnitSpec}
import controllers.declaration.routes
import controllers.helpers.TransportSectionHelper.additionalDeclTypesAllowedOnInlandOrBorder
import forms.declaration.ModeOfTransportCode.{meaningfulModeOfTransportCodes, FixedTransportInstallations, PostalConsignment}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import models.DeclarationType
import models.Mode.Normal
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}

class SupervisingCustomsOfficeHelperSpec
    extends UnitSpec with ExportsDeclarationBuilder with ExportsItemBuilder with JourneyTypeTestRunner with MockExportCacheService
    with MockAuthAction {

  import SupervisingCustomsOfficeHelperSpec._

  private val supervisingCustomsOfficeHelper = instanceOf[SupervisingCustomsOfficeHelper]

  "SupervisingCustomsOfficeHelper on isConditionForAllProcedureCodesVerified" should {

    "return true" when {

      "a declaration contains a single item with ProcedureCodesData(1040-000)" in {
        val declaration = aDeclaration(withItem(itemWithPC("1040")))
        assert(supervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified(declaration))
      }

      "a declaration contains all items with ProcedureCodesData(1040-000)" in {
        val declaration = aDeclaration(withItem(itemWithPC("1040")), withItem(itemWithPC("1040")), withItem(itemWithPC("1040")))
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
        val declaration = aDeclaration(withItem(itemWithPC("1040")), withItem(condNotVerified), withItem(itemWithPC("1040")))
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
          supervisingCustomsOfficeHelper.nextPage(declaration)(Normal) mustBe routes.InlandOrBorderController.displayPage(Normal)
        }
      }
    }

    List(SUPPLEMENTARY_EIDR).foreach { additionalType =>
      "AdditionalDeclarationType is SUPPLEMENTARY_EIDR" should {
        "goto to InlandTransportDetailsController" in {
          val declaration = withRequest(additionalType).cacheModel
          supervisingCustomsOfficeHelper.nextPage(declaration)(Normal) mustBe routes.InlandTransportDetailsController.displayPage(Normal)
        }
      }
    }

    onJourney(DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL) { request =>
      "goto ExpressConsignmentController for SIMPLIFIED & OCCASIONAL journeys" in {
        supervisingCustomsOfficeHelper.nextPage(request.cacheModel)(Normal) mustBe routes.ExpressConsignmentController.displayPage(Normal)
      }
    }

    onJourney(DeclarationType.CLEARANCE) { request =>
      skipDepartureTransportPageCodes.foreach { modeOfTransportCode =>
        s"transportLeavingBoarderCode is ${modeOfTransportCode}" should {
          "goto ExpressConsignmentController" in {
            val declaration = aDeclaration(withType(request.declarationType), withBorderModeOfTransportCode(Some(modeOfTransportCode)))
            supervisingCustomsOfficeHelper.nextPage(declaration)(Normal) mustBe routes.ExpressConsignmentController.displayPage(Normal)
          }
        }
      }

      meaningfulModeOfTransportCodes
        .filter(!skipDepartureTransportPageCodes.contains(_))
        .foreach { modeOfTransportCode =>
          s"transportLeavingBoarderCode is ${modeOfTransportCode}" should {
            "goto DepartureTransportController" in {
              val declaration = aDeclaration(withType(request.declarationType), withBorderModeOfTransportCode(Some(modeOfTransportCode)))
              supervisingCustomsOfficeHelper.nextPage(declaration)(Normal) mustBe routes.DepartureTransportController.displayPage(Normal)
            }
          }
        }
    }
  }
}

object SupervisingCustomsOfficeHelperSpec {
  val skipDepartureTransportPageCodes = Seq(FixedTransportInstallations, PostalConsignment)
}
