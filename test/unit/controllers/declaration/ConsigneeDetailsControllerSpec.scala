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

import controllers.declaration.ConsigneeDetailsController
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.{ConsigneeDetails, EntityDetails}
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.cache.ExportsCacheModel
import unit.base.ControllerSpec
import views.html.declaration.consignee_details

class ConsigneeDetailsControllerSpec extends ControllerSpec {

  trait SetUp {
    val consigneeDetailsPage = new consignee_details(mainTemplate)

    val controller = new ConsigneeDetailsController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      consigneeDetailsPage
    )(ec)

    val model: ExportsCacheModel = aCacheModel(withChoice(SupplementaryDec))
    authorizedUser()
    withNewCaching(model)
  }

  "Consignee Details controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in new SetUp {
        val result = controller.displayPage()(getRequest(model))

        status(result) must be(OK)
      }

      "display page method is invoked and cache contrains data" in new SetUp {

        val modelWithDetails = aCacheModel(withConsigneeDetails(Some("123"), None))

        withNewCaching(modelWithDetails)

        val result = controller.displayPage()(getRequest(modelWithDetails))

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in new SetUp {

        val incorrectForm = Json.toJson(ConsigneeDetails(EntityDetails(None, None)))

        val result = controller.saveAddress()(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "form is correct" in new SetUp {

        val correctForm = Json.toJson(ConsigneeDetails(EntityDetails(Some("1234"), None)))

        val result = controller.saveAddress()(postRequest(correctForm))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(
          Some(controllers.declaration.routes.DeclarantDetailsController.displayForm().url)
        )
      }
    }
  }
}
