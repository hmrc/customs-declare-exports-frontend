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

package base

import java.util.UUID

import models.{CustomsDeclarationsResponse, CustomsDeclareExportsResponse}
import play.api.libs.json.Writes
import play.api.test.Helpers.{ACCEPTED, OK}
import test.XmlBehaviours
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.ws._

import scala.concurrent.{ExecutionContext, Future}

class MockHttpClient[A](expectedUrl: String, expectedBody: A, expectedHeaders: Map[String, String],
                     forceServerError: Boolean = false, conversationId: String = UUID.randomUUID().toString)
  extends HttpClient with WSGet with WSPut with WSPost with WSDelete with WSPatch {
  override val hooks: Seq[HttpHook] = Seq.empty

  //scalastyle:off method.name
  override def POSTString[O](url: String, body: String,headers: Seq[(String, String)])
                            (implicit rds: HttpReads[O],
                             hc: HeaderCarrier,
                             ec: ExecutionContext): Future[O] = (url, body, headers) match {
    case _ if !XmlBehaviours.isValidImportDeclarationXml(body) =>
      throw new BadRequestException(s"Expected: valid XML: $expectedBody. \nGot: invalid XML: $body")
    case _ if !isAuthenticated(headers.toMap, hc) =>
      throw new UnauthorizedException("Submission declaration request was not authenticated")
    case _ if forceServerError => throw new InternalServerException("Customs Declarations has gone bad.")
    case _ if url == expectedUrl && body == expectedBody && headers.toMap == expectedHeaders =>
      Future.successful(CustomsDeclarationsResponse(ACCEPTED, Some(conversationId)).asInstanceOf[O])
    case _ =>
      throw new BadRequestException(s"Expected: \nurl = '$expectedUrl', \nbody = '$expectedBody', \nheaders = '$expectedHeaders'.\nGot: \nurl = '$url', \nbody = '$body', \nheaders = '$headers'.")
  }

  override def POST[I, O](url: String, body: I, headers: Seq[(String, String)])
                         (implicit wts: Writes[I], rds: HttpReads[O], hc: HeaderCarrier, ec: ExecutionContext): Future[O] =
    (url, body, headers) match {
      case _ if !isAuthenticated(Map.empty, hc) =>
        throw new UnauthorizedException("Submission request was not authenticated")
      case _ if url == expectedUrl && body == expectedBody && headers == Seq.empty =>
        Future.successful(CustomsDeclareExportsResponse(OK,"success").asInstanceOf[O])
      case _ =>
        throw new BadRequestException(s"Expected: \nurl = '$expectedUrl', \nbody = '$expectedBody'.\nGot: \nurl = '$url', \nbody = '$body'.")
    }
  //scalastyle:on method.name

  private def isAuthenticated(headers: Map[String, String], hc: HeaderCarrier): Boolean = hc.authorization.isDefined
}