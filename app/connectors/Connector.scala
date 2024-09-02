/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

trait Connector {

  protected def httpClient: HttpClientV2

  protected class ConnectorUrlException(val method: String, val url: String, val message: String) extends Exception {
    override def getMessage: String = s"Invalid $method($url). $message"
  }

  def delete[R](
    url: String,
    additionalHeaders: Seq[(String, String)] = List.empty
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[R]): Future[R] =
    op(httpClient.delete(url"$url"), additionalHeaders)

  def get[R](url: String)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[R]): Future[R] =
    op(httpClient.get(url"$url"), List.empty)

  def get[R](
    url: String,
    queryParams: Seq[(String, String)],
    additionalHeaders: Seq[(String, String)] = List.empty
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[R]): Future[R] =
    if (url.contains("?")) throw new ConnectorUrlException("GET", url, "url should not contain any '?'")
    else if (queryParams.nonEmpty) op(httpClient.get(url"${urlWithQuery(url, queryParams)}"), additionalHeaders)
    else op(httpClient.get(url"$url"), additionalHeaders)

  def post[T, R](
    url: String,
    body: T,
    additionalHeaders: Seq[(String, String)] = List.empty
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[R], writes: Writes[T]): Future[R] =
    transform(httpClient.post(url"$url"), additionalHeaders).withBody(Json.toJson(body)).execute[R]

  def postWithoutBody[R](
    url: String,
    additionalHeaders: Seq[(String, String)] = List.empty
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[R]): Future[R] =
    transform(httpClient.post(url"$url"), additionalHeaders).execute[R]

  def put[T, R](
    url: String,
    body: T,
    additionalHeaders: Seq[(String, String)] = List.empty
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[R], writes: Writes[T]): Future[R] =
    transform(httpClient.put(url"$url"), additionalHeaders).withBody(Json.toJson(body)).execute[R]

  private def op[R](rb: RequestBuilder, additionalHeaders: Seq[(String, String)])(implicit ec: ExecutionContext, reads: HttpReads[R]): Future[R] =
    transform(rb, additionalHeaders).execute[R]

  private def urlWithQuery(url: String, queryParams: Seq[(String, String)]): String =
    s"""$url${queryParams.map { case (k, v) => s"$k=$v" }.mkString("?", "&", "")}"""

  private def transform(rb: RequestBuilder, additionalHeaders: Seq[(String, String)]): RequestBuilder =
    rb.transform(_.addHttpHeaders(additionalHeaders: _*))
}