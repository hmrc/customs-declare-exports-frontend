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
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.NatureOfTransaction
import forms.declaration.NatureOfTransactionSpec._
import helpers.views.declaration.NatureOfTransactionMessages
import org.mockito.Mockito.reset
import play.api.test.Helpers._

class NatureOfTransactionControllerSpec extends CustomExportsBaseSpec with NatureOfTransactionMessages {

  private val uri = uriWithContextPath("/declaration/nature-of-transaction")

  override def beforeEach() {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withChoice(SupplementaryDec)))
  }

  override def afterEach() {
    super.afterEach()
    reset(mockExportsCacheService)
  }

  "Nature Of Transaction Controller on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {
      withNewCaching(aDeclaration(withChoice(SupplementaryDec), withNatureOfTransaction("1")))

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("Purchase")
    }
  }

  "Nature Of Transaction Controller on POST" should {

    "validate request and redirect - empty value" in {

      val result = route(app, postRequest(uri, emptyNatureOfTransactionJSON)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(natureOfTransactionEmpty))
      verifyTheCacheIsUnchanged()
    }

    "validate request and redirect - incorrect values" in {

      val result = route(app, postRequest(uri, incorrectNatureOfTransactionJSON)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)

      stringResult must include(messages(natureOfTransactionError))
      verifyTheCacheIsUnchanged()
    }

    "validate request and redirect - correct values" in {

      val result = route(app, postRequest(uri, correctNatureOfTransactionJSON)).get

      status(result) must be(SEE_OTHER)

      theCacheModelUpdated.natureOfTransaction.get mustBe NatureOfTransaction("1")

      redirectLocation(result) must be(Some("/customs-declare-exports/declaration/previous-documents"))
    }
  }

}
