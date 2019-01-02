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

import play.api.http.Status.ACCEPTED
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.ws._

import scala.concurrent.{ExecutionContext, Future}

class MockInventoryHttpClient[A](
  expectedUrl: String,
  expectedBody: A,
  expectedHeaders: Seq[(String, String)],
  forceServerError: Boolean = false
) extends HttpClient with WSGet with WSPut with WSPost with WSDelete with WSPatch {

  override val hooks: Seq[HttpHook] = Seq.empty

  //scalastyle:off method.name
  override def POSTString[O](
    url: String,
    body: String,
    headers: Seq[(String, String)]
  )(implicit rds: HttpReads[O], hc: HeaderCarrier, ec: ExecutionContext): Future[O] =
    (url, body, headers) match {
      case _ if !isAuthenticated(Map.empty, hc) =>
        throw new UnauthorizedException("Arrival request was not authenticated")
      case _ if forceServerError => throw new InternalServerException("Customs Inventory Linking Exports has gone bad.")
      case _ if url == expectedUrl && headers == expectedHeaders =>
        Future.successful(HttpResponse(ACCEPTED).asInstanceOf[O])
      case error => throw new BadRequestException(error.toString)
    }

  //scalastyle:on method.name

  private def isAuthenticated(headers: Map[String, String], hc: HeaderCarrier): Boolean = hc.authorization.isDefined
}
