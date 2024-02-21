import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "8.3.0"
  val frontendPlayVersion = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                      %% "bootstrap-frontend-play-30"              % bootstrapPlayVersion,
    "uk.gov.hmrc"                      %% "play-frontend-hmrc-play-30"              % frontendPlayVersion,
    "uk.gov.hmrc"                      %% "play-partials-play-30"                   % "9.1.0",
    "uk.gov.hmrc"                      %% "play-allowlist-filter"                   % "1.2.0",
    "uk.gov.hmrc"                      %% "play-conditional-form-mapping-play-30"   % "2.0.0",
    "com.fasterxml.jackson.module"     %% "jackson-module-scala"                    % "2.14.2",
    "com.github.tototoshi"             %% "scala-csv"                               % "1.3.10",
    "net.sf.barcode4j"                 %  "barcode4j"                               % "2.1",
    "org.webjars.npm"                  %  "accessible-autocomplete"                 % "2.0.4",
    "commons-codec"                    %  "commons-codec"                           % "1.15",
    "javax.xml.bind"                   %  "jaxb-api"                                % "2.3.1"
  ).map(_.withSources)

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                      %% "bootstrap-test-play-30"       % bootstrapPlayVersion % "test",
    "com.vladsch.flexmark"             %  "flexmark-all"                 % "0.64.6"   % "test",
    "org.jsoup"                        %  "jsoup"                        % "1.15.4"   % "test",
    "org.scalatestplus"                %% "mockito-3-4"                  % "3.2.10.0" % "test",
  ).map(moduleID => if (moduleID.name.contains("flexmark")) moduleID else moduleID.withSources)

}
