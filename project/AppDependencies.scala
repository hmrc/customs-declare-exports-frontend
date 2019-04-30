import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % "0.2.0",
    "uk.gov.hmrc" %% "play-reactivemongo" % "6.7.0",
    "uk.gov.hmrc" %% "logback-json-logger" % "4.6.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.35.0-play-25",
    "uk.gov.hmrc" %% "play-health" % "3.14.0-play-25",
    "uk.gov.hmrc" %% "play-ui" % "7.39.0-play-25",
    "uk.gov.hmrc" %% "http-caching-client" % "8.3.0",
    "uk.gov.hmrc" %% "bootstrap-play-25" % "4.11.0",
    "uk.gov.hmrc" %% "play-language" % "3.4.0",
    "com.thoughtworks.xstream" % "xstream" % "1.4.11.1",
    "uk.gov.hmrc" %% "wco-dec" % "0.30.0",
    "ai.x" %% "play-json-extensions" % "0.9.0"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.8.0-play-25" % "test",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % "test",
    "org.jsoup" % "jsoup" % "1.11.3" % "test",
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
    "org.mockito" % "mockito-core" % "2.27.0" % "test",
    "org.scalacheck" %% "scalacheck" % "1.14.0" % "test"
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
