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
import base.{MockConnectors, TestHelper}
import com.codahale.metrics.SharedMetricRegistries
import config.AppConfig
import org.scalatest.concurrent.ScalaFutures
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import unit.base.UnitSpec

import scala.concurrent.ExecutionContext.global

class NrsServiceSpec extends UnitSpec with MockConnectors with ScalaFutures {

  SharedMetricRegistries.clear()

  val injector = GuiceApplicationBuilder().injector()
  val appConfig = injector.instanceOf[AppConfig]
  val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(TestHelper.createRandomString(255))))

  val nrsService = new NRSService(appConfig, mockNrsConnector)

  val signedInUser = newUser("12345", "external1")

  "NrsService " should {

    "submit user submission to NRS service with a valid submissionId" in {

      submitNrsRequest()

      val nrsResponse = nrsService.submit("conversationId1", "payload 1", "ducr1")(hc, global, signedInUser)

      nrsResponse.futureValue.nrSubmissionId must be("submissionId1")
    }
  }
}
