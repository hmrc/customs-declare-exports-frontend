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

package models

import forms.SimpleDeclarationForm
import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.hmrc.auth.core.Enrolments

case class SignedInUser(eori: String, enrolments: Enrolments, identityData: IdentityData)

case class CustomsDeclarationsResponse(status: Int, conversationId: Option[String])

case class CustomsDeclareExportsResponse(status: Int, message: String)

object CustomsDeclareExportsResponse {
  implicit val format = Json.format[CustomsDeclareExportsResponse]
}

case class UserSession(
  sessionId: String,
  loggedInDateTime: DateTime = DateTime.now,
  simpleDeclarationForm: Option[SimpleDeclarationForm] = None
)

object UserSession {
  implicit val formats = Json.format[UserSession]
}
