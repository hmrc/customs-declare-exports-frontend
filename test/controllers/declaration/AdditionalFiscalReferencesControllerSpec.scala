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
import org.mockito.Mockito.{reset, verify}
import org.mockito.ArgumentMatchers.any
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import play.api.test.Helpers._
import services.cache.ExportItem

class AdditionalFiscalReferencesControllerSpec extends CustomExportsBaseSpec {
  val cacheModel = createModelWithItem("")
  private val uri = uriWithContextPath(s"/declaration/items/${cacheModel.items.head.id}/additional-fiscal-references")
  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")

  override def beforeEach() {
    authorizedUser()
    withNewCaching(cacheModel)
    withCaching[AdditionalFiscalReferencesData](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  override def afterEach() {
    reset(mockExportsCacheService, mockCustomsCacheService, mockAuthConnector)
  }

  "Additional Fiscal References Controller on GET" should {

    "return 200 status code" in {
      val Some(result) = route(app, getRequest(uri))

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val cachedData = AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("France", "7232")))
      val cachedItem = ExportItem(id = "1244", additionalFiscalReferencesData = Some(cachedData))

      withNewCaching(createModelWithItem("").copy(items = Set(cachedItem)))
      withCaching[AdditionalFiscalReferencesData](Some(cachedData), AdditionalFiscalReferencesData.formId)

      val Some(result) = route(app, getRequest(uri))
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("France")
      page must include("7232")
    }
  }

  "Additional Fiscal References Controller on POST" should {

    "remove item from the cache" in {
      val cachedData = AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("FR", "7232")))
      withNewCaching(createModelWithItem("", Some(ExportItem("id", additionalFiscalReferencesData = Some(cachedData)))))

      val body = (Remove.toString, "0")
      val Some(result) = route(app, postRequestFormUrlEncoded(uri, body))

      status(result) must be(SEE_OTHER)
    }
    "return bad request" when {

      "form contains errors during adding item" in {
        val body = Seq(("country", "hello"), ("reference", "12345"), addActionUrlEncoded)
        val request =
          postRequestFormUrlEncoded(uri, body: _*)

        val Some(result) = route(app, request)

        status(result) must be(BAD_REQUEST)
      }

      "form contains errors during saving" in {
        val body = Seq(("country", "hello"), ("reference", "12345"), saveAndContinueActionUrlEncoded)
        val request = postRequestFormUrlEncoded(uri, body: _*)

        val Some(result) = route(app, request)

        status(result) must be(BAD_REQUEST)
      }
    }

    "return see other" when {

      "user adds correct item" in {
        val body = Seq(("country", "FR"), ("reference", "12345"), addActionUrlEncoded)
        val request = postRequestFormUrlEncoded(uri, body: _*)

        val Some(result) = route(app, request)

        status(result) must be(SEE_OTHER)

      }

      "user clicks save with empty form and item in the cache" in {
        val cachedData = AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("FR", "7232")))
        withNewCaching(
          createModelWithItem("", Some(ExportItem("id", additionalFiscalReferencesData = Some(cachedData))))
        )

        val body = Seq(saveAndContinueActionUrlEncoded)
        val request = postRequestFormUrlEncoded(uri, body: _*)

        val Some(result) = route(app, request)

        status(result) must be(SEE_OTHER)

      }

      "user clicks save with form filled" in {
        val body = Seq(("country", "FR"), ("reference", "12345"), saveAndContinueActionUrlEncoded)
        val request =
          postRequestFormUrlEncoded(uri, body: _*)

        val Some(result) = route(app, request)

        status(result) must be(SEE_OTHER)

      }
    }
  }
}
