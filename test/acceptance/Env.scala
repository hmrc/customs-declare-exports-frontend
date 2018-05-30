/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package acceptance

import acceptance.TestEnvironment._
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}

import scala.util.Try

// In QA oid will be None as we initially didn't run end to end tests in QA (now we run some of them as smoke tests)
case class GGCredentials(userId: String, password: String, oid: Option[String])

object Env {

  private val defaultTestEnvironment = LOCAL

  private lazy val currentEnvironment = Option(System.getProperty("environment")) map withNameEither getOrElse Right(defaultTestEnvironment) match {
    case Left(message) => throw new IllegalArgumentException(message)
    case Right(value) => value
  }

  def forCurrentEnv[T](func: TestEnvironment => T): T = func(currentEnvironment)

  lazy val governmentGatewayHost: String = forCurrentEnv {
    case QA => "https://www.qa.tax.service.gov.uk"
    case DEV => "https://www.development.tax.service.gov.uk"
    case LOCAL => "http://localhost:9025"
  }

  lazy val port: Int =  forCurrentEnv {
    case QA | DEV => 80
    case LOCAL => Option(System.getProperty("port")).fold(9000)(_.toInt)
  }

  lazy val frontendHost: String = forCurrentEnv {
    case QA => "http://customs-rosm-frontend.public.mdtp"
    case DEV => "https://www.development.tax.service.gov.uk"
    case LOCAL => Option(System.getProperty("host")).getOrElse("http://localhost:" + port)
  }

  lazy val businessCustomerFrontendHost: String = forCurrentEnv {
    case QA | DEV => frontendHost
    case LOCAL => "http://localhost:9923"
  }

  lazy val driver: WebDriver = {
    val options = new ChromeOptions()
    options.addArguments("start-maximized")
    val driver = new ChromeDriver(options)
    sys addShutdownHook {
      Try(driver.quit())
    }
    driver
  }

  lazy val ggCredentialsMatch: GGCredentials = forCurrentEnv {
    case QA => GGCredentials("223974975153","p2ssword", None)
    case DEV | LOCAL => GGCredentials("543212300783", "testing123", oid = Some("576beda80f00005100ccd205"))
  }
  lazy val ggCredentialsMatchNotIncorporated: GGCredentials = forCurrentEnv {
    case QA => ??? // TODO: find a not incorporated user on QA
    case DEV | LOCAL => GGCredentials("543212311772", "testing123", oid = Some("578c97830f00000f0019eaa6"))
  }
  lazy val ggCredentialsNotMatch: GGCredentials = forCurrentEnv {
    case QA => GGCredentials("778219207874","p2ssword", None)
    case DEV | LOCAL => GGCredentials("2345235235", "testing123", oid = Some("5756a2de0f00000f00e2de5b"))
  }
  lazy val ggCredentialsNotRegisteredMatch: GGCredentials = forCurrentEnv {
    case QA => GGCredentials("749689447494","testing123", None)
    case DEV | LOCAL => GGCredentials("543212311004", "testing123", oid = Some("582b03ab4200003801164852"))
  }
}
