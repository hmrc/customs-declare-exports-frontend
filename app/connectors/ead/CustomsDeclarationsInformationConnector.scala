/*
 * Copyright 2020 HM Revenue & Customs
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

package connectors.ead

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.dis.parsers.MrnStatusParser
import models.dis.{MrnStatus, XmlTags}
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDeclarationsInformationConnector @Inject()(mrnStatusParser: MrnStatusParser, appConfig: AppConfig, httpClient: HttpClient) {
  private val logger = Logger(this.getClass)

  def fetchMrnStatus(mrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MrnStatus] =
    httpClient
      .doGet(url = s"${appConfig.customsDeclarationsInformation}${appConfig.fetchMrnStatus.replace(XmlTags.id, mrn)}", headers = headers())
      .map { response =>
        logger.debug(s"CUSTOMS_DECLARATIONS_INFORMATION fetch MRN status response ${response.body}")
        mrnStatusParser.parse(xml.XML.loadString(response.body))
      }

  private def headers(): Seq[(String, String)] = Seq(
    "X-Client-ID" -> appConfig.cdiClientID,
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.${appConfig.cdiApiVersion}+xml",
    HeaderNames.AUTHORIZATION -> appConfig.cdiBearerToken,
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
    HeaderNames.CACHE_CONTROL -> "no-cache"
  )
}
