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

package services

import base.ExportsTestData.newUser
import base.{CustomExportsBaseSpec, TestHelper}
import org.joda.time.DateTime
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization

class NrsServiceSpec extends CustomExportsBaseSpec {

  implicit val hc: HeaderCarrier =
    HeaderCarrier(
      authorization = Some(Authorization(TestHelper.createRandomAlphanumericString(255))),
      nsStamp = DateTime.now().getMillis
    )
  val nrsService = new NRSService(appConfig, mockNrsConnector)
  implicit val signedInUser = newUser("12345", "external1")
  "NrsService " should {
    " submit user submission to NRS service with a valid submissionId" in {
      submitNrsRequest()
      val nrsResponse = nrsService.submit("conversationId1", "payload 1", "ducr1")
      nrsResponse.futureValue.nrSubmissionId must be("submissionId1")
    }
  }
}
