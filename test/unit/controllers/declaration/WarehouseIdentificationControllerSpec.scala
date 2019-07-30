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

import controllers.declaration.WarehouseIdentificationController
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.WarehouseIdentification
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import org.scalatest.BeforeAndAfterEach
import unit.base.ControllerSpec
import views.html.declaration.warehouse_identification
import play.api.test.Helpers._

class WarehouseIdentificationControllerSpec extends ControllerSpec with BeforeAndAfterEach {

  val controller = new WarehouseIdentificationController(
    appConfig = minimalAppConfig,
    authenticate = mockAuthAction,
    journeyType = mockJourneyAction,
    customsCacheService = mockCustomsCacheService,
    exportsCacheService = mockExportsCacheService,
    mcc = stubMessagesControllerComponents(),
    warehouseIdentificationPage = new warehouse_identification(mainTemplate)
  )



  "WerehouseIdentificationController" should {
    "return 200 OK" when {
      "request were made" in {
        val response = controller.displayForm().apply(getRequest())
        status(response) must be(OK)
        verify(mockExportsCacheService, times(2)).get(any())
      }
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aCacheModel(withChoice(SupplementaryDec), withoutWarehouseIdentification()))
//    withCaching[WarehouseIdentification](None)
  }
}
