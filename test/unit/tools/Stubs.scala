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

package unit.tools

import com.typesafe.config.ConfigFactory
import config.AppConfig
import play.api.Mode.Test
import play.api.{Configuration, Environment}
import play.api.http.{DefaultFileMimeTypes, FileMimeTypes, FileMimeTypesConfiguration}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.NoMaterializer
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}
import uk.gov.hmrc.play.config.{AssetsConfig, GTMConfig, OptimizelyConfig}
import uk.gov.hmrc.play.views.html.layouts._
import views.html.layouts.GovUkTemplate
import views.html.{govuk_wrapper, main_template}

import scala.concurrent.ExecutionContext

trait Stubs {

  def stubMessagesControllerComponents(
    bodyParser: BodyParser[AnyContent] = stubBodyParser(AnyContentAsEmpty),
    playBodyParsers: PlayBodyParsers = stubPlayBodyParsers(NoMaterializer),
    messagesApi: MessagesApi = stubMessagesApi(),
    langs: Langs = stubLangs(),
    fileMimeTypes: FileMimeTypes = new DefaultFileMimeTypes(FileMimeTypesConfiguration()),
    executionContext: ExecutionContext = ExecutionContext.global
  ): MessagesControllerComponents =
    DefaultMessagesControllerComponents(
      new DefaultMessagesActionBuilderImpl(bodyParser, messagesApi)(executionContext),
      DefaultActionBuilder(bodyParser)(executionContext),
      playBodyParsers,
      messagesApi,
      langs,
      fileMimeTypes,
      executionContext
    )

  private val minimalConfig = ConfigFactory.parseString("""
      |assets.url="localhost"
      |assets.version="version"
      |google-analytics.token=N/A
      |google-analytics.host=localhostGoogle
      |metrics.name=""
      |metrics.rateUnit="SECONDS"
      |metrics.durationUnit="SECONDS"
      |metrics.showSamples=false
      |metrics.jvm=false
      |metrics.logback=false
    """.stripMargin)

  val minimalConfiguration = Configuration(minimalConfig)

  private val environment = Environment.simple()

  private def runMode(conf: Configuration): RunMode = new RunMode(conf, Test)
  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf, runMode(conf))
  private def appConfig(conf: Configuration) = new AppConfig(conf, environment, servicesConfig(conf), "AppName")

  val minimalAppConfig = appConfig(minimalConfiguration)

  private val head: Head = new Head(
    new OptimizelySnippet(new OptimizelyConfig(minimalConfiguration)),
    new AssetsConfig(minimalConfiguration),
    new GTMSnippet(new GTMConfig(minimalConfiguration))
  )

  private val footer: Footer = new Footer(new AssetsConfig(minimalConfiguration))

  private val govukWrapper: govuk_wrapper = new govuk_wrapper(
    head,
    new HeaderNav(),
    footer,
    new ServiceInfo(),
    new MainContentHeader(),
    new MainContent(),
    new FooterLinks(),
    new GovUkTemplate(),
    minimalAppConfig
  )

  val mainTemplate: main_template = new main_template(govukWrapper, new Sidebar(), new Article())
}
