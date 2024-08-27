/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.timeline

import base.{ControllerWithoutFormSpec, Injector, MrnStatusTestData}
import connectors.CustomsDeclareExportsConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers._
import services.ead.BarcodeService
import uk.gov.hmrc.http.UpstreamErrorResponse
import views.html.timeline.ead

import scala.concurrent.Future

class EADControllerSpec extends ControllerWithoutFormSpec with Injector with MrnStatusTestData {

  val barcodeService = instanceOf[BarcodeService]
  val connector = mock[CustomsDeclareExportsConnector]
  val view = instanceOf[ead]

  val controller = new EADController(mockAuthAction, mcc, connector, errorHandler, barcodeService, view)

  override def beforeEach(): Unit = {
    when(connector.fetchMrnStatus(any())(any(), any())).thenReturn(Future.successful(mrnStatus))
    super.beforeEach()
    authorizedUser()
  }

  "EAD Controller" should {
    val mrn = "18GB9JLC3CU1LFGVR2"

    "return 200 and display the EAD page" when {
      "the declaration information are found" in {
        val result = controller.generateDocument(mrn).apply(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "the declaration information for the provided MRN cannot be found" in {
        when(connector.fetchMrnStatus(any[String])(any(), any()))
          .thenReturn(Future.failed(UpstreamErrorResponse("some issue", NOT_FOUND)))

        val result = controller.generateDocument(mrn).apply(getRequest())

        status(result) mustBe BAD_REQUEST
      }
    }
  }
}
