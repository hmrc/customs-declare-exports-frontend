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

package base

import java.util.UUID

import akka.stream.Materializer
import com.codahale.metrics.SharedMetricRegistries
import config.AppConfig
import connectors.{CustomsDeclareExportsConnector, NrsConnector}
import controllers.actions.FakeAuthAction
import metrics.ExportsMetrics
import models.NrsSubmissionResponse
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfter
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
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, AnyContentAsJson}
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfig, CSRFConfigProvider, CSRFFilter}
import services._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.{ExecutionContext, Future}

trait CustomExportsBaseSpec
    extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures with MockAuthAction
    with MockConnectors with BeforeAndAfter {

  protected val contextPath: String = "/customs-declare-exports"

  val mockCustomsCacheService: CustomsCacheService = mock[CustomsCacheService]
  val mockNrsService: NRSService = mock[NRSService]
  val mockItemsCachingService: ItemsCachingService = mock[ItemsCachingService]

  SharedMetricRegistries.clear()

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(
      bind[AuthConnector].to(mockAuthConnector),
      bind[CustomsCacheService].to(mockCustomsCacheService),
      bind[CustomsDeclareExportsConnector].to(mockCustomsDeclareExportsConnector),
      bind[NrsConnector].to(mockNrsConnector),
      bind[NRSService].to(mockNrsService),
      bind[ItemsCachingService].to(mockItemsCachingService),
      bind[WcoMetadataMapper].to(new WcoMetadataMapper with WcoMetadataJavaMappingStrategy)
    )
    .build()

  implicit val mat: Materializer = app.materializer

  implicit val ec: ExecutionContext = Implicits.defaultContext

  def injector: Injector = app.injector

  def appConfig: AppConfig = injector.instanceOf[AppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  val metrics = app.injector.instanceOf[ExportsMetrics]

  val cfg: CSRFConfig = injector.instanceOf[CSRFConfigProvider].get

  val token: String = injector.instanceOf[CSRFFilter].tokenProvider.generateToken

  def fakeRequest = FakeRequest("", "")

  // MockAuthAction has this value as default value... we need to use it to make cache working in tests
  val eoriForCache = "12345"

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  protected def uriWithContextPath(path: String): String = s"$contextPath$path"

  protected def getRequest(
    uri: String,
    headers: Map[String, String] = Map.empty
  ): FakeRequest[AnyContentAsEmpty.type] = {
    val session: Map[String, String] = Map(
      SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
      SessionKeys.userId -> FakeAuthAction.defaultUser.identityData.internalId.get
    )
    val tags = Map(Token.NameRequestTag -> cfg.tokenName, Token.RequestTag -> token)
    FakeRequest("GET", uri)
      .withHeaders((Map(cfg.headerName -> token) ++ headers).toSeq: _*)
      .withSession(session.toSeq: _*)
      .copyFakeRequest(tags = tags)
  }

  protected def postRequest(
    uri: String,
    body: JsValue,
    headers: Map[String, String] = Map.empty
  ): FakeRequest[AnyContentAsJson] = {
    val session: Map[String, String] = Map(
      SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
      SessionKeys.userId -> FakeAuthAction.defaultUser.identityData.internalId.get
    )
    val tags = Map(Token.NameRequestTag -> cfg.tokenName, Token.RequestTag -> token)
    FakeRequest("POST", uri)
      .withHeaders((Map(cfg.headerName -> token) ++ headers).toSeq: _*)
      .withSession(session.toSeq: _*)
      .withJsonBody(body)
      .copyFakeRequest(tags = tags)
  }

  protected def postRequestFormUrlEncoded(
    uri: String,
    body: (String, String)*
  ): FakeRequest[AnyContentAsFormUrlEncoded] = {
    val session: Map[String, String] = Map(
      SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
      SessionKeys.userId -> FakeAuthAction.defaultUser.identityData.internalId.get
    )
    val tags = Map(Token.NameRequestTag -> cfg.tokenName, Token.RequestTag -> token)
    FakeRequest("POST", uri)
      .withHeaders(Map(cfg.headerName -> token).toSeq: _*)
      .withSession(session.toSeq: _*)
      .withFormUrlEncodedBody(body: _*)
      .copyFakeRequest(tags = tags)
  }

  def withCaching[T](form: Option[Form[T]]): OngoingStubbing[Future[CacheMap]] = {
    when(mockCustomsCacheService.fetchAndGetEntry[Form[T]](any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(form))

    when(mockCustomsCacheService.cache[T](any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(CacheMap("id1", Map.empty)))
  }

  def withCaching[T](dataToReturn: Option[T], id: String): OngoingStubbing[Future[CacheMap]] = {
    when(
      mockCustomsCacheService
        .fetchAndGetEntry[T](any(), ArgumentMatchers.eq(id))(any(), any(), any())
    ).thenReturn(Future.successful(dataToReturn))

    when(mockCustomsCacheService.cache[T](any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(CacheMap(id, Map.empty)))
  }

  def withNrsSubmission(): OngoingStubbing[Future[NrsSubmissionResponse]] =
    when(mockNrsService.submit(any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(NrsSubmissionResponse("submissionid1")))
}

object CSRFUtil {
  implicit class CSRFReplacer(str: String) {
    def replaceCSRF() = str.replaceAll("name=\"csrfToken\" value=\".*\"/>", "csrfToken1")
  }
}
