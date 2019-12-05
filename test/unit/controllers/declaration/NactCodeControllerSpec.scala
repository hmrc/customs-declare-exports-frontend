/*
 * Copyright 2019 HM Revenue & Customs
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

import base.TestHelper
import controllers.declaration.NactCodeController
import controllers.util.{Add, SaveAndContinue}
import forms.declaration.NactCode
import forms.declaration.NactCode.nactCodeKey
import models.DeclarationType.DeclarationType
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.mvc.Call
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.nact_codes

class NactCodeControllerSpec extends ControllerSpec with ErrorHandlerMocks with OptionValues {

  val mockPage = mock[nact_codes]

  val controller =
    new NactCodeController(
      mockAuthAction,
      mockJourneyAction,
      mockErrorHandler,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      mockPage
    )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  def theNactCodes: List[NactCode] = {
    val captor = ArgumentCaptor.forClass(classOf[List[NactCode]])
    verify(mockPage).apply(any(), any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1) = verify(mockPage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  "NACT Code controller" should {

    "return 200 (OK)" when {

      def controllerDisplaysPage(decType: DeclarationType): Unit = {
        "display page method is invoked and cache is empty" in {
          val item = anItem()
          withNewCaching(aDeclaration(withType(decType), withItems(item)))

          val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()

          theNactCodes mustBe empty
        }

        "display page method is invoked and cache contains data" in {
          val nactCode = NactCode("VATE")
          val item = anItem(withNactCodes(nactCode))
          withNewCaching(aDeclaration(withType(decType), withItems(item)))

          val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()

          theNactCodes must contain(nactCode)
        }
      }

      "we are on standard journey" should {
        behave like controllerDisplaysPage(DeclarationType.STANDARD)
      }

      "we are on simplified journey" should {
        behave like controllerDisplaysPage(DeclarationType.SIMPLIFIED)
      }

      "we are on supplementary journey" should {
        behave like controllerDisplaysPage(DeclarationType.SUPPLEMENTARY)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      def controllerErrors(decType: DeclarationType): Unit = {

        "user adds invalid code" in {
          val item = anItem()
          withNewCaching(aDeclaration(withType(decType), withItems(item)))

          val body = Seq((nactCodeKey, "invalidCode"), (Add.toString, ""))

          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(body: _*))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }

        "user adds duplicate code" in {
          val nactCode = NactCode("VATE")
          val item = anItem(withNactCodes(nactCode))
          withNewCaching(aDeclaration(withType(decType), withItems(item)))

          val body = Seq((nactCodeKey, "VATE"), (Add.toString, ""))

          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(body: _*))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }

        "user adds too many codes" in {
          val nactCodes = List.fill(99)(NactCode(TestHelper.createRandomAlphanumericString(4)))
          val item = anItem(withNactCodes(nactCodes))
          withNewCaching(aDeclaration(withType(decType), withItems(item)))

          val body = Seq((nactCodeKey, "VATE"), (Add.toString, ""))

          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(body: _*))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }
      }

      "we are on standard journey" should {
        behave like controllerErrors(DeclarationType.STANDARD)
      }

      "we are on clearance journey" should {
        behave like controllerErrors(DeclarationType.CLEARANCE)
      }

      "we are on simplified journey" should {
        behave like controllerErrors(DeclarationType.SIMPLIFIED)
      }

      "we are on supplementary journey" should {
        behave like controllerErrors(DeclarationType.SUPPLEMENTARY)
      }
    }

    "return 303 (SEE_OTHER)" when {

      def controllerRedirectsToNextPage(decType: DeclarationType, nextPage: Call): Unit =
        "user submits valid code" in {

          val item = anItem(withItemId("itemId"))
          withNewCaching(aDeclaration(withType(decType), withItems(item)))

          val body = Seq((nactCodeKey, "VATE"), (SaveAndContinue.toString, ""))

          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(body: _*))

          await(result) mustBe aRedirectToTheNextPage

          thePageNavigatedTo mustBe nextPage
          verifyPageInvoked(0)
        }

      for (decType <- Set(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.CLEARANCE)) {
        s"we are on $decType journey" should {
          behave like controllerRedirectsToNextPage(
            decType,
            controllers.declaration.routes.StatisticalValueController.displayPage(Mode.Normal, "itemId")
          )
        }
      }

      for (decType <- Set(DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL)) {
        s"we are on $decType journey" should {
          behave like controllerRedirectsToNextPage(
            decType,
            controllers.declaration.routes.PackageInformationController.displayPage(Mode.Normal, "itemId")
          )
        }
      }

    }
  }
}
