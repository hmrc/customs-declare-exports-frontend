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

package api.declaration

import config.FrontendAppConfig
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.Injector
import play.api.mvc._
import play.api.routing.sird._
import play.api.test._
import play.api.test.Helpers.{ACCEPTED, BAD_REQUEST}
import play.core.server.Server

class SubmitDeclarationSpec extends WordSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneAppPerSuite {
  val injector: Injector = app.injector

  val frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  "Submit declaration" should {
    "return HTTP Status 400 (Bad request) for missing declarant" in {
      Server.withRouter() {
        case POST(p"/") => Action { implicit req =>
          req.body.asXml match {
            case Some(n) if (n \\ "Declarant" \ "ID").text == "testDecId" => Results.Accepted
            case _ => Results.BadRequest
          }
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val submitter = new SubmitDeclaration(frontendAppConfig, client, "")
          whenReady(submitter.submit(Declaration(Declarant("")), "Non CSP")) {
            _ shouldEqual BAD_REQUEST
          }
        }
      }
    }

    "return HTTP Status 202 (Accepted) for valid declaration" in {
      Server.withRouter() {
        case POST(p"/") => Action { implicit req =>
          req.body.asXml match {
            case Some(n) if (n \\ "Declarant" \ "ID").text == "testDecId" => Results.Accepted
            case _ => Results.BadRequest
          }
        }
      } { implicit port =>
        WsTestClient.withClient { client =>
          val submitter = new SubmitDeclaration(frontendAppConfig, client, "")
          whenReady(submitter.submit(Declaration(Declarant("testDecId")), "Non CSP")) {
            _ shouldEqual ACCEPTED
          }
        }
      }
    }
  }
}
