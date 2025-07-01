import sbt.*

object Dependencies {

  val bootstrapPlayVersion = "9.13.0"
  val frontendPlayVersion = "12.6.0"

  val compile: Seq[ModuleID] = List(
    "uk.gov.hmrc"                    %% "bootstrap-frontend-play-30"             % bootstrapPlayVersion,
    "uk.gov.hmrc"                    %% "play-frontend-hmrc-play-30"             % frontendPlayVersion,
    "uk.gov.hmrc"                    %% "play-conditional-form-mapping-play-30"  % "3.3.0",
    "com.fasterxml.jackson.module"   %% "jackson-module-scala"                   % "2.18.1",
    "net.sf.barcode4j"               %  "barcode4j"                              % "2.1",
    "org.webjars.npm"                %  "accessible-autocomplete"                % "3.0.0",
    "commons-codec"                  %  "commons-codec"                          % "1.17.1"
  )

  val test: Seq[ModuleID] = List(
    "uk.gov.hmrc"                    %% "bootstrap-test-play-30"  % bootstrapPlayVersion % "test",
    "com.vladsch.flexmark"           %  "flexmark-all"            % "0.64.8"   % "test",
    "org.jsoup"                      %  "jsoup"                   % "1.18.2"   % "test",
    "org.scalatestplus"              %% "mockito-3-4"             % "3.2.10.0" % "test",
  )

  private val missingSources = List("accessible-autocomplete", "flexmark-all")

  def apply(): Seq[ModuleID] =
    (compile ++ test).map(moduleId => if (missingSources.contains(moduleId.name)) moduleId else moduleId.withSources)
}
