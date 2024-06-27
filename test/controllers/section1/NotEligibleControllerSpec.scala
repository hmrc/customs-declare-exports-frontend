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

package controllers.section1

import base.ControllerWithoutFormSpec
import models.DeclarationType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section1.{not_declarant, not_eligible}

class NotEligibleControllerSpec extends ControllerWithoutFormSpec {

  trait SetUp {
    val notEligiblePage = mock[not_eligible]
    val notDeclarantPage = mock[not_declarant]

    val controller = new NotEligibleController(mockAuthAction, mcc, notEligiblePage, notDeclarantPage)

    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(notEligiblePage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(notDeclarantPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  "Not Eligible Controller" should {
    "return 200 (OK)" when {

      "display not eligible location is invoked" in new SetUp {
        private val result = controller.displayNotEligible()(getRequest())

        status(result) must be(OK)
        verify(notEligiblePage, times(1)).apply()(any(), any())
      }

      "display not eligible user is invoked" in new SetUp {
        private val result = controller.displayNotDeclarant()(getRequest())

        status(result) must be(OK)
        verify(notDeclarantPage, times(1)).apply()(any(), any())
      }
    }
  }
}
