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
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDeclareExportsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) {

  private[connectors] def get(url: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    httpClient.GET(url, Seq())

  private[connectors] def postSubmission(
    body: Submission
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclareExportsResponse] =
    httpClient.POST[Submission, CustomsDeclareExportsResponse](
      s"${appConfig.customsDeclareExports}${appConfig.saveSubmissionResponse}",
      body,
      Seq()
    )

  def saveSubmissionResponse(
    body: Submission
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclareExportsResponse] =
    postSubmission(body).map { response =>
      Logger.debug(s"CUSTOMS_DECLARE_EXPORTS response is --> ${response.toString}")
      response
    }

  def fetchNotifications(
    eori: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[ExportsNotification]] =
    httpClient.GET[Seq[ExportsNotification]](s"${appConfig.customsDeclareExports}${appConfig.fetchNotifications}/$eori")

  def fetchNotificationsByConversationId(
    conversationId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ExportsNotification]] =
    httpClient.GET[Option[ExportsNotification]](s"${appConfig.customsDeclareExports}${appConfig.fetchSubmissionNotifications}/$conversationId")

  def saveMovementSubmission(
    body: MovementSubmission
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclareExportsResponse] =
    httpClient
      .POST[MovementSubmission, CustomsDeclareExportsResponse](
        s"${appConfig.customsDeclareExports}${appConfig.saveMovementSubmission}",
        body,
        Seq()
      )
      .map { response =>
        Logger.debug(s"CUSTOMS_DECLARE_EXPORTS save movement response is --> ${response.toString}")
        response
      }

  def fetchSubmissions()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[SubmissionData]] =
    httpClient.GET[Seq[SubmissionData]](s"${appConfig.customsDeclareExports}${appConfig.fetchSubmissions}").map {
      response =>
        Logger.debug(s"CUSTOMS_DECLARE_EXPORTS fetch submission response is --> ${response.toString}")
        response
    }
}
