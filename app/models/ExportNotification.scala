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

package models

import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.hmrc.wco.dec._

import scala.util.Random

case class ExportsNotification(
  dateTimeReceived: DateTime = DateTime.now(),
  conversationId: String,
  eori: String,
  badgeId: Option[String] = None,
  metadata: DeclarationMetadata,
  response: Seq[Response] = Seq.empty
)

case class DeclarationMetadata(
  wcoDataModelVersionCode: Option[String] = None,
  wcoTypeName: Option[String] = None,
  responsibleCountryCode: Option[String] = None,
  responsibleAgencyName: Option[String] = None,
  agencyAssignedCustomizationCode: Option[String] = None,
  agencyAssignedCustomizationVersionCode: Option[String] = None
)

object DeclarationMetadata {
  implicit val declarationMetadataFormats = Json.format[DeclarationMetadata]
}

object ExportsNotification {
  implicit val measureFormats = Json.format[Measure]
  implicit val amountFormats = Json.format[Amount]
  implicit val dateTimeStringFormats = Json.format[DateTimeString]
  implicit val responseDateTimeElementFormats = Json.format[ResponseDateTimeElement]
  implicit val responsePointerFormats = Json.format[ResponsePointer]
  implicit val responseDutyTaxFeePaymentFormats = Json.format[ResponseDutyTaxFeePayment]
  implicit val responseCommodityDutyTaxFeeFormats = Json.format[ResponseCommodityDutyTaxFee]
  implicit val responseCommodityFormats = Json.format[ResponseCommodity]
  implicit val responseGovernmentAgencyGoodsItemFormats = Json.format[ResponseGovernmentAgencyGoodsItem]
  implicit val responseGoodsShipmentFormats = Json.format[ResponseGoodsShipment]
  implicit val responseCommunicationFormats = Json.format[ResponseCommunication]
  implicit val responseObligationGuaranteeFormats = Json.format[ResponseObligationGuarantee]
  implicit val responseStatusFormats = Json.format[ResponseStatus]

  implicit val responseAdditionalInformationFormats = Json.format[ResponseAdditionalInformation]
  implicit val responseAmendmentFormats = Json.format[ResponseAmendment]
  implicit val responseAppealOfficeFormats = Json.format[ResponseAppealOffice]
  implicit val responseBankFormats = Json.format[ResponseBank]
  implicit val responseContactOffice = Json.format[ResponseContactOffice]
  implicit val responseErrorFormats = Json.format[ResponseError]
  implicit val responsePaymentFormats = Json.format[ResponsePayment]
  implicit val responseDutyTaxFee = Json.format[ResponseDutyTaxFee]
  implicit val responseDeclarationFormats = Json.format[ResponseDeclaration]

  implicit val responseFormats = Json.format[Response]
  implicit val exportsNotificationFormats = Json.format[ExportsNotification]
}

// Case class for view notifications - TODO use ExportsNotification directly and map fields
case class Notification(name: String, dateAndTime: String, reference: String, status: Status)

object Notification {
  implicit val format = Json.format[Notification]

  private def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  private val Ten = 10

  // Only to generate random data
  def randomNotifications(amount: Int = Ten): Seq[Notification] =
    Seq.fill(amount)(Notification(randomString(Ten), randomString(Ten), randomString(Ten), Query))
}
