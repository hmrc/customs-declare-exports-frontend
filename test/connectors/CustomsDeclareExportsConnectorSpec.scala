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
import models.{CustomsDeclareExportsResponse, MovementSubmission, Submission}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import play.api.http.Status.OK

import scala.concurrent.Future

class CustomsDeclareExportsConnectorSpec extends CustomExportsBaseSpec {

  val submission = Submission("eori", "id", Some("lrn"), Some("mrn"))
  val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(randomString(255))))
  val expectedHeaders: Map[String, String] = Map.empty
  val falseServerError: Boolean = false
  val movementSubmission = MovementSubmission("eori1", "convid1", "ducr1", None, "EAL")


  "CustomsDeclareExportsConnector" should {
    "POST submission to Customs Declare Exports" in saveSubmission() { response =>
      response.futureValue.status must be(OK)
    }

    "POST to Customs Declare Exports to save movements" in saveMovementSubmission() { response =>
      response.futureValue.status must be(OK)
    }
  }

  def saveSubmission()(test: Future[CustomsDeclareExportsResponse] => Unit): Unit = {
    val http = new MockHttpClient(expectedUrl(appConfig.saveSubmissionResponse),
      submission, expectedHeaders, falseServerError)
    val client = new CustomsDeclareExportsConnector(appConfig, http)
    test(client.saveSubmissionResponse(submission)(hc, ec))
  }

  def saveMovementSubmission(
  )(test: Future[CustomsDeclareExportsResponse] => Unit): Unit = {

    val http = new MockHttpClient(expectedUrl(appConfig.saveMovementSubmission),
      movementSubmission, expectedHeaders, falseServerError)
    val client = new CustomsDeclareExportsConnector(appConfig, http)
    test(client.saveMovementSubmission(movementSubmission)(hc, ec))
  }

  def expectedUrl(endpointUrl: String): String = s"${appConfig.customsDeclareExports}${endpointUrl}"

}
