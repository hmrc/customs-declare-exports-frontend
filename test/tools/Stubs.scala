/*
 * Copyright 2023 HM Revenue & Customs
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

package tools

import scala.concurrent.ExecutionContext
import org.apache.pekko.stream.testkit.NoMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import config.featureFlags.{BetaBannerConfig, FeatureSwitchConfig}
import config.{AppConfig, AppConfigSpec}
import play.api.http.{DefaultFileMimeTypes, FileMimeTypes, FileMimeTypesConfiguration}
import play.api.i18n.{Lang, Langs, MessagesApi}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.govukfrontend.views.html.components.{Footer => _, _}
import uk.gov.hmrc.hmrcfrontend.config._
import uk.gov.hmrc.hmrcfrontend.views.config.HmrcFooterItems
import uk.gov.hmrc.hmrcfrontend.views.html.components._
import uk.gov.hmrc.hmrcfrontend.views.html.helpers._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import views.html.components.gds._
import views.html.components.viewDeclarationSummaryLink

trait Stubs {

  def stubMessagesControllerComponents(
    bodyParser: BodyParser[AnyContent] = stubBodyParser(AnyContentAsEmpty),
    playBodyParsers: PlayBodyParsers = stubPlayBodyParsers(NoMaterializer),
    messagesApi: MessagesApi = stubMessagesApi(),
    langs: Langs = stubLangs(List(Lang("en"), Lang("cy"))),
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

  private val minimalConfig: Config =
    ConfigFactory.parseString(AppConfigSpec.configBareMinimum + """
      |assets.version="version"
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
      |urls.tradeTariff=tradeTariff
      |urls.classificationHelp=classificationHelp
      |urls.ecicsTool=ecicsTool
      |urls.notDeclarant.eoriContactTeam=eoriContactTeamUrl
      |urls.generalEnquiriesHelp=generalEnquiriesHelpUrl
      |play.i18n.langs = ["en", "cy"]
      |language.fallback.url=""
      |files.codelists.doc-type="/code-lists/document-type.json"
    """.stripMargin)

  private val minimalConfiguration = Configuration(minimalConfig)
  private val tudorCrownConfig = TudorCrownConfig(minimalConfiguration)

  private def govukHeader: GovukHeader = new GovukHeader(tudorCrownConfig)

  private val environment = Environment.simple()

  private val servicesConfig = new ServicesConfig(minimalConfiguration)
  private val appConfig = new AppConfig(minimalConfiguration, environment, servicesConfig, "AppName")
  private val timeoutDialogConfig = new config.TimeoutDialogConfig(servicesConfig)
  private val betaBannerConfig = new BetaBannerConfig(new FeatureSwitchConfig(minimalConfiguration))

  val minimalAppConfig = appConfig

  val gdsGovukLayout = new GovukLayout(
    new GovukTemplate(govukHeader = govukHeader, govukFooter = new GovukFooter(), new GovukSkipLink(), new FixedWidthPageLayout()),
    govukHeader,
    new GovukFooter(),
    new GovukBackLink(),
    new TwoThirdsMainContent(),
    new FixedWidthPageLayout()
  )

  val gdsGovukFlexibleLayout = new govukFlexibleLayout(
    new GovukTemplate(govukHeader = govukHeader, govukFooter = new GovukFooter(), new GovukSkipLink(), new FixedWidthPageLayout()),
    govukHeader,
    new GovukFooter(),
    new GovukBackLink()
  )

  val hmrcFooter = new HmrcStandardFooter(new HmrcFooter(), new HmrcFooterItems(new AccessibilityStatementConfig(minimalConfiguration)))

  val hmrcTrackingConsentSnippet = new HmrcTrackingConsentSnippet(new TrackingConsentConfig(minimalConfiguration))
  val hmrcReportTechnicalIssue = new HmrcReportTechnicalIssue()

  val viewDeclarationSummaryLink = new viewDeclarationSummaryLink(new link(), new paragraph())

  val pBanner = new phaseBanner(new GovukPhaseBanner(new GovukTag()), minimalAppConfig)

  val sHeader = new siteHeader(
    new HmrcHeader(new HmrcBanner(tudorCrownConfig), new HmrcUserResearchBanner(), new GovukPhaseBanner(new GovukTag()), tudorCrownConfig)
  )

  val hmrcTimeoutDialogHelper = new HmrcTimeoutDialogHelper(new HmrcTimeoutDialog, new TimeoutDialogConfig(minimalConfiguration))
  val hmrcLanguageSelectHelper = new HmrcLanguageSelectHelper(new HmrcLanguageSelect, new LanguageConfig(minimalConfiguration))

  val gdsMainTemplate = new gdsMainTemplate(
    govukHeader = govukHeader,
    govukLayout = gdsGovukLayout,
    govukFlexibleLayout = gdsGovukFlexibleLayout,
    govukBackLink = new GovukBackLink(),
    siteHeader = sHeader,
    phaseBanner = pBanner,
    timeoutDialogConfig = timeoutDialogConfig,
    betaBannerConfig = betaBannerConfig,
    hmrcHead = new HmrcHead(hmrcTrackingConsentSnippet, new AssetsConfig),
    hmrcLanguageSelectHelper = hmrcLanguageSelectHelper,
    hmrcTimeoutDialogHelper = hmrcTimeoutDialogHelper,
    hmrcTrackingConsentSnippet = hmrcTrackingConsentSnippet,
    hmrcReportTechnicalIssue = hmrcReportTechnicalIssue,
    hmrcFooter = hmrcFooter,
    viewDeclarationSummaryLink = viewDeclarationSummaryLink,
    appConfig = minimalAppConfig
  )
}
