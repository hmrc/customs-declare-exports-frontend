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

package connectors

import com.google.inject.Inject
import config.AppConfig
import javax.inject.Singleton
import models.CustomsDeclarationsResponse
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.wco.dec.MetaData

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDeclarationsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) {

  def submitExportDeclaration(metaData: MetaData, badgeIdentifier: Option[String] = None)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] =
    postMetaData(appConfig.submitImportDeclarationUri, metaData, badgeIdentifier).map{ res =>
      Logger.debug(s"CUSTOMS_DECLARATIONS response is  --> ${res.toString} " )
      res
    }

  private def postMetaData(uri: String,
                           metaData: MetaData,
                           badgeIdentifier: Option[String] = None)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] =
    post(uri, metaData.toXml, badgeIdentifier)

  //noinspection ConvertExpressionToSAM
  private implicit val responseReader: HttpReads[CustomsDeclarationsResponse] =
    new HttpReads[CustomsDeclarationsResponse] {
      override def read(method: String, url: String, response: HttpResponse): CustomsDeclarationsResponse =
        CustomsDeclarationsResponse(response.status, response.header("X-Conversation-ID"))
    }

  private[connectors] def post(uri: String, body: String, badgeIdentifier: Option[String] = None)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclarationsResponse] = {
    val headers: Seq[(String, String)] = Seq(
      "X-Client-ID" -> appConfig.developerHubClientId,
      HeaderNames.ACCEPT -> s"application/vnd.hmrc.${appConfig.customsDeclarationsApiVersion}+xml",
      HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)
    ) ++ badgeIdentifier.map(id => "X-Badge-Identifier" -> id)
    Logger.debug(s"CUSTOMS_DECLARATIONS request payload is -> ${body}")

    httpClient.POSTString[CustomsDeclarationsResponse](
      s"${appConfig.customsDeclarationsEndpoint}$uri", body, headers
    )(responseReader, hc, ec)
  }
}