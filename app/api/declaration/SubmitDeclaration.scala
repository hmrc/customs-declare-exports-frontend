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

import javax.inject.Inject
import play.api.{Environment, Play}
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class SubmitDeclaration (ws: WSClient, baseUrl: String) {
  @Inject def this(ws: WSClient, env: Environment) = this(ws, "https://customs-declarations.protected.mdtp")
  //@Inject def this(ws: WSClient, env: Environment) = this(ws, "http://localhost:9820")

  def submit(declaration: Declaration, bearerToken: String): Future[Int] = {
    ws.url(s"$baseUrl/")
      .withHeaders(
        "Accept" -> "application/vnd.hmrc.2.0+xml",
        "Content-Type" -> "application/xml; charset=UTF-8",
        "X-Client-ID" -> "d65f2252-9fcf-4f04-9445-5971021226bb",
        "Authorization" -> s"Bearer $bearerToken"
      )
      .post("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + declaration.toXml.toString())
      .map(_.status)
  }
}
