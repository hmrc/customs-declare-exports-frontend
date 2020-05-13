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

import controllers.declaration.DeclarantDetailsController
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.DeclarantEoriConfirmation
import forms.declaration.DeclarantEoriConfirmation.isEoriKey
import models.{DeclarationType, Mode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.declarant_details

class DeclarantDetailsControllerSpec extends ControllerSpec {

  trait SetUp {
    private val declarantDetailsPage = mock[declarant_details]

    val controller = new DeclarantDetailsController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      declarantDetailsPage
    )(ec)

    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(declarantDetailsPage.apply(any[Mode], any[Form[DeclarantEoriConfirmation]])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  "Declarant Details Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in new SetUp {

        private val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked and cache is not empty" in new SetUp {

        withNewCaching(aDeclaration(withDeclarantDetails()))

        private val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form contains incorrect values" in new SetUp {

        private val incorrectForm = JsObject(Map(isEoriKey -> JsString("wrong")))

        private val result = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER) and redirect to is declarant exporter details page" in new SetUp {

      private val correctForm = JsObject(Map(isEoriKey -> JsString(YesNoAnswers.yes)))

      private val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

      await(result) mustBe aRedirectToTheNextPage
      thePageNavigatedTo mustBe controllers.declaration.routes.DeclarantExporterController.displayPage()
    }
  }
}
