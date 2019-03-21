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

package base
import akka.actor.ActorSystem
import com.typesafe.config.Config
import play.api.libs.json.Writes
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.ws._

import scala.concurrent.{ExecutionContext, Future}

class MockHttpClient[A, B](
  expectedUrl: String,
  expectedBody: A,
  expectedHeaders: Seq[(String, String)] = Seq.empty,
  forceServerError: Boolean = false,
  result: B
) extends HttpClient with WSGet with WSPut with WSPost with WSDelete with WSPatch {

  override val hooks: Seq[HttpHook] = Seq.empty
  override val actorSystem = ActorSystem("HttpClient")
  override val configuration: Option[Config] = None

  // scalastyle:off method.name
  override def GET[O](url: String)(implicit rds: HttpReads[O], hc: HeaderCarrier, ec: ExecutionContext): Future[O] =
    Future.successful(result.asInstanceOf[O])

  override def POSTString[O](
    url: String,
    body: String,
    headers: Seq[(String, String)] = Seq.empty
  )(implicit rds: HttpReads[O], hc: HeaderCarrier, ec: ExecutionContext): Future[O] =
    (url, body, headers) match {
      case _ if !isAuthenticated(hc) =>
        throw new UnauthorizedException("Request is not authenticated")
      case _ if forceServerError => throw new InternalServerException("Internal service problem")
      case _ if url == expectedUrl && headers == expectedHeaders =>
        Future.successful(result.asInstanceOf[O])
      case error => throw new BadRequestException(error.toString)
    }

  override def POST[I, O](
    url: String,
    body: I,
    headers: Seq[(String, String)] = Seq.empty
  )(implicit wts: Writes[I], rds: HttpReads[O], hc: HeaderCarrier, ec: ExecutionContext): Future[O] =
    (url, body, headers) match {
      case _ if !isAuthenticated(hc) =>
        throw new UnauthorizedException("Request is not authenticated")
      case _ if forceServerError =>
        throw new InternalServerException("Internal service problem")
      case _ if url == expectedUrl && body.asInstanceOf[A] == expectedBody && headers == expectedHeaders =>
        Future.successful(result.asInstanceOf[O])
      case _ =>
        throw new BadRequestException(
          s"Expected: \nurl = '$expectedUrl', \nbody = '$expectedBody'.\nGot: \nurl = '$url', \nbody = '$body'."
        )
    }
  //scalastyle:on method.name

  private def isAuthenticated(hc: HeaderCarrier): Boolean = hc.authorization.isDefined
}
