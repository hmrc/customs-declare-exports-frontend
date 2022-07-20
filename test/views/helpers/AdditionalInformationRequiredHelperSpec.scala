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

package views.helpers

import base.ExportsTestData.pc1040
import base.Injector
import models.DeclarationType._
import models.declaration.{ExportItem, ProcedureCodesData}
import org.scalatest.Assertion
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec

class AdditionalInformationRequiredHelperSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  val helper = instanceOf[AdditionalInformationRequiredHelper]

  "AdditionalInformationRequiredHelper getBodyContent" when {

    "procedure code equals 1040" should {
      val item = ExportItem("12345", procedureCodes = pc1040)

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY, CLEARANCE)(aDeclaration(withItem(item))) { implicit request =>
        "display the correct body content" in {
          val bodyContent = helper.getBodyContent(request.declarationType, pc1040)

          checkConentIsClearanceOr1040(bodyContent.body)
        }
      }
    }

    "procedure code not equals 1040" should {
      val maybeProcedureCodesData = Some(ProcedureCodesData(Some("1007"), Seq("000")))
      val item = ExportItem("12345", procedureCodes = maybeProcedureCodesData)

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY)(aDeclaration(withItem(item))) { implicit request =>
        "display the correct body content" in {
          val bodyContent = helper.getBodyContent(request.declarationType, maybeProcedureCodesData)

          checkConentIsNot1040(bodyContent.body)
        }
      }

      onJourney(CLEARANCE) { implicit request =>
        "display the correct body content" in {
          val bodyContent = helper.getBodyContent(request.declarationType, maybeProcedureCodesData)

          checkConentIsClearanceOr1040(bodyContent.body)
        }
      }
    }

    "procedure code is missing" should {
      val item = ExportItem("12345", procedureCodes = None)

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY, CLEARANCE)(aDeclaration(withItem(item))) { implicit request =>
        "display the correct body content" in {
          val bodyContent = helper.getBodyContent(request.declarationType, None)

          checkConentIsClearanceOr1040(bodyContent.body)
        }
      }
    }
  }

  private def checkConentIsClearanceOr1040(body: String): Assertion =
    body contains "ai_containers_link" mustBe true

  private def checkConentIsNot1040(body: String): Assertion =
    body contains "proc_codes_link" mustBe true
}
