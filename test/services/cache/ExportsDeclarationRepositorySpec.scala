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

import java.time.Instant

import models.ExportsDeclaration
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import reactivemongo.bson.{BSONDocument, BSONLong}
import reactivemongo.play.json.ImplicitBSONHandlers.BSONDocumentWrites

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

class ExportsDeclarationRepositorySpec
    extends WordSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with ScalaFutures with MustMatchers
    with OptionValues {

  private implicit val ec: ExecutionContext = global
  private val instant = Instant.EPOCH
  private val sessionId = "12345"
  override lazy val app: Application = GuiceApplicationBuilder()
    .build()
  private val repo = app.injector.instanceOf[ExportsDeclarationRepository]

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  override def beforeEach(): Unit = {
    super.beforeEach()
    repo.removeAll().futureValue
  }

  override def afterEach(): Unit = {
    super.afterEach()
    repo.removeAll().futureValue
  }

  "ExportsCacheModelRepository" when {

    "upserting" should {
      "return Some with model " in {
        val model = createModel(sessionId)
        val result = repo.upsert(sessionId, model).futureValue
        result must be(Some(model))

      }

      "store dates as Mongo Date Types" in {
        val model = createModel(sessionId)
        repo.upsert(sessionId, model).futureValue

        val result: Option[Map[String, JsValue]] = repo.collection
          .find(BSONDocument("sessionId" -> sessionId))
          .one[Map[String, JsValue]]
          .futureValue

        result must be(defined)
        result.get("createdDateTime") mustBe Json.obj("$date" -> instant.toEpochMilli)
        result.get("updatedDateTime") mustBe Json.obj("$date" -> instant.toEpochMilli)
      }

    }

    "retrieving" should {
      "return Some with model when exists" in {
        val model = createModel(sessionId)
        repo.upsert(sessionId, model).futureValue
        val result = repo.get(sessionId).futureValue
        result must be(Some(model))
      }

      "return None when cache entry does not exist" in {
        val result = repo.get("UNKNOWNID").futureValue
        result must be(None)
      }
    }

    "removing" should {
      "remove Some with model when exists" in {
        val model = createModel(sessionId)
        repo.upsert(sessionId, model).futureValue
        val result = repo.get(sessionId).futureValue
        result must be(Some(model))

        repo.remove(sessionId)
        val result2 = repo.get(sessionId).futureValue
        result2 must be(None)
      }

      "remove nothing when does not exist" in {
        repo.remove(sessionId)
        val result2 = repo.get(sessionId).futureValue
        result2 must be(None)
      }
    }

    "list indexes" should {
      "return TTL" in {
        val index = repo.indexes.find(_.name.contains("ttl"))

        index must be(defined)
        index.get.name must be(Some("ttl"))
        index.get.key must have(size(1))
        index.get.key.head._1 must be("updatedDateTime")
        index.get.options.get("expireAfterSeconds") must be(Some(BSONLong(1800)))
      }
    }
  }

  def createModel(existingSessionId: String): ExportsDeclaration =
    models.ExportsDeclaration(
      sessionId = existingSessionId,
      draftId = "",
      createdDateTime = instant,
      updatedDateTime = instant,
      choice = "SMP"
    )
}
