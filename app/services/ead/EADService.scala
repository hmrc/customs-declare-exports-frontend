/*
 * Copyright 2022 HM Revenue & Customs
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

package services.ead

import connectors.CustomsDeclareExportsConnector
import models.dis.MrnStatus
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EADService @Inject() (barcodeService: BarcodeService, connector: CustomsDeclareExportsConnector) extends Logging {

  def generateStatus(mrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages): Future[MrnStatus] =
    connector
      .fetchMrnStatus(mrn)
      .map {
        case Some(mrnStatus) =>
          mrnStatus // barcodeService.base64Image(mrn))
        case _ => throw new IllegalArgumentException(s"No declaration information was found")
      }
      .recoverWith { case _: Throwable =>
        logger.error("An error occurred whilst trying to retrieve mrn status")
        throw new IllegalArgumentException(s"No declaration information was found")
      }
}
