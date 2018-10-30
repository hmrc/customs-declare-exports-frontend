/*
 * Copyright 2018 HM Revenue & Customs
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

package connectors

import base.{CustomExportsBaseSpec, MockHttpClient}
import models.{CustomsDeclareExportsResponse, Submission}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import play.api.http.Status.OK

import scala.concurrent.Future

class CustomsDeclareExportsConnectorSpec extends CustomExportsBaseSpec {

  val submission = Submission("eori", "id", Some("lrn"), Some("mrn"))

  "CustomsDeclareExportsConnector" should {
    "POST submission to Customs Declare Exports" in saveSubmission(submission) { response =>
      response.futureValue.status must be (OK)
    }
  }

  def saveSubmission(
    submission: Submission,
    hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(randomString(255))))
  )(test: Future[CustomsDeclareExportsResponse] => Unit): Unit = {
    val expectedUrl: String = s"${appConfig.customsDeclareExports}${appConfig.saveSubmissionResponse}"
    val falseServerError: Boolean = false
    val expectedHeaders: Map[String, String] = Map.empty
    val http = new MockHttpClient(expectedUrl, submission, expectedHeaders, falseServerError)
    val client = new CustomsDeclareExportsConnector(appConfig, http)
    test(client.saveSubmissionResponse(submission)(hc, ec))
  }
}
