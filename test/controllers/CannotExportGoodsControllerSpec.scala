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

package controllers

import base.ControllerWithoutFormSpec
import mock.ErrorHandlerMocks
import models.CannotExportGoodsReason.{allCannotExportGoodsReasons, CatAndDogFur, UrlDirect}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.cannot_export_goods

class CannotExportGoodsControllerSpec extends ControllerWithoutFormSpec with BeforeAndAfterEach with ErrorHandlerMocks {

  val cannotExportGoodsPage = mock[cannot_export_goods]

  val controller =
    new CannotExportGoodsController(cannotExportGoodsPage, stubMessagesControllerComponents(), mockAuthAction, mockErrorHandler)

  override def beforeEach(): Unit = {
    setupErrorHandler()
    authorizedUser()
    when(cannotExportGoodsPage(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  "Cannot export goods controller" should {

    "return 200 (OK)" when {

      for (reason <- allCannotExportGoodsReasons)
        s"displayPage is passed with $reason" in {
          val result = controller.displayPage(CatAndDogFur)(getRequest())
          status(result) must be(OK)
        }
    }

    "return 400" when {

      "displayPage is invoked without a valid reason" in {
        val result = controller.displayPage(UrlDirect)(getRequest())

        status(result) must be(BAD_REQUEST)
        verify(mockErrorHandler).displayErrorPage()(any())
      }
    }
  }
}
