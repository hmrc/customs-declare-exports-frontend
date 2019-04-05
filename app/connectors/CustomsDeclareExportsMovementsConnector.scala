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
import models.requests.CancellationStatus
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.wco.dec.MetaData

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDeclareExportsMovementsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) {

  def submitMovementDeclaration(ducr: String, mucr: Option[String], movementType: String, xmlBody: String)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] =
    httpClient
      .POSTString[HttpResponse](
        s"${appConfig.customsDeclareExportsMovements}${appConfig.saveMovementSubmission}",
        xmlBody,
        Seq(
          (HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8)),
          (HeaderNames.ACCEPT -> ContentTypes.XML(Codec.utf_8)),
          ("X-DUCR", ducr),
          ("X-MUCR", mucr.getOrElse("")),
          ("X-MOVEMENT-TYPE", movementType.toString)
        )
      )
      .map { response =>
        Logger.debug(s"CUSTOMS_DECLARE_EXPORTS_MOVEMENTS response is --> ${response.toString}")
        response
      }

  def saveMovementSubmission(
    body: MovementSubmission
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CustomsDeclareExportsMovementsResponse] =
    httpClient
      .POST[MovementSubmission, CustomsDeclareExportsMovementsResponse](
        s"${appConfig.customsDeclareExportsMovements}${appConfig.saveMovementSubmission}",
        body,
        Seq()
      )
      .map { response =>
        Logger.debug(s"CUSTOMS_DECLARE_EXPORTS save movement response is --> ${response.toString}")
        response
      }

}
