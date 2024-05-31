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
import models.requests.JourneyRequest
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.helpers.summary._
import views.html.declaration.summary.sections.section_summary

class SectionSummaryControllerSpec extends ControllerWithoutFormSpec with OptionValues {

  val mockPage = mock[section_summary]

  val mockCard1ForReferences = mock[Card1ForReferences]
  val mockCard2ForParties = mock[Card2ForParties]
  val mockSection3Card = mock[Card3ForRoutesAndLocations]
  val mockCard4ForTransactions = mock[Card4ForTransactions]
  val mockCard5ForItems = mock[Card5ForItems]
  val mockSection6CardForTransport = mock[Card6ForTransport]

  val controller = new SectionSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mcc,
    mockPage,
    mockCard1ForReferences,
    mockCard2ForParties,
    mockSection3Card,
    mockCard4ForTransactions,
    mockCard5ForItems,
    mockSection6CardForTransport
  )

  def verifyPage(summaryCard: SummaryCard): HtmlFormat.Appendable =
    verify(mockPage, times(1))(eqTo(summaryCard))(any(), any())

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  implicit val request: JourneyRequest[AnyContent] = getJourneyRequest()

  "Section Summary controller" must {

    "return 200 (OK)" when {
      val mocks = Array(
        mockCard1ForReferences,
        mockCard2ForParties,
        mockSection3Card,
        mockCard4ForTransactions,
        mockCard5ForItems,
        mockSection6CardForTransport
      )

      for (section <- 1 to 6)
        s"display page method is invoked with section $section" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(section)(request)

          status(result) mustBe OK
          verifyPage(mocks(section - 1))
        }
    }

    "redirect to summary" when {
      "unknown section request" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage(0)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SummaryController.displayPage.url
      }
    }
  }
}
