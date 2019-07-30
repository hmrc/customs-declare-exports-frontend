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

import java.time.LocalDateTime

import base.CSRFUtil._
import base.CustomExportsBaseSpec
import base.TestHelper._
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.Seal
import generators.Generators
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, times, verify}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen.listOf
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.mvc.Request
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.Helpers._
import services.cache.ExportsCacheModel
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import views.html.declaration.seal

class SealControllerSpec extends CustomExportsBaseSpec with Generators with PropertyChecks {

  private val form: Form[Seal] = Form(Seal.formMapping)
  private val sealPage = app.injector.instanceOf[seal]
  private val uri = uriWithContextPath("/declaration/add-seal")

  override def beforeEach() {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aCacheModel(withChoice(SupplementaryDec)))
    withCaching[Seq[Seal]](None, Seal.formId)
  }

  override def afterEach() {
    super.afterEach()
    reset(mockCustomsCacheService, mockExportsCacheService)
  }

  private def view(form: Form[Seal], seals: Seq[Seal], container: Boolean = false)(implicit request: Request[_]) =
    sealPage(form, seals, container)(appConfig, request, messages)

  "GET" should {

    "return 200 code" in {
      authorizedUser()

      val result = route(app, getRequest(uri)).value
      status(result) must be(OK)
      verify(mockExportsCacheService, times(2)).get(anyString)
    }

    "populate the form fields with data from cache" in {
      val request = addCSRFToken(getRequest(uri))

      forAll(listOf[Seal](sealArbitrary.arbitrary)) { seals =>
        withCache(seals)
        val result = route(app, request).value
        contentAsString(result).replaceCSRF mustBe view(form, seals)(request).body.replaceCSRF()
      }
    }
  }

  ".onSubmit" should {

    "return UNAUTHORIZED" when {

      "user does not have an EORI" in {
        userWithoutEori()
        val body = Seq(("id", "A1"))
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

        intercept[InsufficientEnrolments](status(result))
        verifyTheCacheIsUnchanged()
      }
    }

    "return BAD_REQUEST" when {

      "invalid data is submitted" in {
        forAll(listOf[Seal](sealArbitrary.arbitrary)) { seals =>
          withCache(seals)

          val body = Seq(("id", "")) :+ addActionUrlEncoded
          val request = postRequestFormUrlEncoded(uri, body: _*)

          val result = route(app, request).value

          status(result) must be(BAD_REQUEST)
          contentAsString(result).replaceCSRF mustBe view(form.bindFromRequest()(request), seals)(request).body.replaceCSRF
          verifyTheCacheIsUnchanged()
          reset(mockExportsCacheService)
        }
      }

    }

    "add seal to the cache" when {

      "with valid data and on click of add" in {

        val body = Seq(("id", "12345")) :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value
        status(result) must be(SEE_OTHER)
        verify(mockCustomsCacheService)
          .cache[Seq[Seal]](any(), ArgumentMatchers.eq(Seal.formId), any())(any(), any(), any())
        theCacheModelUpdated.seals(0) mustBe Seal("12345")
      }
    }
    "remove seal from the cache" when {

      " user click remove" in {
        withCache(Seq(Seal("123"), Seal("4321")))
        val body = Seq(removeActionUrlEncoded((1).toString))
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value
        status(result) must be(SEE_OTHER)
        verify(mockCustomsCacheService)
          .cache[Seq[Seal]](any(), ArgumentMatchers.eq(Seal.formId), any())(any(), any(), any())
        theCacheModelUpdated.seals(0) mustBe Seal("123")
      }
    }
  }

  "navigate to summary page" when {

    "on click of continue" in {
      forAll(arbitrary[Seal]) { seal =>
        withNewCaching(aCacheModel(withChoice(SupplementaryDec)))
        val payload = Seq(("id", seal.id)) :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
        status(result) must be(SEE_OTHER)
        result.futureValue.header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/summary"))
        theCacheModelUpdated.seals(0) mustBe Seal(seal.id)
        reset(mockExportsCacheService)
      }
    }
  }

  private def withCache(data: Seq[Seal]) =
    withNewCaching(aCacheModel(withChoice(SupplementaryDec), withSeals(data)))
}
