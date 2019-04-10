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
import forms.declaration.Seal
import generators.Generators
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen.listOf
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import views.html.declaration.seal

class SealControllerSpec extends CustomExportsBaseSpec with Generators with PropertyChecks {

  private val uri = uriWithContextPath("/declaration/add-seal")

  val form: Form[Seal] = Form(Seal.formMapping)

  def view(form: Form[Seal], seals: Seq[Seal])(implicit request: FakeRequest[_]): Html =
    seal(form, seals)(appConfig, request, messages)

  before {
    authorizedUser()
    withCaching[Seq[Seal]](None, Seal.formId)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.StandardDec)), choiceId)
  }
  after {
    reset(mockCustomsCacheService)
  }

  "GET" should {

    "return 200 code" in {
      authorizedUser()

      val result = route(app, getRequest(uri)).value
      status(result) must be(OK)
    }

    "populate the form fields with data from cache" in {
      val request = getRequest(uri)

      forAll(listOf[Seal](sealArbitrary.arbitrary)) { seals =>
        withCaching[Seq[Seal]](Some(seals), Seal.formId)
        val result = route(app, request).value

        contentAsString(result).replaceCSRF mustBe
          view(form, seals)(request).body.replaceCSRF()
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
        }
      }

    }

    "add seal to the cache" when {

      "with valid data and on click of add" in {

        forAll(arbitrary[Seal], listOf[Seal](sealArbitrary.arbitrary)) { (seal, seals) =>
          withCaching[Seq[Seal]](Some(seals), Seal.formId)
          val body = Seq(("id", seal.id)) :+ addActionUrlEncoded

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

          status(result) must be(SEE_OTHER)
          val updatedSeals = seals :+ seal
          verify(mockCustomsCacheService)
            .cache[Seq[Seal]](any(), ArgumentMatchers.eq(Seal.formId), ArgumentMatchers.eq(updatedSeals))(
              any(),
              any(),
              any()
            )
        }
      }
    }
    "remove seal from the cache" when {

      " user click remove" in {

        forAll(listOf[Seal](sealArbitrary.arbitrary)) { (seals) =>
          whenever(seals.size > 1) {
            withCaching[Seq[Seal]](Some(seals), Seal.formId)
            val body = Seq(removeActionUrlEncoded((1).toString))

            val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

            status(result) must be(SEE_OTHER)
            val updatedSeals = seals.zipWithIndex.filter(_._2 != 1).map(_._1)
            verify(mockCustomsCacheService)
              .cache[Seq[Seal]](any(), ArgumentMatchers.eq(Seal.formId), ArgumentMatchers.eq(updatedSeals))(
                any(),
                any(),
                any()
              )
          }

        }
      }
    }

    "navigate to summary page" when {

      "on click of continue" in {
        forAll(arbitrary[Seal]) { seal =>
          withCaching[Seq[Seal]](None, Seal.formId)
          val payload = Seq(("id", seal.id)) :+ saveAndContinueActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
          status(result) must be(SEE_OTHER)
          result.futureValue.header.headers.get("Location") must be(
            Some("/customs-declare-exports/declaration/summary")
          )

        }
      }
    }
  }
}
