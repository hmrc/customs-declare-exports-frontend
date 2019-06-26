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

package models.declaration.submissions

import play.api.libs.json._
import uk.gov.hmrc.wco.dec.Response

sealed trait SubmissionStatus {
  val fullCode: String
}

object SubmissionStatus {

  implicit object StatusFormat extends Format[SubmissionStatus] {
    def reads(status: JsValue): JsResult[SubmissionStatus] = status match {
      case JsString(code) => JsSuccess(getStatusOrUnknown(code))
      case _              => JsSuccess(UnknownStatus)
    }

    def writes(status: SubmissionStatus): JsValue = JsString(status.fullCode)
  }

  def retrieveFromResponse(response: Response): SubmissionStatus = {
    val searchKey = response.functionCode + response.status.headOption.flatMap(_.nameCode).getOrElse("")
    getStatusOrUnknown(searchKey)
  }

  def retrieve(functionCode: String, nameCode: Option[String]): SubmissionStatus =
    getStatusOrUnknown(functionCode + nameCode.getOrElse(""))

  private def getStatusOrUnknown(searchKey: String): SubmissionStatus =
    codesMap.get(searchKey) match {
      case Some(status) => status
      case None         => UnknownStatus
    }

  private val codesMap: Map[String, SubmissionStatus] = Map(
    Pending.fullCode -> Pending,
    RequestedCancellation.fullCode -> RequestedCancellation,
    Accepted.fullCode -> Accepted,
    Received.fullCode -> Received,
    Rejected.fullCode -> Rejected,
    UndergoingPhysicalCheck.fullCode -> UndergoingPhysicalCheck,
    AdditionalDocumentsRequired.fullCode -> AdditionalDocumentsRequired,
    Amended.fullCode -> Amended,
    Released.fullCode -> Released,
    Cleared.fullCode -> Cleared,
    Cancelled.fullCode -> Cancelled,
    CustomsPositionGranted.fullCode -> CustomsPositionGranted,
    CustomsPositionDenied.fullCode -> CustomsPositionDenied,
    GoodsHaveExitedTheCommunity.fullCode -> GoodsHaveExitedTheCommunity,
    DeclarationHandledExternally.fullCode -> DeclarationHandledExternally,
    AwaitingExitResults.fullCode -> AwaitingExitResults,
    UnknownStatus.fullCode -> UnknownStatus
  )
}

case object Pending extends SubmissionStatus {
  val fullCode: String = "Pending"
}

case object RequestedCancellation extends SubmissionStatus {
  val fullCode: String = "Cancellation Requested"

  override def toString: String = "Cancellation Requested"
}

case object Accepted extends SubmissionStatus {
  val fullCode: String = "01"
}

case object Received extends SubmissionStatus {
  val fullCode: String = "02"
}

case object Rejected extends SubmissionStatus {
  val fullCode: String = "03"
}

case object UndergoingPhysicalCheck extends SubmissionStatus {
  val fullCode: String = "05"

  override def toString(): String = "Undergoing Physical Check"
}

case object AdditionalDocumentsRequired extends SubmissionStatus {
  val fullCode: String = "06"

  override def toString(): String = "Additional Documents Required"
}

case object Amended extends SubmissionStatus {
  val fullCode: String = "07"
}

case object Released extends SubmissionStatus {
  val fullCode: String = "08"
}

case object Cleared extends SubmissionStatus {
  val fullCode: String = "09"
}

case object Cancelled extends SubmissionStatus {
  val fullCode: String = "10"
}

case object CustomsPositionGranted extends SubmissionStatus {
  val fullCode: String = "1139"

  override def toString(): String = "Customs Position Granted"
}

case object CustomsPositionDenied extends SubmissionStatus {
  val fullCode: String = "1141"

  override def toString(): String = "Customs Position Denied"
}

case object GoodsHaveExitedTheCommunity extends SubmissionStatus {
  val fullCode: String = "16"

  override def toString(): String = "Goods Have Exited The Community"
}

case object DeclarationHandledExternally extends SubmissionStatus {
  val fullCode: String = "17"

  override def toString(): String = "Declaration Handled Externally"
}

case object AwaitingExitResults extends SubmissionStatus {
  val fullCode: String = "18"

  override def toString(): String = "Awaiting Exit Results"
}

case object UnknownStatus extends SubmissionStatus {
  val fullCode: String = "UnknownStatus"

  override def toString(): String = "Unknown status"
}
