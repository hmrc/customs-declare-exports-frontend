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
import controllers.actions.{TestAuthAction, FakeAuthAction, AuthAction}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.concurrent.Execution.Implicits
import play.api.libs.ws.WSClient
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfig, CSRFFilter, CSRFConfigProvider}
import uk.gov.hmrc.http.SessionKeys

import scala.concurrent.ExecutionContext

trait SpecBase extends PlaySpec with GuiceOneAppPerSuite {
  protected val contextPath: String = "/customs-declare-exports"

  override lazy val app: Application = GuiceApplicationBuilder().build()

  implicit val mat: Materializer = app.materializer
  implicit val ec: ExecutionContext = Implicits.defaultContext
  def injector: Injector = app.injector

  def appConfig: AppConfig = injector.instanceOf[AppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def authenticate: AuthAction = injector.instanceOf[TestAuthAction]

  def cfg:CSRFConfig = injector.instanceOf[CSRFConfigProvider].get

  def token = injector.instanceOf[CSRFFilter].tokenProvider.generateToken

  def wsClient: WSClient = injector.instanceOf[WSClient]

  def fakeRequest = FakeRequest("", "")

  def messages: Messages = messagesApi.preferred(fakeRequest)

  protected def uriWithContextPath(path: String): String = s"$contextPath$path"

  protected def getRequest(uri: String, headers: Map[String, String] = Map.empty):
  FakeRequest[AnyContentAsEmpty.type] = {
    val session: Map[String, String] = Map(
      SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
      SessionKeys.userId -> FakeAuthAction.defaultUser.internalId.get
    )
    val tags = Map(
      Token.NameRequestTag -> cfg.tokenName,
      Token.RequestTag -> token
    )
    FakeRequest("GET", uri).
      withHeaders((Map(cfg.headerName -> token) ++ headers).toSeq: _*).
      withSession(session.toSeq: _*).copyFakeRequest(tags = tags)
  }


}
