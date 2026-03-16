/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.general.routes.RootController
import models.DeclarationType.{DeclarationType, OCCASIONAL, STANDARD, SUPPLEMENTARY}
import models.requests.SessionHelper.errorKey
import models.requests.{AuthenticatedRequest, JourneyRequest}
import models.{IdentityData, SignedInUser}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.cache.{ExportsCacheService, ExportsDeclarationBuilder}
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JourneyActionSpec extends UnitWithMocksSpec with BeforeAndAfterEach with ExportsDeclarationBuilder with RequestBuilder {

  private val cache = mock[ExportsCacheService]
  private val block = mock[JourneyRequest[_] => Future[Result]]
  private val user = SignedInUser("eori", Enrolments(Set.empty), IdentityData())
  private val declaration = aDeclaration()

  private val journeyAction = new JourneyAction(cache)

  private val declarationId = "id"

  private def verifiedEmailRequest(maybeId: Option[String] = Some(declarationId)): AuthenticatedRequest[_] = {
    val request = maybeId match {
      case Some(id) => FakeRequest().withSession("declarationUuid" -> id)
      case _        => FakeRequest()
    }
    buildVerifiedEmailRequest(request, user)
  }

  override def afterEach(): Unit = {
    reset(cache, block)
    super.afterEach()
  }

  "JourneyAction" should {

    "permit requests" when {
      "the session provides a known declaration id" in {
        given(block.apply(any())).willReturn(Future.successful(Results.Ok))
        given(cache.get(refEq(declarationId))(any[HeaderCarrier])).willReturn(Future.successful(Some(declaration)))

        await(journeyAction.invokeBlock(verifiedEmailRequest(), block)) mustBe Results.Ok

        val result = theRequestBuilt
        result.cacheModel mustBe declaration
      }
    }

    "block requests" when {

      "the session provides an unknown declaration id" in {
        given(cache.get(refEq(declarationId))(any[HeaderCarrier])).willReturn(Future.successful(None))
        invokeAction(verifiedEmailRequest())
      }

      "the session does not contain the declaration id" in {
        val request = verifiedEmailRequest(None)
        val result = invokeAction(request)
        result.session(request)(errorKey) mustBe "error.root.redirect.1|error.root.redirect.2"
      }
    }
  }
  "JourneyAction on specific journeys" should {

    "permit requests" when {
      "the session provides a known declaration id and" when {

        "on unshared journey" in {
          given(block.apply(any())).willReturn(Future.successful(Results.Ok))
          given(cache.get(refEq(declarationId))(any[HeaderCarrier])).willReturn(Future.successful(Some(declaration)))

          await(journeyAction(STANDARD).invokeBlock(verifiedEmailRequest(), block)) mustBe Results.Ok

          val response = theRequestBuilt
          response.cacheModel mustBe declaration
        }

        "on shared journey" in {
          given(block.apply(any())).willReturn(Future.successful(Results.Ok))
          given(cache.get(refEq(declarationId))(any[HeaderCarrier])).willReturn(Future.successful(Some(declaration)))

          await(journeyAction(STANDARD, SUPPLEMENTARY).invokeBlock(verifiedEmailRequest(), block)) mustBe Results.Ok

          val response = theRequestBuilt
          response.cacheModel mustBe declaration
        }
      }
    }

    "block requests" when {

      "the session provides an unknown declaration id" in {
        given(cache.get(refEq(declarationId))(any[HeaderCarrier])).willReturn(Future.successful(None))
        invokeAction(verifiedEmailRequest(), Some(STANDARD))
      }

      "the session does not contain the declaration id" in {
        val request = verifiedEmailRequest(None)
        val result = invokeAction(request, Some(STANDARD))
        result.session(request)(errorKey) mustBe "error.root.redirect.1|error.root.redirect.2"
      }

      "the session provides an id for a declaration of a different journey type" in {
        given(cache.get(refEq(declarationId))(any[HeaderCarrier])).willReturn(Future.successful(Some(declaration)))
        invokeAction(verifiedEmailRequest(), Some(OCCASIONAL))
      }
    }
  }

  private def invokeAction(request: AuthenticatedRequest[_], maybeType: Option[DeclarationType] = None): Result = {
    val action = maybeType.fold(journeyAction.invokeBlock(request, block))(journeyAction(_).invokeBlock(request, block))
    val result = await(action)
    result.header.status mustBe SEE_OTHER
    result.header.headers("Location") mustBe RootController.displayPage.url
    result
  }

  private def theRequestBuilt: JourneyRequest[_] = {
    val captor = ArgumentCaptor.forClass(classOf[JourneyRequest[_]])
    verify(block).apply(captor.capture())
    captor.getValue
  }
}
