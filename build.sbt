import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "customs-declare-exports-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.12"

PlayKeys.devSettings := List("play.server.http.port" -> "6791")

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports/html-report")
Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin, SbtWeb)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(commonSettings)
  .settings(scoverageSettings)

lazy val commonSettings = List(
  scalacOptions ++= scalacFlags,
  retrieveManaged := true,
  libraryDependencies ++= Dependencies(),
  routesImport += "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl",
  TwirlKeys.templateImports ++= List.empty
)

lazy val scalacFlags = List(
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

// Prevent the "No processor claimed any of these annotations" warning
javacOptions ++= List("-Xlint:-processing")

lazy val scoverageSettings = List(
  coverageExcludedPackages := List(
    "<empty>",
    "Reverse.*",
    "metrics\\..*",
    "features\\..*",
    "test\\..*",
    ".*(BuildInfo|Routes|Options|TestingUtilitiesController).*",
    "logger.*\\(.*\\)"
  ).mkString(";"),
  coverageMinimumStmtTotal := 90,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  Test / parallelExecution := false
)

addCommandAlias("ucomp", "Test/compile")
addCommandAlias("precommit", ";clean;scalafmt;Test/scalafmt;coverage;test;scalafmtCheckAll;coverageReport")
