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

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, FunSuite, MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

class ExportsCacheModelRepositorySpec
    extends WordSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with ScalaFutures with MustMatchers
    with OptionValues {

  val sessionId = "12345"
  override lazy val app: Application = GuiceApplicationBuilder().build()
  private val repo = app.injector.instanceOf[ExportsCacheModelRepository]

  implicit val ec: ExecutionContext = global

  override def beforeEach(): Unit = {
    super.beforeEach()
    repo.removeAll().futureValue
  }

  override def afterEach(): Unit = {
    super.afterEach()
    repo.removeAll().futureValue
  }

  "ExportsCacheModelRepository" when {

    "on update" should {
      "return model " in {
        val model = createModel(sessionId)
        val result = repo.upsert(sessionId, model).futureValue
        result must be(Some(model))
      }

    }

    "on get" should {
      "return Right with model when exists" in {
        val model = createModel(sessionId)
        repo.upsert(sessionId, model).futureValue
        val result = repo.get(sessionId).futureValue
        result must be(Right(model))
      }

      "return left when cache entry does not exist" in {
        val result = repo.get("UNKNOWNID").futureValue
        result must be(Left("Unable to find model with sessionID: UNKNOWNID"))
      }
    }
  }

  def createModel(existingSessionId: String): ExportsCacheModel =
    ExportsCacheModel(
      sessionId = existingSessionId,
      draftId = "",
      createdDateTime = LocalDateTime.now(),
      updatedDateTime = LocalDateTime.now(),
      choice = "SMP"
    )
}
