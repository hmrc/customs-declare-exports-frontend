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

package models.declaration.submissions

import play.api.libs.json.Format
import utils.EnumJson

object EnhancedStatus extends Enumeration {

  type EnhancedStatus = Value

  implicit val format: Format[EnhancedStatus.Value] = EnumJson.format(EnhancedStatus)

  implicit class InclusionInSet(status: EnhancedStatus) {
    def in(set: Set[EnhancedStatus]): Boolean = set.contains(status)
  }

  val ADDITIONAL_DOCUMENTS_REQUIRED, AMENDED, AWAITING_EXIT_RESULTS, CANCELLED, CLEARED, CUSTOMS_POSITION_DENIED, CUSTOMS_POSITION_GRANTED,
    DECLARATION_HANDLED_EXTERNALLY, ERRORS, EXPIRED_NO_ARRIVAL, EXPIRED_NO_DEPARTURE, GOODS_ARRIVED, GOODS_ARRIVED_MESSAGE, GOODS_HAVE_EXITED,
    QUERY_NOTIFICATION_MESSAGE, RECEIVED, RELEASED, UNDERGOING_PHYSICAL_CHECK, WITHDRAWN, PENDING, REQUESTED_CANCELLATION, UNKNOWN = Value

  lazy val actionRequiredStatuses = Set(ADDITIONAL_DOCUMENTS_REQUIRED, QUERY_NOTIFICATION_MESSAGE)

  lazy val cancelledStatuses = Set(CANCELLED, EXPIRED_NO_ARRIVAL, WITHDRAWN, EXPIRED_NO_DEPARTURE)

  lazy val rejectedStatuses = Set(ERRORS)

  lazy val otherStatuses = values &~ rejectedStatuses &~ cancelledStatuses &~ actionRequiredStatuses

  lazy val eadAcceptableStatuses = values &~ Set(CANCELLED, ERRORS, PENDING, UNKNOWN, WITHDRAWN)

  lazy val uploadFilesStatuses = Set(ADDITIONAL_DOCUMENTS_REQUIRED, UNDERGOING_PHYSICAL_CHECK)
}

object EnhancedStatusGroup extends Enumeration {
  type EnhancedStatusGroup = Value
  implicit val format: Format[EnhancedStatusGroup.Value] = EnumJson.format(EnhancedStatusGroup)

  val ActionRequiredStatuses = Value("action")
  val CancelledStatuses = Value("cancelled")
  val RejectedStatuses = Value("rejected")
  val SubmittedStatuses = Value("submitted")

  // Order of the list's elements follows the Dashboard tabs' order
  lazy val statusGroups = List(SubmittedStatuses, ActionRequiredStatuses, RejectedStatuses, CancelledStatuses)
}
