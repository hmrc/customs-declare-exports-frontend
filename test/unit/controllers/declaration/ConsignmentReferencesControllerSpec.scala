/*
 * Copyright 2020 HM Revenue & Customs
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

package unit.controllers.declaration

import controllers.declaration.ConsignmentReferencesController
import forms.declaration.ConsignmentReferences
import forms.{Ducr, Lrn}
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.{DeclarationType, Mode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.consignment_references

class ConsignmentReferencesControllerSpec extends ControllerSpec {

  trait SetUp {
    val consignmentReferencesPage = mock[consignment_references]

    val controller = new ConsignmentReferencesController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      consignmentReferencesPage
    )(ec)

    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(consignmentReferencesPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  "Consignment References controller" should {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in new SetUp {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }

        "display page method is invoked and cache contains data" in new SetUp {

          withNewCaching(aDeclaration(withConsignmentReferences()))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }
      }

      "return 400 (BAD_REQUEST)" in new SetUp {

        withNewCaching(request.cacheModel)
        val incorrectForm = Json.toJson(ConsignmentReferences(Ducr("1234"), Lrn("")))

        val result = controller.submitConsignmentReferences(Mode.Normal)(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return 303 (SEE_OTHER) and redirect to declarant details page" in new SetUp {

        withNewCaching(request.cacheModel)
        val correctForm = Json.toJson(ConsignmentReferences(Ducr(DUCR), LRN))

        val result = controller.submitConsignmentReferences(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DeclarantDetailsController.displayPage()
      }
    }

    onClearance { request =>
      "return 303 (SEE_OTHER) and redirect to Entry into Declarant's Records page" in new SetUp {

        withNewCaching(request.cacheModel)
        val correctForm = Json.toJson(ConsignmentReferences(Ducr(DUCR), LRN))

        val result = controller.submitConsignmentReferences(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.EntryIntoDeclarantsRecordsController.displayPage()
      }
    }
  }
}
