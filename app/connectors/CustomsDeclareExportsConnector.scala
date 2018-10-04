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

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.{CustomsDeclareExportsResponse, ExportsNotification}
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
      s"${appConfig.customsDeclareExports}${appConfig.saveSubmissionResponse}", body, Seq()
    )

  def saveSubmissionResponse(body: Submission)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclareExportsResponse] =
    postSubmission(body).map { response =>
      Logger.debug(s"CUSTOMS_DECLARE_EXPORSTS response is --> ${response.toString}")
      response
    }

  def fetchNotifications(eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[ExportsNotification]] = {
    //httpClient.GET[ExportsNotification](s"${appConfig.customsDeclareExports}${appConfig.fetchNotifications}/$eori")

    Future.successful(Seq(ExportsNotification()))
  }
}