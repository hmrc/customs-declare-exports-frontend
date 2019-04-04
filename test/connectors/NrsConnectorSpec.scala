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

package connectors

import base.ExportsTestData._
import base.{CustomExportsBaseSpec, MockHttpClient, TestHelper}
import models._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization

import scala.concurrent.Future

class NrsConnectorSpec extends CustomExportsBaseSpec {

  val submission = Submission("eori", "id", "ducr", Some("lrn"), Some("mrn"), Accepted)
  val hc: HeaderCarrier = HeaderCarrier(
    authorization = Some(Authorization(TestHelper.createRandomAlphanumericString(255)))
  )
  val expectedHeaders: Seq[(String, String)] =
    Seq(("Content-Type", "application/json"), ("X-API-Key", appConfig.nrsApiKey))
  val nrsMetadata = Metadata(
    "cds",
    "cds-exports",
    "application/json",
    Some("248857ca67c92e1c18459ff287139fd8409372221e32d245ad8cc470dd5c80d5"),
    nrsTimeStamp,
    newUser("12345", "external1").identityData,
    "bearer-token",
    HeaderData(),
    SearchKeys(Some("converstionId1"), Some("ducr1"))
  )

  "NrsConnector" should {

    "submit non repudiation request successfully" in submitNonRepudiation() { response =>
      response.futureValue.nrSubmissionId must be("submissionId1")
    }
  }

  def submitNonRepudiation()(test: Future[NrsSubmissionResponse] => Unit): Unit = {
    val nrsSubmission = NRSSubmission(submission.toString, nrsMetadata)
    val expectedUrl = s"${appConfig.nrsServiceUrl}/submission"
    val http =
      new MockHttpClient(expectedUrl, nrsSubmission, expectedHeaders, false, NrsSubmissionResponse("submissionId1"))
    val client = new NrsConnector(appConfig, http)
    test(client.submitNonRepudiation(nrsSubmission)(hc, ec))
  }

}
