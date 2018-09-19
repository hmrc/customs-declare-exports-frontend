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
import models.CustomsDeclareExportsResponse
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDeclareExportsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) {

  def saveSubmissionResponse(body: Submission)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclareExportsResponse] =
    post(body).map{ response =>
      Logger.debug(s"CUSTOMS_DECLARE_EXPORSTS response is --> ${response.toString}")
      response
    }

  private[connectors] def post(body: Submission)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclareExportsResponse] =
    httpClient.POST[Submission, CustomsDeclareExportsResponse](
      s"${appConfig.customsDeclareExports}${appConfig.saveSubmissionResponse}", body, Seq()
    )
}
