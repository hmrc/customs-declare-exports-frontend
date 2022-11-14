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

import base.{ControllerWithoutFormSpec, Injector}
import connectors.CustomsDeclareExportsConnector
import models.dis.MrnStatusSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers._
import services.ead.{BarcodeService, EADService}
import views.html.ead

import scala.concurrent.Future

class EADControllerSpec extends ControllerWithoutFormSpec with Injector {

  val mcc = stubMessagesControllerComponents()
  val barcodeService = instanceOf[BarcodeService]
  val connector = mock[CustomsDeclareExportsConnector]
  val eADService = new EADService(barcodeService, connector)
  val view = instanceOf[ead]

  val controller = new EADController(mockAuthAction, mcc, eADService, view)

  override def beforeEach(): Unit = {
    when(connector.fetchMrnStatus(any())(any(), any())).thenReturn(Future.successful(Some(MrnStatusSpec.completeMrnStatus)))
    super.beforeEach()
    authorizedUser()
  }

  override protected def afterEach(): Unit =
    super.afterEach()

  "EAD Controller" should {

    "return 200" when {

      "display page method is invoked" in {

        val mrn = "18GB9JLC3CU1LFGVR2"
        val result = controller.generateDocument(mrn).apply(getRequest())

        status(result) must be(OK)

      }
    }
  }
}
