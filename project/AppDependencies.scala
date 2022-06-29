import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-28"     % "5.9.0",
    "uk.gov.hmrc"                   %% "logback-json-logger"            % "5.1.0",
    "uk.gov.hmrc"                   %% "play-allowlist-filter"          % "1.0.0-play-28",
    "uk.gov.hmrc"                   %% "play-conditional-form-mapping"  % "1.9.0-play-28",
    "uk.gov.hmrc"                   %% "play-frontend-hmrc"             % "3.9.0-play-28",
    "ai.x"                          %% "play-json-extensions"           % "0.42.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"           % "2.12.5",
    "com.github.tototoshi"          %% "scala-csv"                      % "1.3.8",
    "com.dmanchester"               %% "playfop"                        % "1.0",
    "net.sf.barcode4j"              %  "barcode4j"                      % "2.1",
    "org.webjars.npm"               %  "accessible-autocomplete"        % "2.0.3"
  ).map(_.withSources)

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play"      %% "play-test"          % current   % "test",
    "org.scalatest"          %% "scalatest"          % "3.2.9"   % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"   % "test",
    "com.vladsch.flexmark"   %  "flexmark-all"       % "0.36.8"  % "test",
    "org.jsoup"              %  "jsoup"              % "1.13.1"  % "test",
    "org.scalatestplus"      %% "mockito-3-4"        % "3.2.9.0" % "test",
    "org.apache.pdfbox"      %  "pdfbox"             % "2.0.24"  % "test",
    "com.github.tomakehurst" %  "wiremock-jre8"      % "2.28.1"  % "test"
  ).map(moduleID => if (moduleID.name.contains("flexmark")) moduleID else moduleID.withSources)
}
