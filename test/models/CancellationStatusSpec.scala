/*
 * Copyright 2024 HM Revenue & Customs
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

import base.UnitSpec
import models.CancellationStatus.CancellationStatusReads.reads
import models.CancellationStatus.CancellationStatusWrites.writes
import play.api.libs.json._

class CancellationStatusSpec extends UnitSpec {

  "Cancellation Status Reads" should {

    "read value correctly" in {

      reads(JsString("CancellationAlreadyRequested")) must be(JsSuccess(CancellationAlreadyRequested))
      reads(JsString("CancellationRequestSent")) must be(JsSuccess(CancellationRequestSent))
      reads(JsString("IncorrectStatus")) must be(JsError("Incorrect cancellation status"))
    }
  }

  "Cancellation Status Writes" should {

    "write value correctly" in {

      writes(CancellationAlreadyRequested) must be(JsString("CancellationAlreadyRequested"))
      writes(CancellationRequestSent) must be(JsString("CancellationRequestSent"))
    }
  }

  "Cancellation Status unapply" should {

    "correctly unapply CancellationStatus object" in {

      val expectedResult =
        Some(CancellationAlreadyRequested.productPrefix -> Json.toJson(CancellationAlreadyRequested.toString))

      CancellationStatus.unapply(CancellationAlreadyRequested) must be(expectedResult)
    }
  }
}
