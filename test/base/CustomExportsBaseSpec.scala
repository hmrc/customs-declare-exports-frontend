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

package base

import java.util.UUID

import akka.stream.Materializer
import config.AppConfig
import connectors.{CustomsDeclarationsConnector, CustomsDeclareExportsConnector, CustomsInventoryLinkingExportsConnector}
import controllers.actions.FakeAuthAction
import forms.{ChoiceForm, MovementFormsAndIds}
import metrics.ExportsMetrics
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.concurrent.Execution.Implicits
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsJson}
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfig, CSRFConfigProvider, CSRFFilter}
import services.CustomsCacheService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.http.cache.client.CacheMap
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

import org.mockito.ArgumentMatchers

trait CustomExportsBaseSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with ScalaFutures
  with MockAuthAction
  with MockConnectors {

  protected val contextPath: String = "/customs-declare-exports"

  lazy val mockCustomsCacheService: CustomsCacheService = mock[CustomsCacheService]

  val mockMetrics : ExportsMetrics = mock[ExportsMetrics]

  override lazy val app: Application = GuiceApplicationBuilder().overrides(
    bind[AuthConnector].to(mockAuthConnector),
    bind[CustomsDeclarationsConnector].to(mockCustomsDeclarationsConnector),
    bind[CustomsCacheService].to(mockCustomsCacheService),
    bind[CustomsDeclareExportsConnector].to(mockCustomsDeclareExportsConnector),
    bind[CustomsInventoryLinkingExportsConnector].to(mockCustomsInventoryLinkingExportsConnector),
    bind[ExportsMetrics].to(mockMetrics)
  ).build()

  implicit val mat: Materializer = app.materializer

  implicit val ec: ExecutionContext = Implicits.defaultContext

  def injector: Injector = app.injector

  def appConfig: AppConfig = injector.instanceOf[AppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  val cfg: CSRFConfig = injector.instanceOf[CSRFConfigProvider].get

  val token = injector.instanceOf[CSRFFilter].tokenProvider.generateToken

  def fakeRequest = FakeRequest("", "")

  def messages: Messages = messagesApi.preferred(fakeRequest)
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  protected def uriWithContextPath(path: String): String = s"$contextPath$path"

  protected def getRequest(uri: String, headers: Map[String, String] = Map.empty): FakeRequest[AnyContentAsEmpty.type] = {
    val session: Map[String, String] = Map(
      SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
      SessionKeys.userId -> FakeAuthAction.defaultUser.internalId.get
    )
    val tags = Map(
      Token.NameRequestTag -> cfg.tokenName,
      Token.RequestTag -> token
    )
    FakeRequest("GET", uri)
      .withHeaders((Map(cfg.headerName -> token) ++ headers).toSeq: _*)
      .withSession(session.toSeq: _*).copyFakeRequest(tags = tags)
  }

  protected def postRequest(uri: String, body: JsValue, headers: Map[String, String] = Map.empty): FakeRequest[AnyContentAsJson] = {
    val session: Map[String, String] = Map(
      SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
      SessionKeys.userId -> FakeAuthAction.defaultUser.internalId.get
    )
    val tags = Map(
      Token.NameRequestTag -> cfg.tokenName,
      Token.RequestTag -> token
    )
    FakeRequest("POST", uri)
      .withHeaders((Map(cfg.headerName -> token) ++ headers).toSeq: _*)
      .withSession(session.toSeq: _*).copyFakeRequest(tags = tags)
      .withJsonBody(body)
  }

  protected def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  def withCaching[T](form: Option[Form[T]]) = {
    when(mockCustomsCacheService.fetchAndGetEntry[Form[T]](any(), any())(any(), any(),any()))
      .thenReturn(Future.successful(form))

    when(mockCustomsCacheService.cache[T](any(), any(),any())(any(), any(), any()))
      .thenReturn(Future.successful(CacheMap("id1", Map.empty)))
  }

  def withCaching[T](data: Option[T], id: String) =
    when(mockCustomsCacheService.fetchAndGetEntry[T](ArgumentMatchers.eq(appConfig.appName), ArgumentMatchers.eq(id))(any(), any(), any()))
      .thenReturn(Future.successful(data))

}
