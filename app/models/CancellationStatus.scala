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

package models

import play.api.libs.json._

sealed trait CancellationStatus

case object NotFound extends CancellationStatus
case object CancellationAlreadyRequested extends CancellationStatus
case object CancellationRequestSent extends CancellationStatus

object CancellationStatus {

  val MrnNotFoundName = NotFound.toString
  val CancellationAlreadyRequestedName = CancellationAlreadyRequested.toString
  val CancellationRequestSentName = CancellationRequestSent.toString

  def unapply(status: CancellationStatus): Option[(String, JsValue)] = {
    val (prod: Product, sub) = status match {
      case CancellationAlreadyRequested => (CancellationAlreadyRequested, Json.toJson(CancellationAlreadyRequestedName))
      case CancellationRequestSent      => (CancellationRequestSent, Json.toJson(CancellationRequestSentName))
      case NotFound                     => (NotFound, Json.toJson(MrnNotFoundName))
    }
    Some(prod.productPrefix -> sub)
  }

  def apply(`class`: String, data: JsValue): CancellationStatus =
    `class` match {
      case CancellationAlreadyRequestedName => CancellationAlreadyRequested
      case CancellationRequestSentName      => CancellationRequestSent
      case MrnNotFoundName                  => NotFound
    }

  implicit object CancellationStatusReads extends Reads[CancellationStatus] {
    def reads(jsValue: JsValue): JsResult[CancellationStatus] = jsValue match {
      case JsString(CancellationAlreadyRequestedName) => JsSuccess(CancellationAlreadyRequested)
      case JsString(CancellationRequestSentName)      => JsSuccess(CancellationRequestSent)
      case JsString(MrnNotFoundName)                  => JsSuccess(NotFound)
      case _                                          => JsError("Incorrect cancellation status")
    }
  }

  implicit object CancellationStatusWrites extends Writes[CancellationStatus] {
    def writes(status: CancellationStatus): JsValue = JsString(status.toString)
  }
}
