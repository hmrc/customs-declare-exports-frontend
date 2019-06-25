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


import java.time.LocalDateTime

import base.CustomExportsBaseSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.Future

class ExportsCacheServiceSpec extends CustomExportsBaseSpec with BeforeAndAfterEach {

  val mockRepo = mock[ExportsCacheModelRepository]
  val service = new ExportsCacheService(mockRepo)

  override def beforeEach(): Unit = {
    reset(mockRepo)
  }

  "ExportsCacheService" should {

    val model = ExportsCacheModel("SessionId", "draftId", LocalDateTime.now(), LocalDateTime.now(), "choice")

    "return a Left with error message when repository throws exception" in {
      val errorMessage = "some error message"
      when(mockRepo.save(any())).thenReturn(Future.failed(new RuntimeException(errorMessage)))

      service.save(model).futureValue must be(Left(errorMessage))
    }

    "return a Right Unit when repository successfully saves model" in {
      when(mockRepo.save(any())).thenReturn(Future.successful(true))

      service.save(model).futureValue must be(Right(()))
    }

    "return a Left with error message when repository fails to save model" in {
      when(mockRepo.save(any())).thenReturn(Future.successful(false))

      service.save(model).futureValue must be(Left("Failed saving cacheModel"))
    }
  }
}
