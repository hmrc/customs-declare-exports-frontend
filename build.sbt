import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.uglify.Import._
import com.typesafe.sbt.web.Import._
import net.ground5hark.sbt.concat.Import._
import sbt.Keys.{scalacOptions, _}
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.SbtAutoBuildPlugin

val appName = "customs-declare-exports-frontend"

PlayKeys.devSettings := Seq("play.server.http.port" -> "6791")

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin, SbtWeb)
  .settings(commonSettings: _*)
  .settings(
    retrieveManaged := true,
    majorVersion := 0,
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(integrationTestSettings())
  .settings(
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    Assets / pipelineStages := Seq(concat, uglify),
    // only compress files generated by concat
    uglify / includeFilter := GlobFilter("customsdecexfrontend-*.js")
  )
  .settings(scoverageSettings)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := List(
    "<empty>",
    "Reverse.*",
    "metrics\\..*",
    "features\\..*",
    "test\\..*",
    ".*(BuildInfo|Routes|Options|TestingUtilitiesController).*",
    "logger.*\\(.*\\)"
  ).mkString(";"),
  coverageMinimumStmtTotal := 86,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  Test / parallelExecution := false
)

lazy val commonSettings = Seq(
  scalaVersion := "2.13.8",
  scalacOptions ++= scalacFlags,
  libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test
)

lazy val scalacFlags = Seq(
  "-deprecation",            // warn about use of deprecated APIs
  "-encoding", "UTF-8",      // source files are in UTF-8
  "-feature",                // warn about misused language features
  "-unchecked",              // warn about unchecked type parameters
  "-Xfatal-warnings",        // warnings are fatal!!
  "-Wunused:-nowarn",        // enable @no-warn annotation
  "-Wconf:src=target/.*:s",  // silence warnings from compiled files
  "-Wconf:msg=match may not be exhaustive:s", // silence warnings about non-exhaustive pattern matching
  "-Wconf:src=test/.*&msg=a type was inferred to be `Object`:s", // silence warnings from mockito reset
)
