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

package services.cache

import base.CustomExportsBaseSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.Future

class ExportsCacheServiceSpec extends CustomExportsBaseSpec with BeforeAndAfterEach {

  val mockRepo = mock[ExportsCacheModelRepository]
  val service = new ExportsCacheService(mockRepo)
  val sessionId = "12345"

  override def beforeEach(): Unit =
    reset(mockRepo)

  "ExportsCacheService" when {

    "on get" should {

      //TODO does this add value?
      "return a cached model when exist" in {
        val returnedModel = createModel(sessionId)
        when(mockRepo.get(sessionId))
          .thenReturn(Future.successful(Right(returnedModel)))

        val result = service.get(sessionId).futureValue
        result must be(Right(returnedModel))
      }

      //TODO does this add value?
      "return a left error when doesn't exist" in {
        when(mockRepo.get(sessionId))
          .thenReturn(Future.successful(Left("some error")))

        val result = service.get(sessionId).futureValue
        result must be(Left("some error"))
      }
    }

    "on upsert" should {

      "update returns a model when upsert is successful" in {
        val returnedModel = createModel(sessionId)
        when(mockRepo.upsert(any(), any()))
          .thenReturn(Future.successful(Some(returnedModel)))

        val result = service.update(sessionId, returnedModel).futureValue
        result must be(Right(returnedModel))
      }
    }

    "update returns a String with an error message when upsert is unsuccessful" in {
      val returnedModel = createModel(sessionId)
      when(mockRepo.upsert(any(), any()))
        .thenReturn(Future.successful(None))

      val result = service.update(sessionId, returnedModel).futureValue
      result must be(Left(s"Unable to retrieve a model for session id $sessionId"))
    }
  }

}
