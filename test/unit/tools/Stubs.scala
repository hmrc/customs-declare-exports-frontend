/*
 * Copyright 2020 HM Revenue & Customs
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

import com.typesafe.config.{Config, ConfigFactory}
import config.{AppConfig, BetaBannerConfig, FeatureSwitchConfig, TimeoutDialogConfig}
import play.api.http.{DefaultFileMimeTypes, FileMimeTypes, FileMimeTypesConfiguration}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.NoMaterializer
import play.api.{Configuration, Environment}
import uk.gov.hmrc.govukfrontend.views.html.components
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukHeader, Footer => _, _}
import uk.gov.hmrc.hmrcfrontend.config.TrackingConsentConfig
import uk.gov.hmrc.hmrcfrontend.views.html.components.{HmrcBanner, HmrcHeader, HmrcReportTechnicalIssue}
import uk.gov.hmrc.hmrcfrontend.views.html.helpers.HmrcTrackingConsentSnippet
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import views.html.components.gds._

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

  private val minimalConfig: Config = ConfigFactory.parseString("""
      |assets.url="localhost"
      |assets.version="version"
      |google-analytics.token=N/A
      |google-analytics.host=localhostGoogle
      |tracking-consent-frontend.gtm.container=a
      |metrics.name=""
      |metrics.rateUnit="SECONDS"
      |metrics.durationUnit="SECONDS"
      |metrics.showSamples=false
      |metrics.jvm=false
      |metrics.logback=false
      |draft.timeToLive=1d
      |timeoutDialog.timeout=13min
      |timeoutDialog.countdown=3min
      |list-of-available-journeys = "SMP,STD,CAN,SUB,CON"
      |microservice.services.features.default=disabled
      |microservice.services.features.use-improved-error-messages=true
      |urls.tradeTariff=tradeTariff
      |urls.classificationHelp=classificationHelp
      |urls.ecicsTool=ecicsTool
    """.stripMargin)

  val minimalConfiguration = Configuration(minimalConfig)

  private val environment = Environment.simple()

  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf)
  private def appConfig(conf: Configuration) = new AppConfig(conf, environment, servicesConfig(conf), "AppName")
  private def timeoutDialogConfig() = new TimeoutDialogConfig(servicesConfig(minimalConfiguration))
  private def betaBannerConfig() = new BetaBannerConfig(new FeatureSwitchConfig(minimalConfiguration))

  val minimalAppConfig = appConfig(minimalConfiguration)

  val gdsGovukLayout = new GovukLayout(
    new components.GovukTemplate(govukHeader = new GovukHeader(), govukFooter = new GovukFooter(), new GovukSkipLink()),
    new GovukHeader(),
    new GovukFooter(),
    new GovukBackLink()
  )

  val gdsGovukFlexibleLayout = new govukFlexibleLayout(
    new components.GovukTemplate(govukHeader = new GovukHeader(), govukFooter = new GovukFooter(), new GovukSkipLink()),
    new GovukHeader(),
    new GovukFooter(),
    new GovukBackLink()
  )

  val hmrcTrackingConsentSnippet = new HmrcTrackingConsentSnippet(new TrackingConsentConfig(Configuration(minimalConfig)))
  val hmrcReportTechnicalIssue = new HmrcReportTechnicalIssue()

  val govukHeader = new GovukHeader()
  val sHeader = new siteHeader(new HmrcHeader(new HmrcBanner()))
  val pBanner = new phaseBanner(new GovukPhaseBanner(new govukTag()), minimalAppConfig)
  val gdsMainTemplate = new gdsMainTemplate(
    govukHeader = govukHeader,
    govukLayout = gdsGovukLayout,
    govukFlexibleLayout = gdsGovukFlexibleLayout,
    govukBackLink = new components.GovukBackLink(),
    siteHeader = sHeader,
    phaseBanner = pBanner,
    timeoutDialogConfig = timeoutDialogConfig(),
    betaBannerConfig = betaBannerConfig(),
    hmrcTrackingConsentSnippet = hmrcTrackingConsentSnippet,
    hmrcReportTechnicalIssue = hmrcReportTechnicalIssue,
    appConfig = minimalAppConfig
  )
}
