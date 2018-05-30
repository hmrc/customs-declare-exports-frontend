/*
 * Copyright 2018 HM Revenue & Customs
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

package acceptance

import org.scalatest._
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerSuite, OneServerPerSuite}
import play.api.Application
import play.api.http.HttpVerbs
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.modules.reactivemongo.{MongoDbConnection, ReactiveMongoComponent}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

trait StubbedWordSpecWithOneServerPerSuite extends WordSpec with MustMatchers with BeforeAndAfterAll with BeforeAndAfterEach
   with OneBrowserPerSuite with HtmlUnitFactory with OneServerPerSuite with WireMockRunner {

  override lazy val port = Env.port

  protected def await[A](future: Future[A]): A = Await.result(future, 5.seconds)

  override implicit lazy val app: Application = new GuiceApplicationBuilder().configure(Map(
    "microservice.services.cds-health.host" -> ExternalServicesConfig.Host,
    "microservice.services.cds-health.port" -> ExternalServicesConfig.Port,
    "microservice.services.auth.port" -> ExternalServicesConfig.Port,
    "microservice.services.customs-hods-proxy.host" -> ExternalServicesConfig.Host,
    "microservice.services.customs-hods-proxy.port" -> ExternalServicesConfig.Port,
    "microservice.services.pdf-generator.host" -> ExternalServicesConfig.Host,
    "microservice.services.pdf-generator.port" -> ExternalServicesConfig.Port,
    "microservice.services.handle-subscription.host" -> ExternalServicesConfig.Host,
    "microservice.services.handle-subscription.port" -> ExternalServicesConfig.Port,
    "microservice.services.session-cache.host" -> "localhost",
    "microservice.services.session-cache.port" -> port,
    "microservice.services.session-cache.domain" -> ExternalServicesConfig.sessionCacheDomain,
    "cds-frontend-cache.ttl" -> "5minutes",
    "application.router" -> "prod.Routes",
    "play.filters.csrf.method.whiteList" -> List(HttpVerbs.GET, HttpVerbs.PUT, HttpVerbs.DELETE)
  )
  ).overrides(bind[ReactiveMongoComponent].to[ReactiveMongoComponentForTests])
    .build()

  override def beforeAll: Unit = {
    dropDb()
    startMockServer()
  }

  protected def dropDb(): Unit = {
    await(new MongoDbConnection(){}.db().drop())
  }

  override def beforeEach(): Unit = {
    webDriver manage() deleteAllCookies()
    resetMockServer()
  }

  override def afterAll: Unit = {
    stopMockServer()
  }
}
