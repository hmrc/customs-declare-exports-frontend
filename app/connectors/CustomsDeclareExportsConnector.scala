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
import connectors.CustomsDeclareExportsConnector.toXml
import connectors.exchange.ExportsDeclarationExchange
import javax.inject.{Inject, Singleton}
import models.declaration.notifications.Notification
import models.declaration.submissions.Submission
import models.requests.CancellationStatus
import models.{ExportsDeclaration, Page, Paginated}
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Codec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import wco.datamodel.wco.documentmetadata_dms._2.MetaData

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDeclareExportsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) {

  private val logger = Logger(this.getClass)
  
  private def logPayload[T](prefix: String, t: T)(implicit wts: Writes[T]): T = {
    Logger.debug(s"$prefix: ${Json.toJson(t)}")
    t
  }

  def create(
    declaration: ExportsDeclaration
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] = {
    logPayload("Create Declaration Request", declaration)
    httpClient
      .POST[ExportsDeclarationExchange, ExportsDeclarationExchange](
        s"${appConfig.customsDeclareExports}${appConfig.submitDeclarationV2}",
        ExportsDeclarationExchange(declaration)
      )
      .map(logPayload("Create Declaration Response", _))
      .map(_.toExportsDeclaration(declaration.sessionId))
  }

  def update(
    declaration: ExportsDeclaration
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] = {
    logPayload("Update Declaration Request", declaration)
    httpClient
      .PUT[ExportsDeclarationExchange, ExportsDeclarationExchange](
        s"${appConfig.customsDeclareExports}${appConfig.submitDeclarationV2}/${declaration.id
          .getOrElse(throw new IllegalArgumentException("Cannot update a declaration which hasn't been created first"))}",
        ExportsDeclarationExchange(declaration)
      )
      .map(logPayload("Update Declaration Response", _))
      .map(_.toExportsDeclaration(declaration.sessionId))
  }

  def find(
    sessionId: String,
    page: Page
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Paginated[ExportsDeclaration]] = {
    val pagination = Page.bindable.unbind("page", page)
    httpClient
      .GET[Paginated[ExportsDeclarationExchange]](
        s"${appConfig.customsDeclareExports}${appConfig.submitDeclarationV2}?$pagination"
      )
      .map(_.map(_.toExportsDeclaration(sessionId)))
  }

  def find(
    sessionId: String,
    id: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ExportsDeclaration]] =
    httpClient
      .GET[Option[ExportsDeclarationExchange]](
        s"${appConfig.customsDeclareExports}${appConfig.submitDeclarationV2}/$id"
      )
      .map(_.map(_.toExportsDeclaration(sessionId)))

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
        toXml(metadata),
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

object CustomsDeclareExportsConnector {
  def toXml(metaData: MetaData): String = {
    import java.io.StringWriter

    import javax.xml.bind.{JAXBContext, Marshaller}

    val jaxbMarshaller = JAXBContext.newInstance(classOf[MetaData]).createMarshaller
    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)

    val sw = new StringWriter
    jaxbMarshaller.marshal(metaData, sw)
    sw.toString
  }
}
