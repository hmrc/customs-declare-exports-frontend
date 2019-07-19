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

package controllers.declaration

import base.CustomExportsBaseSpec
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.{AdditionalFiscalReferencesData, FiscalInformation}
import helpers.views.declaration.FiscalInformationMessages
import org.mockito.Mockito.reset
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers.{OK, route, status, _}
import services.cache.ExportItem

class FiscalInformationControllerSpec extends CustomExportsBaseSpec with FiscalInformationMessages {

  private val existingItem = ExportItem("id")
  private val cacheModel = createModelWithItem("", Some(existingItem))
  private val uri: String = uriWithContextPath(s"/declaration/items/${cacheModel.items.head.id}/fiscal-information")
  private val emptyFiscalInformationJson: JsValue = JsObject(Map("onwardSupplyRelief" -> JsString("")))
  private val incorrectFiscalInformation: JsValue = JsObject(
    Map("onwardSupplyRelief" -> JsString("NeitherRadioOption"))
  )
  private val fiscalInformationWithYes: JsValue = JsObject(Map("onwardSupplyRelief" -> JsString("Yes")))
  private val fiscalInformationWithNo: JsValue = JsObject(Map("onwardSupplyRelief" -> JsString("No")))

  override def beforeEach {
    authorizedUser()
    withNewCaching(cacheModel)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.StandardDec)), choiceId)
  }

  override def afterEach() {
    reset(mockCustomsCacheService, mockExportsCacheService)
  }

  "GET" should {

    "return 200 on GET request with a success" in {
      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {
      withNewCaching(createModelWithItem("", Some(ExportItem("id", fiscalInformation = Some(FiscalInformation("Yes"))))))

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
      contentAsString(result) must include("Yes")
    }
  }

  "POST" should {

    "return bad request for empty form" in {
      val result = route(app, postRequest(uri, emptyFiscalInformationJson)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(errorMessageEmpty))
      verifyTheCacheIsUnchanged()
    }

    "return bad request for incorrect values" in {
      val result = route(app, postRequest(uri, incorrectFiscalInformation)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(errorMessageIncorrect))
      verifyTheCacheIsUnchanged()
    }

    "redirect to 'AdditionalFiscalReferences' page when choice is yes" in {
      val result = route(app, postRequest(uri, fiscalInformationWithYes)).get

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(
        Some(routes.AdditionalFiscalReferencesController.displayPage(cacheModel.items.head.id).url)
      )
      theCacheModelUpdated.items.head must be(ExportItem("id", fiscalInformation = Some(FiscalInformation("Yes"))))
    }

    "redirect to 'ItemsSummary' page and clear fiscal references when choice is no" in {
      withNewCaching(createModelWithItem("", Some(ExportItem("id", additionalFiscalReferencesData = Some(mock[AdditionalFiscalReferencesData])))))

      val result = route(app, postRequest(uri, fiscalInformationWithNo)).get

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(
        Some(routes.ItemTypePageController.displayPage(cacheModel.items.head.id).url)
      )
      theCacheModelUpdated.items.head must be(ExportItem("id", fiscalInformation = Some(FiscalInformation("No")), additionalFiscalReferencesData = None))
    }


  }
}
