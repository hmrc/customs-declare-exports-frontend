/*
 * Copyright 2019 HM Revenue & Customs
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

package connectors

import java.util.UUID

import base.TestHelper._
import base.{CustomExportsBaseSpec, MockHttpClient}
import models.CustomsDeclarationsResponse
import play.api.http.Status._
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.wco.dec.{Declaration, MetaData}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class CustomsDeclarationsConnectorSpec extends CustomExportsBaseSpec {

  val eori = Some(randomString(16))
  val lrn = Some(randomString(35))
  val conversationId: String = randomString(80)


  "CustomsDeclarationsConnector " should {

    "POST metadata to Customs Declarations" in submitDeclarationScenario(MetaData(declaration = Some(Declaration()))) { resp =>
      resp.futureValue.status must be(ACCEPTED)
    }
    "POST metadata to Customs Declarations cancellation" in submitDeclarationScenario(
      MetaData(declaration = Some(Declaration())),
      postURL = appConfig.submitCancellationUri) { resp =>
      resp.futureValue.status must be(ACCEPTED)
    }

    "update declaration status on acceptance of declaration" in submitDeclarationScenario(metaData =
      MetaData(declaration = Some(Declaration(
        functionalReferenceId = lrn
      ))), conversationId = conversationId) { resp =>
      Await.result(resp, 1.second)
    }

  }

  def submitDeclarationScenario(metaData: MetaData,
    badgeIdentifier: Option[String] = None,
    forceServerError: Boolean = false,
    hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(randomString(255)))),
    conversationId: String = UUID.randomUUID().toString,
    postURL: String = appConfig.submitExportDeclarationUri)
    (test: Future[CustomsDeclarationsResponse] => Unit): Unit = {
    val expectedUrl: String = s"${appConfig.customsDeclarationsEndpoint}${postURL}"
    val expectedBody: String = metaData.toXml
    val expectedHeaders: Map[String, String] = Map(
      "X-Client-ID" -> appConfig.developerHubClientId,
      HeaderNames.ACCEPT -> s"application/vnd.hmrc.${appConfig.customsDeclarationsApiVersion}+xml",
      HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
    ) ++ badgeIdentifier.map(id => "X-Badge-Identifier" -> id)
    val http = new MockHttpClient(expectedUrl, expectedBody, expectedHeaders, forceServerError, conversationId)
    val client = new CustomsDeclarationsConnector(appConfig, http)
    postURL.contains("cancellation") match {
      case true => test(client.submitCancellation(metaData, badgeIdentifier)(hc, ec))
      case _ => test(client.submitExportDeclaration(metaData, badgeIdentifier)(hc, ec))
    }
  }
}
