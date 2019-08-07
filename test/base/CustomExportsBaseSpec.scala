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
import models.{ExportsCacheModel, NrsSubmissionResponse}
import org.joda.time.DateTime
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{never, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.test.FakeRequest
import play.filters.csrf.{CSRFConfig, CSRFConfigProvider, CSRFFilter}
import services._
import services.cache.{ExportItemIdGeneratorService, ExportsCacheModelBuilder, ExportsCacheService}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import utils.FakeRequestCSRFSupport._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

trait CustomExportsBaseSpec
    extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures with MockAuthAction
    with MockConnectors with BeforeAndAfterEach with ExportsCacheModelBuilder {

  protected val contextPath: String = "/customs-declare-exports"

  val mockExportsCacheService: ExportsCacheService = mock[ExportsCacheService]
  val mockSubmissionService: SubmissionService = mock[SubmissionService]
  val mockItemGeneratorService: ExportItemIdGeneratorService = mock[ExportItemIdGeneratorService]
  val mockNrsService: NRSService = mock[NRSService]

  implicit val hc: HeaderCarrier =
    HeaderCarrier(
      authorization = Some(Authorization(TestHelper.createRandomString(255))),
      nsStamp = DateTime.now().getMillis
    )

  SharedMetricRegistries.clear()

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(
      bind[AuthConnector].to(mockAuthConnector),
      bind[ExportsCacheService].to(mockExportsCacheService),
      bind[ExportItemIdGeneratorService].to(mockItemGeneratorService),
      bind[CustomsDeclareExportsConnector].to(mockCustomsDeclareExportsConnector),
      bind[NrsConnector].to(mockNrsConnector),
      bind[SubmissionService].to(mockSubmissionService),
      bind[NRSService].to(mockNrsService)
    )
    .build()

  implicit val mat: Materializer = app.materializer

  implicit val ec: ExecutionContext = global

  def injector: Injector = app.injector

  def appConfig: AppConfig = injector.instanceOf[AppConfig]

  def mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

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

  val mockWSClient = mock[WSClient]

  protected def uriWithContextPath(path: String): String = s"$contextPath$path"

  protected def getRequest(uri: String, headers: Map[String, String] = Map.empty): Request[AnyContentAsEmpty.type] = {
    val session: Map[String, String] = Map(
      SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
      SessionKeys.userId -> FakeAuthAction.defaultUser.identityData.internalId.get
    )

    FakeRequest("GET", uri)
      .withHeaders((Map(cfg.headerName -> token) ++ headers).toSeq: _*)
      .withSession(session.toSeq: _*)
      .withCSRFToken
  }

  protected def postRequest(
    uri: String,
    body: JsValue,
    headers: Map[String, String] = Map.empty
  ): Request[AnyContentAsJson] = {
    val session: Map[String, String] = Map(
      SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
      SessionKeys.userId -> FakeAuthAction.defaultUser.identityData.internalId.get
    )

    FakeRequest("POST", uri)
      .withHeaders((Map(cfg.headerName -> token) ++ headers).toSeq: _*)
      .withSession(session.toSeq: _*)
      .withJsonBody(body)
      .withCSRFToken
  }

  protected def postRequestFormUrlEncoded(uri: String, body: (String, String)*): Request[AnyContentAsFormUrlEncoded] = {
    val session: Map[String, String] = Map(
      SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
      SessionKeys.userId -> FakeAuthAction.defaultUser.identityData.internalId.get
    )

    FakeRequest("POST", uri)
      .withHeaders(Map(cfg.headerName -> token).toSeq: _*)
      .withSession(session.toSeq: _*)
      .withFormUrlEncodedBody(body: _*)
      .withCSRFToken
  }

  def withNewCaching(dataToReturn: ExportsCacheModel) {
    when(mockExportsCacheService.getItemByIdAndSession(any[String], any[String]))
      .thenReturn(Future.successful(dataToReturn.items.headOption))

    when(
      mockExportsCacheService
        .update(any[String], any[ExportsCacheModel])
    ).thenReturn(Future.successful(Some(dataToReturn)))

    when(
      mockExportsCacheService
        .get(any[String])
    ).thenReturn(Future.successful(Some(dataToReturn)))
  }

  def withNewCaching() {
    when(mockExportsCacheService.getItemByIdAndSession(any[String], any[String]))
      .thenReturn(Future.successful(None))

    when(
      mockExportsCacheService
        .update(any[String], any[ExportsCacheModel])
    ).thenReturn(Future.successful(None))

    when(
      mockExportsCacheService
        .get(any[String])
    ).thenReturn(Future.successful(None))
  }

  def withNrsSubmission(): OngoingStubbing[Future[NrsSubmissionResponse]] =
    when(mockNrsService.submit(any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(NrsSubmissionResponse("submissionid1")))

  protected def theCacheModelUpdated: ExportsCacheModel = {
    val captor = ArgumentCaptor.forClass(classOf[ExportsCacheModel])
    verify(mockExportsCacheService).update(anyString, captor.capture())
    captor.getValue
  }

  protected def verifyTheCacheIsUnchanged(): Unit =
    verify(mockExportsCacheService, never()).update(anyString, any[ExportsCacheModel])

}

object CSRFUtil {
  implicit class CSRFReplacer(str: String) {
    def replaceCSRF(): String = str.replaceAll("name=\"csrfToken\" value=\".*\"/>", "csrfToken1")
  }
}
