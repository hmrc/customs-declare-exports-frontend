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

  val ADDITIONAL_DOCUMENTS_REQUIRED, AMENDED, AWAITING_EXIT_RESULTS, CANCELLED, CLEARED, CUSTOMS_POSITION_DENIED, CUSTOMS_POSITION_GRANTED,
    DECLARATION_HANDLED_EXTERNALLY, ERRORS, EXPIRED_NO_ARRIVAL, EXPIRED_NO_DEPARTURE, GOODS_ARRIVED, GOODS_ARRIVED_MESSAGE, GOODS_HAVE_EXITED,
    QUERY_NOTIFICATION_MESSAGE, RECEIVED, RELEASED, UNDERGOING_PHYSICAL_CHECK, WITHDRAWN, PENDING, REQUESTED_CANCELLATION, UNKNOWN = Value

  lazy val rejectedStatuses = Seq(CANCELLED, ERRORS, WITHDRAWN)

  lazy val eadAcceptableStatuses: Set[EnhancedStatus.Value] = values &~ (Set(PENDING, UNKNOWN) ++ rejectedStatuses)
}
