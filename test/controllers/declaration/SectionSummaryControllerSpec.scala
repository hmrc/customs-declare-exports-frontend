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

package controllers.declaration

import base.ControllerWithoutFormSpec
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.helpers.summary.sections.{Card1ForReferencesSection, SectionCard}
import views.html.declaration.summary.sections.section_summary

class SectionSummaryControllerSpec extends ControllerWithoutFormSpec with OptionValues {

  val mockPage = mock[section_summary]
  val mockSection1Card = mock[Card1ForReferencesSection]

  val controller = new SectionSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    mockPage,
    mockSection1Card
  )

  val statusCode = "2"

  def verifyPage(sectionCard: SectionCard): HtmlFormat.Appendable =
    verify(mockPage, times(1))(eqTo(sectionCard))(any(), any())

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  "Section Summary controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {

        "display page method is invoked with section 1" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(1)(getRequest())

          status(result) mustBe OK
          verifyPage(mockSection1Card)

        }

      }

      "redirect to summary" when {

        "unknown section request" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(0)(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.SummaryController.displayPage.url

        }

      }

    }

  }
}
