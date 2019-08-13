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
import connectors.exchange.ExportsDeclarationExchange
import javax.inject.{Inject, Singleton}
import models.ExportsDeclaration
import models.declaration.notifications.Notification
import models.declaration.submissions.Submission
import models.requests.CancellationStatus
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import services.WcoMetadataMapper
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import wco.datamodel.wco.documentmetadata_dms._2.MetaData

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDeclareExportsConnector @Inject()(
  appConfig: AppConfig,
  httpClient: HttpClient,
  wcoMetadataMapper: WcoMetadataMapper
) {

  private val logger = Logger(this.getClass)

  def create(
    declaration: ExportsDeclaration
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] =
    httpClient
      .POST[ExportsDeclarationExchange, ExportsDeclarationExchange](
        s"${appConfig.customsDeclareExports}${appConfig.submitDeclarationV2}",
        ExportsDeclarationExchange(declaration)
      )
      .map(_.toExportsDeclaration(declaration.sessionId))

  def update(
    declaration: ExportsDeclaration
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] =
    httpClient
      .PUT[ExportsDeclarationExchange, ExportsDeclarationExchange](
        s"${appConfig.customsDeclareExports}${appConfig.submitDeclarationV2}/${declaration.id
          .getOrElse(throw new IllegalArgumentException("Cannot update a declaration which hasnt been created first"))}",
        ExportsDeclarationExchange(declaration)
      )
      .map(_.toExportsDeclaration(declaration.sessionId))

  def find(sessionId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[ExportsDeclaration]] =
    httpClient
      .GET[Seq[ExportsDeclarationExchange]](s"${appConfig.customsDeclareExports}${appConfig.submitDeclarationV2}")
      .map(_.map(_.toExportsDeclaration(sessionId)))

  def find(
    sessionId: String,
    id: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ExportsDeclaration]] =
    httpClient
      .GET[Option[ExportsDeclarationExchange]](
        s"${appConfig.customsDeclareExports}${appConfig.submitDeclarationV2}/$id"
      )
      .map(_.map(_.toExportsDeclaration(sessionId)))

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
        wcoMetadataMapper.toXml(metadata),
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
