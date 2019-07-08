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

import base.CSRFUtil._
import base.CustomExportsBaseSpec
import base.TestHelper._
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.{Seal, TransportDetails}
import generators.Generators
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen.listOf
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.mvc.Request
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import views.html.declaration.seal

class SealControllerSpec extends CustomExportsBaseSpec with Generators with PropertyChecks {

  private val form: Form[Seal] = Form(Seal.formMapping)
  private val sealPage = app.injector.instanceOf[seal]
  private val uri = uriWithContextPath("/declaration/add-seal")

  override def beforeEach() {
    authorizedUser()
    withNewCaching(createModel())
    withCaching[Seq[Seal]](None, Seal.formId)
    withCaching[TransportDetails](None, TransportDetails.formId)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.StandardDec)), choiceId)
  }

  override def afterEach() {
    reset(mockCustomsCacheService, mockExportsCacheService)
  }

  private def view(form: Form[Seal], seals: Seq[Seal], container: Boolean = false)(implicit request: Request[_]) =
    sealPage(form, seals, container)(appConfig, request, messages)

  "GET" should {

    "return 200 code" in {
      authorizedUser()

      val result = route(app, getRequest(uri)).value
      status(result) must be(OK)
    }

    "populate the form fields with data from cache" in {
      val request = addCSRFToken(getRequest(uri))

      forAll(listOf[Seal](sealArbitrary.arbitrary)) { seals =>
        withCaching[Seq[Seal]](Some(seals), Seal.formId)
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
      }
    }

    "return BAD_REQUEST" when {

      "invalid data is submitted" in {
        forAll(listOf[Seal](sealArbitrary.arbitrary)) { seals =>
          withCaching[Seq[Seal]](Some(seals), Seal.formId)

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
        theCacheModelUpdated.seals.size mustBe 1
      }
    }
    "remove seal from the cache" when {

      " user click remove" in {
        withCaching[Seq[Seal]](Some(Seq(Seal("123"), Seal("4321"))), Seal.formId)
        val body = Seq(removeActionUrlEncoded((1).toString))
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value
        status(result) must be(SEE_OTHER)
        verify(mockCustomsCacheService)
          .cache[Seq[Seal]](any(), ArgumentMatchers.eq(Seal.formId), any())(any(), any(), any())
        theCacheModelUpdated.seals.size mustBe 1
      }
    }
  }

  "navigate to summary page" when {

    "on click of continue" in {
      forAll(arbitrary[Seal]) { seal =>
        withNewCaching(createModel())
        withCaching[Seq[Seal]](None, Seal.formId)
        val payload = Seq(("id", seal.id)) :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
        status(result) must be(SEE_OTHER)
        result.futureValue.header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/summary"))
        theCacheModelUpdated.seals.size mustBe 1
        reset(mockExportsCacheService)
      }
    }
  }
}
