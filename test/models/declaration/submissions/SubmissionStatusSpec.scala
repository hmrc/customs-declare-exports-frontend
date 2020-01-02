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

import SubmissionStatus._
import org.scalatest.{MustMatchers, WordSpec}

class SubmissionStatusSpec extends WordSpec with MustMatchers {

  "format" should {

    "correctly convert status to specified string" in {

      format(PENDING) must be("Pending")
      format(REQUESTED_CANCELLATION) must be("Cancellation Requested")
      format(ACCEPTED) must be("Accepted")
      format(RECEIVED) must be("Received")
      format(REJECTED) must be("Rejected")
      format(UNDERGOING_PHYSICAL_CHECK) must be("Undergoing Physical Check")
      format(ADDITIONAL_DOCUMENTS_REQUIRED) must be("Additional Documents Required")
      format(AMENDED) must be("Amended")
      format(RELEASED) must be("Released")
      format(CLEARED) must be("Cleared")
      format(CANCELLED) must be("Cancelled")
      format(CUSTOMS_POSITION_GRANTED) must be("Customs Position Granted")
      format(CUSTOMS_POSITION_DENIED) must be("Customs Position Denied")
      format(GOODS_HAVE_EXITED_THE_COMMUNITY) must be("Goods Have Exited The Community")
      format(DECLARATION_HANDLED_EXTERNALLY) must be("Declaration Handled Externally")
      format(AWAITING_EXIT_RESULTS) must be("Awaiting Exit Results")
      format(UNKNOWN) must be("Unknown status")
    }
  }
}
