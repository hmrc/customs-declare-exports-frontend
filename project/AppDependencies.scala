import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % "1.1.0-play-26",
    "uk.gov.hmrc" %% "simple-reactivemongo" % "7.20.0-play-26",
    "uk.gov.hmrc" %% "logback-json-logger" % "4.6.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.35.0-play-26",
    "uk.gov.hmrc" %% "play-health" % "3.14.0-play-26",
    "uk.gov.hmrc" %% "play-ui" % "7.39.0-play-26",
    "uk.gov.hmrc" %% "bootstrap-play-26" % "0.39.0",
    "uk.gov.hmrc" %% "wco-dec" % "0.30.0",
    "uk.gov.hmrc" %% "auth-client" % "2.22.0-play-26",
    "ai.x" %% "play-json-extensions" % "0.30.1",
    "uk.gov.hmrc" %% "play-whitelist-filter" % "3.1.0-play-26",
    "com.typesafe.play" %% "play-json-joda" % "2.6.10"
  )

  val test: Seq[ModuleID] = Seq(
    //TODO remove hmrctest dependency
    "uk.gov.hmrc" %% "hmrctest" % "3.8.0-play-26" % "test",
    "com.github.tomakehurst" % "wiremock" % "2.24.1" % "test",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % "test",
    "org.jsoup" % "jsoup" % "1.11.3" % "test",
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
    "org.mockito" % "mockito-core" % "2.27.0" % "test"
  )

  private lazy val jettyVersion = "9.2.26.v20180806"
  val overrides: Set[ModuleID] = Set(
    "org.eclipse.jetty" % "jetty-server" % jettyVersion % "test",
    "org.eclipse.jetty" % "jetty-servlet" % jettyVersion % "test",
    "org.eclipse.jetty" % "jetty-security" % jettyVersion % "test",
    "org.eclipse.jetty" % "jetty-servlets" % jettyVersion % "test",
    "org.eclipse.jetty" % "jetty-continuation" % jettyVersion % "test",
    "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "test",
    "org.eclipse.jetty" % "jetty-xml" % jettyVersion % "test",
    "org.eclipse.jetty" % "jetty-client" % jettyVersion % "test",
    "org.eclipse.jetty" % "jetty-http" % jettyVersion % "test",
    "org.eclipse.jetty" % "jetty-io" % jettyVersion % "test",
    "org.eclipse.jetty" % "jetty-util" % jettyVersion % "test",
    "org.eclipse.jetty.websocket" % "websocket-api" % jettyVersion % "test",
    "org.eclipse.jetty.websocket" % "websocket-common" % jettyVersion % "test",
    "org.eclipse.jetty.websocket" % "websocket-client" % jettyVersion % "test"
  )
}
