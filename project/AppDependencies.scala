import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"          %% "play-conditional-form-mapping" % "1.2.0-play-26",
    "uk.gov.hmrc"          %% "logback-json-logger"           % "4.6.0",
    "uk.gov.hmrc"          %% "govuk-template"                % "5.54.0-play-26",
    "uk.gov.hmrc"          %% "play-health"                   % "3.15.0-play-26",
    "uk.gov.hmrc"          %% "play-ui"                       % "8.9.0-play-26",
    "uk.gov.hmrc"          %% "bootstrap-play-26"             % "1.7.0",
    "uk.gov.hmrc"          %% "play-frontend-govuk"           % "0.43.0-play-26",
    "uk.gov.hmrc"          %% "auth-client"                   % "3.0.0-play-27",
    "ai.x"                 %% "play-json-extensions"          % "0.40.2",
    "uk.gov.hmrc"          %% "play-whitelist-filter"         % "3.3.0-play-26",
    "com.typesafe.play"    %% "play-json-joda"                % "2.6.10",
    "com.github.tototoshi" %% "scala-csv"                     % "1.3.6",
    "com.dmanchester"      %% "playfop"                       % "1.0",
    "net.sf.barcode4j"     %  "barcode4j"                     % "2.1",
    "org.webjars.npm"      %  "govuk-frontend"                % "3.4.0",
    "org.webjars.npm"      %  "accessible-autocomplete"       % "2.0.2"
  )

  val test: Seq[ModuleID] = Seq(
    "com.github.tomakehurst" %  "wiremock-jre8"      % "2.24.1"            % "test",
    "org.scalatest"          %% "scalatest"          % "3.0.8"             % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2"             % "test",
    "org.pegdown"            %  "pegdown"            % "1.6.0"             % "test, it",
    "org.jsoup"              %  "jsoup"              % "1.12.1"            % "test",
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current % "test",
    "org.mockito"            %  "mockito-core"       % "3.0.0"             % "test",
    "org.apache.pdfbox"      %  "pdfbox"             % "2.0.19"            % "test"
  )
}
