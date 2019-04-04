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

import base.TestHelper._
import base.{CustomExportsBaseSpec, MockHttpClient, TestHelper}
import models._
import play.api.test.Helpers.OK
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.wco.dec.MetaData

class CustomsDeclareExportsMovementsConnectorSpec extends CustomExportsBaseSpec {
  import CustomsDeclareExportsMovementsConnectorSpec._

  "Customs Exports Movements Connector" should {

    "POST to Customs Declare Exports endpoint to save movement submission" in {
      val http = new MockHttpClient(
        expectedMovementsUrl(appConfig.saveMovementSubmission),
        movementSubmission,
        expectedHeaders,
        falseServerError,
        CustomsDeclareExportsMovementsResponse(OK, "success")
      )
      val client = new CustomsDeclareExportsMovementsConnector(appConfig, http)
      val response = client.saveMovementSubmission(movementSubmission)(hc, ec)

      response.futureValue.status must be(OK)
    }

  }

  private def expectedMovementsUrl(endpointUrl: String): String =
    s"${appConfig.customsDeclareExportsMovements}$endpointUrl"
}

object CustomsDeclareExportsMovementsConnectorSpec {
  val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(createRandomAlphanumericString(255))))
  val mrn: String = TestHelper.createRandomAlphanumericString(10)
  val metadata = MetaData()

  val conversationId: String = TestHelper.createRandomAlphanumericString(10)
  val eori: String = TestHelper.createRandomAlphanumericString(15)

  val expectedHeaders: Seq[(String, String)] = Seq.empty

  val falseServerError: Boolean = false
  val movementSubmission = MovementSubmission("eori1", "convid1", "ducr1", None, "EAL")

}
