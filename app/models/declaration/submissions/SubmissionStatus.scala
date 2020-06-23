/*
 * Copyright 2020 HM Revenue & Customs
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
import models.declaration.submissions
import play.api.libs.json.Format
import utils.EnumJson

object SubmissionStatus extends Enumeration {
  type SubmissionStatus = Value
  implicit val format: Format[SubmissionStatus.Value] = EnumJson.format(SubmissionStatus)

  val PENDING, REQUESTED_CANCELLATION, ACCEPTED, RECEIVED, REJECTED, UNDERGOING_PHYSICAL_CHECK, ADDITIONAL_DOCUMENTS_REQUIRED, AMENDED, RELEASED,
  CLEARED, CANCELLED, CUSTOMS_POSITION_GRANTED, CUSTOMS_POSITION_DENIED, GOODS_HAVE_EXITED_THE_COMMUNITY, DECLARATION_HANDLED_EXTERNALLY,
  AWAITING_EXIT_RESULTS, UNKNOWN = Value

  def format(status: SubmissionStatus): String = status match {
    case PENDING                         => "Pending"
    case REQUESTED_CANCELLATION          => "Cancellation Requested"
    case ACCEPTED                        => "Accepted"
    case RECEIVED                        => "Received"
    case REJECTED                        => "Rejected"
    case UNDERGOING_PHYSICAL_CHECK       => "Undergoing Physical Check"
    case ADDITIONAL_DOCUMENTS_REQUIRED   => "Additional Documents Required"
    case AMENDED                         => "Amended"
    case RELEASED                        => "Released"
    case CLEARED                         => "Cleared"
    case CANCELLED                       => "Cancelled"
    case CUSTOMS_POSITION_GRANTED        => "Customs Position Granted"
    case CUSTOMS_POSITION_DENIED         => "Customs Position Denied"
    case GOODS_HAVE_EXITED_THE_COMMUNITY => "Goods Have Exited The Community"
    case DECLARATION_HANDLED_EXTERNALLY  => "Declaration Handled Externally"
    case AWAITING_EXIT_RESULTS           => "Awaiting Exit Results"
    case UNKNOWN                         => "Unknown status"
  }

  val rejectedStatuses: Set[submissions.SubmissionStatus.Value] = Set(REJECTED)
  val actionRequiredStatuses: Set[submissions.SubmissionStatus.Value] = Set(ADDITIONAL_DOCUMENTS_REQUIRED, UNDERGOING_PHYSICAL_CHECK)
  val otherStatuses: Set[submissions.SubmissionStatus.Value] = values &~ rejectedStatuses &~ actionRequiredStatuses
  val acceptableStatuses: Set[submissions.SubmissionStatus.Value] = values &~ Set(PENDING, REJECTED, UNKNOWN)
}
