import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.uglify.Import._
import com.typesafe.sbt.web.Import._
import net.ground5hark.sbt.concat.Import._
import play.sbt.routes.RoutesKeys.routesImport
import sbt.Keys.{scalacOptions, _}
import sbt._

val appName = "customs-declare-exports-frontend"

PlayKeys.devSettings := Seq("play.server.http.port" -> "6791")

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports/html-report")
Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin, SbtWeb)
  .settings(commonSettings: _*)
  .settings(
    retrieveManaged := true,
    majorVersion := 0,
  )
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
  .settings(routesImport += "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl")
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
  scalaVersion := "2.13.12",
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
  "-Wconf:cat=unused&src=.*routes.*:s", // silence private val defaultPrefix in class Routes is never used
  "-Wconf:msg=eq not selected from this instance:s", // silence eq not selected from this instance warning
  "-Wconf:msg=While parsing annotations in:s" // silence While parsing annotations in warning
)
