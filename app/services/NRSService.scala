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

package services

import java.nio.charset.StandardCharsets
import java.util.Base64

import com.google.inject.Inject
import config.AppConfig
import connectors.NrsConnector
import javax.inject.Singleton
import models._
import org.joda.time.DateTime
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NRSService @Inject()(appConfig: AppConfig, connector: NrsConnector) {
  private val logger: Logger = Logger(this.getClass)

  def submit(
    conversationId: String,
    payload: String,
    ducr: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext, signedInUser: SignedInUser): Future[NrsSubmissionResponse] = {
    logger.debug(s"[NRSService][submit] - Submitting payload to NRS")
    connector.submitNonRepudiation(convertToNrsSubmission(conversationId, payload, ducr))
  }

  private def convertToNrsSubmission(conversationId: String, payload: String, ducr: String)(
    implicit hc: HeaderCarrier,
    signedInUser: SignedInUser
  ): NRSSubmission = {

    val encoder = Base64.getEncoder
    NRSSubmission(
      payload = encoder.encodeToString(payload.getBytes(StandardCharsets.UTF_8)),
      metadata = Metadata(
        businessId = "cds",
        notableEvent = "cds-exports-dec-submission",
        payloadContentType = "application/json",
        payloadSha256Checksum = None,
        userSubmissionTimestamp = DateTime.now(),
        identityData = signedInUser.identityData,
        userAuthToken = hc.authorization.get.value,
        headerData = createHeaderData(),
        searchKeys = SearchKeys(
          Some(conversationId),
          //TODO update to populate correct ducr/mucr value.
          Some(ducr)
        )
      )
    )
  }

  private def createHeaderData()(implicit hc: HeaderCarrier) =
    //TODO update/revisit for correct details.
    HeaderData(
      hc.trueClientIp,
      hc.trueClientPort,
      hc.deviceID,
      hc.userId.map(_.value),
      Some(hc.nsStamp.toString),
      hc.trueClientIp,
      None
    )
}
