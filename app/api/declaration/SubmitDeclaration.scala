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

class SubmitDeclaration(ws: WSClient, baseUrl: String) {
  @Inject def this(ws: WSClient, env: Environment) = this(ws, "https://api.qa.tax.service.gov.uk")

  def submit(declaration: Declaration): Future[Int] = {
    ws.url(s"$baseUrl/customs/declarations/")
      .withHeaders("Content-Type" -> "application/xml")
      .post(declaration.toXml)
      .map(_.status)
  }
}
