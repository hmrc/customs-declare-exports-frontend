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

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models._
import models.declaration.notifications.Notification
import models.requests.CancellationStatus
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.wco.dec.MetaData

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDeclareExportsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) {

  private val logger = Logger(this.getClass())

  private[connectors] def get(url: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    httpClient.GET(url, Seq())

  def submitExportDeclaration(ducr: Option[String], lrn: Option[String], payload: String)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    httpClient
      .POSTString[HttpResponse](
        s"${appConfig.customsDeclareExports}${appConfig.submitDeclaration}",
        payload,
        Seq(
          (HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)),
          (HeaderNames.ACCEPT -> ContentTypes.XML(Codec.utf_8)),
          ("X-DUCR", ducr.getOrElse("")),
          ("X-LRN", lrn.getOrElse(""))
        )
      )
      .map { response =>
        logger.debug(s"CUSTOMS_DECLARE_EXPORTS response is --> ${response.toString}")
        response
      }

  def fetchNotifications()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Notification]] =
    httpClient.GET[Seq[Notification]](s"${appConfig.customsDeclareExports}${appConfig.fetchNotifications}")

  def fetchNotificationsByMrn(
    mrn: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Notification]] =
    httpClient.GET[Seq[Notification]](
      s"${appConfig.customsDeclareExports}${appConfig.fetchSubmissionNotifications}/$mrn"
    )

  def fetchSubmissions()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Submission]] =
    httpClient.GET[Seq[Submission]](s"${appConfig.customsDeclareExports}${appConfig.fetchSubmissions}").map {
      response =>
        logger.debug(s"CUSTOMS_DECLARE_EXPORTS fetch submission response is --> ${response.toString}")
        response
    }

  def submitCancellation(
    mrn: String,
    metadata: MetaData
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CancellationStatus] =
    httpClient
      .POSTString[CancellationStatus](
        s"${appConfig.customsDeclareExports}${appConfig.cancelDeclaration}",
        metadata.toXml,
        Seq(
          (HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)),
          (HeaderNames.ACCEPT -> ContentTypes.XML(Codec.utf_8)),
          ("X-MRN", mrn)
        )
      )
      .map { response =>
        logger.debug(s"CUSTOMS_DECLARE_EXPORTS cancel declaration response is --> ${response.toString}")
        response
      }
}
