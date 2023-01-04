/*
 * Copyright 2023 HM Revenue & Customs
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

import base.{RequestBuilder, UnitWithMocksSpec}
import com.typesafe.config.ConfigFactory
import config.featureFlags.FeatureSwitchConfig
import features.Feature
import models.requests.JourneyRequest
import models.{IdentityData, SignedInUser}
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import play.api.Configuration
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.cache.ExportsDeclarationBuilder
import uk.gov.hmrc.auth.core.Enrolments

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FeatureFlagActionSpec extends UnitWithMocksSpec with BeforeAndAfterEach with ExportsDeclarationBuilder with RequestBuilder {

  private val block = mock[JourneyRequest[_] => Future[Result]]
  private val journey =
    new JourneyRequest(buildAuthenticatedRequest(FakeRequest(), SignedInUser("eori", Enrolments(Set.empty), IdentityData())), aDeclaration())

  override def afterEach(): Unit = {
    reset(block)
    super.afterEach()
  }

  "refine" should {

    "permit request" when {
      "feature flag enabled" in {

        given(block.apply(any())).willReturn(Future.successful(Results.Ok))

        val refiner =
          new FeatureFlagAction(new FeatureSwitchConfig(Configuration(ConfigFactory.parseString("microservice.services.features.default=enabled"))))

        await(refiner(Feature.default).invokeBlock(journey, block)) mustBe Results.Ok

      }

    }

    "block request" when {

      "feature flag disabled" in {

        val refiner =
          new FeatureFlagAction(new FeatureSwitchConfig(Configuration(ConfigFactory.parseString("microservice.services.features.default=disabled"))))

        await(refiner(Feature.default).invokeBlock(journey, block)) mustBe Results.Redirect(controllers.routes.RootController.displayPage)
      }

    }
  }
}
