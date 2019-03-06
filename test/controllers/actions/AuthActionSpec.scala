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

package controllers.actions

import base.CustomExportsBaseSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.InsufficientEnrolments

class AuthActionSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/choice")

  // should we check other missing fields ?
  "Auth Action" should {

    "return InsufficientEnrolments when EORI number is missing" in {
      userWithoutEori()

      val result = route(app, FakeRequest("GET", uri)).get

      intercept[InsufficientEnrolments](status(result))
    }

    "return NoExternalId when External Id is missing" in {
      userWithoutExternalId()

      val result = route(app, FakeRequest("GET", uri)).get

      intercept[NoExternalId](status(result))
    }
  }
}
