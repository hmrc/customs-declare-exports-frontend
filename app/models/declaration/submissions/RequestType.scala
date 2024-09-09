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

package models.declaration.submissions

import play.api.libs.json._

sealed trait RequestType

object RequestType {

  case object SubmissionRequest extends RequestType
  case object CancellationRequest extends RequestType
  case object AmendmentRequest extends RequestType
  case object AmendmentCancellationRequest extends RequestType
  case object ExternalAmendmentRequest extends RequestType

  implicit object RequestTypeFormat extends Format[RequestType] {
    override def writes(requestType: RequestType): JsValue = JsString(requestType.toString)

    override def reads(json: JsValue): JsResult[RequestType] = json match {
      case JsString("SubmissionRequest")            => JsSuccess(SubmissionRequest)
      case JsString("CancellationRequest")          => JsSuccess(CancellationRequest)
      case JsString("AmendmentRequest")             => JsSuccess(AmendmentRequest)
      case JsString("AmendmentCancellationRequest") => JsSuccess(AmendmentCancellationRequest)
      case JsString("ExternalAmendmentRequest")     => JsSuccess(ExternalAmendmentRequest)
      case _                                        => JsError(s"Could not read Request Type from: $json")
    }
  }
}
