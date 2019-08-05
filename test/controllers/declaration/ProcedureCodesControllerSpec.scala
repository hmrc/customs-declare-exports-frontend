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

import base.{CustomExportsBaseSpec, ViewValidator}
import controllers.util.{Add, Remove, SaveAndContinue}
import helpers.views.declaration.{CommonMessages, ProcedureCodesMessages}
import models.declaration.ProcedureCodesData
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify}
import play.api.test.Helpers._
import services.cache.ExportItem

class ProcedureCodesControllerSpec
    extends CustomExportsBaseSpec with ViewValidator with ProcedureCodesMessages with CommonMessages {
  import ProcedureCodesControllerSpec.cacheWithMaximumAmountOfAdditionalCodes

  private val itemModel = aCacheModel(withChoice("SMP"), withItem())
  private val uri = uriWithContextPath(s"/declaration/items/${itemModel.items.head.id}/procedure-codes")
  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")

  override def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    withNewCaching(itemModel)
  }

  override def afterEach(): Unit = {
    super.afterEach()

    reset(mockExportsCacheService)
  }

  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  "Procedure Codes Controller on GET" should {

    "return 200 status code" in {

      val Some(result) = route(app, getRequest(uri, sessionId = itemModel.sessionId))

      status(result) must be(OK)

      verify(mockExportsCacheService).get(any[String])
    }

    "read item from cache and display it" in {
      val Some(result) = route(app, getRequest(uri, sessionId = itemModel.sessionId))

      status(result) must be(OK)
      verify(mockExportsCacheService).get(any[String])
    }
  }

  "Procedure Codes Controller on POST" should {

    "return 400 (Bad Request)" when {

      "form is incorrect during adding" in {

        val body = Seq(("procedureCode", "incorrect"), addActionUrlEncoded)

        val Some(result) = route(app, postRequestFormUrlEncoded(uri, itemModel.sessionId)(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "form is incorrect during saving" in {

        val body = Seq(("procedureCode", "incorrect"), saveAndContinueActionUrlEncoded)

        val Some(result) = route(app, postRequestFormUrlEncoded(uri, itemModel.sessionId)(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "form and cache is empty during saving" in {

        val body = Seq(("procedureCode", ""), ("additionalProcedureCode", ""), saveAndContinueActionUrlEncoded)

        val Some(result) = route(app, postRequestFormUrlEncoded(uri, itemModel.sessionId)(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "procedureCode is empty but has additional procedure codes in cache, it should return a bad request" in {

        val cachedData = ProcedureCodesData(Some("1234"), Seq("123"))
        val model = aCacheModel(withItem(ExportItem("id", procedureCodes = Some(cachedData))), withChoice("SMP"))
        withNewCaching(model)

        val body = Seq(("procedureCode", ""), ("additionalProcedureCode", ""), saveAndContinueActionUrlEncoded)

        val Some(result) = route(app, postRequestFormUrlEncoded(uri, model.sessionId)(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "maximum amount of codes are reached" in {

        val model = aCacheModel(
          withItem(ExportItem("id", procedureCodes = Some(cacheWithMaximumAmountOfAdditionalCodes))),
          withChoice("SMP")
        )
        withNewCaching(model)

        val body = Seq(("procedureCode", "1234"), ("additionalProcedureCode", "321"), addActionUrlEncoded)

        val Some(result) = route(app, postRequestFormUrlEncoded(uri, model.sessionId)(body: _*))

        status(result) must be(BAD_REQUEST)
      }

      "code is duplicated" in {

        val cachedData = ProcedureCodesData(Some("1234"), Seq("123"))
        val model = aCacheModel(withItem(ExportItem("id", procedureCodes = Some(cachedData))), withChoice("SMP"))

        withNewCaching(model)

        val body = Seq(("procedureCode", "1234"), ("additionalProcedureCode", "123"), addActionUrlEncoded)

        val Some(result) = route(app, postRequestFormUrlEncoded(uri, model.sessionId)(body: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (See Other) and redirect to the same page" when {

      "form is correct during adding" in {

        val body = Seq(("procedureCode", "1234"), ("additionalProcedureCode", "321"), addActionUrlEncoded)

        val Some(result) = route(app, postRequestFormUrlEncoded(uri, itemModel.sessionId)(body: _*))

        status(result) must be(SEE_OTHER)

        verify(mockExportsCacheService, times(1)).update(any(), any())
      }

      "user remove existing code" in {

        val cachedData = ProcedureCodesData(Some("1234"), Seq("123"))
        val model = aCacheModel(withItem(ExportItem("id", procedureCodes = Some(cachedData))), withChoice("SMP"))
        withNewCaching(model)

        val body = removeActionUrlEncoded("123")

        val Some(result) = route(app, postRequestFormUrlEncoded(uri, model.sessionId)(body))

        status(result) must be(OK)

        verify(mockExportsCacheService, times(1)).update(any(), any())
      }
    }

    "return 303 (See Other) and redirect to the new page" when {

      "form is correct during saving with empty cache" in {

        val body = Seq(("procedureCode", "1234"), ("additionalProcedureCode", "123"), saveAndContinueActionUrlEncoded)

        val Some(result) = route(app, postRequestFormUrlEncoded(uri, itemModel.sessionId)(body: _*))

        status(result) must be(SEE_OTHER)

        verify(mockExportsCacheService, times(1)).update(any(), any())
      }

      "form is empty but cache contains at least one item" in {

        val cachedData = ProcedureCodesData(Some("1234"), Seq("123"))
        val model = aCacheModel(withItem(ExportItem("id", procedureCodes = Some(cachedData))), withChoice("SMP"))
        withNewCaching(model)

        val body = Seq(("procedureCode", "1234"), saveAndContinueActionUrlEncoded)

        val Some(result) = route(app, postRequestFormUrlEncoded(uri, model.sessionId)(body: _*))

        status(result) must be(SEE_OTHER)

        verify(mockExportsCacheService, times(1)).update(any(), any())
      }
    }
  }
}

object ProcedureCodesControllerSpec {
  val cacheWithMaximumAmountOfAdditionalCodes =
    ProcedureCodesData(Some("1234"), Seq.range[Int](100, 200, 1).map(_.toString))
}
