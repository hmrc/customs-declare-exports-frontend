import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "govuk-template" % "5.20.0",
    "uk.gov.hmrc" %% "play-ui" % "7.15.0",
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25" % "1.5.0"
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.0.0" % scope,
    "org.scalatest" %% "scalatest" % "3.0.4" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.10.2" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" % "scalatestplus-play_2.11" % "2.0.1" % scope
  )

}
