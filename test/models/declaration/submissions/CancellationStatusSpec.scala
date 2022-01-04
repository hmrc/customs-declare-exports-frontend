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

import models.declaration.submissions.CancellationStatus.CancellationStatusReads.reads
import models.declaration.submissions.CancellationStatus.CancellationStatusWrites.writes
import base.UnitSpec
import play.api.libs.json._

class CancellationStatusSpec extends UnitSpec {

  "Cancellation Status Reads" should {

    "read value correctly" in {

      reads(JsString("CancellationRequestExists")) must be(JsSuccess(CancellationRequestExists))
      reads(JsString("CancellationRequested")) must be(JsSuccess(CancellationRequested))
      reads(JsString("MissingDeclaration")) must be(JsSuccess(MissingDeclaration))
      reads(JsString("IncorrectStatus")) must be(JsError("Incorrect cancellation status"))
    }
  }

  "Cancellation Status Writes" should {

    "write value correctly" in {

      writes(CancellationRequestExists) must be(JsString("CancellationRequestExists"))
      writes(CancellationRequested) must be(JsString("CancellationRequested"))
      writes(MissingDeclaration) must be(JsString("MissingDeclaration"))
    }
  }

  "Cancellation Status unapply" should {

    "correctly unapply CancellationStatus object" in {

      val expectedResult =
        Some(CancellationRequestExists.productPrefix -> Json.toJson(CancellationRequestExists.toString))

      CancellationStatus.unapply(CancellationRequestExists) must be(expectedResult)
    }
  }
}
