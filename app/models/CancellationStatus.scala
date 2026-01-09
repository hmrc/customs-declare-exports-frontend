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

import play.api.libs.json._

sealed trait CancellationStatus

case object CancellationAlreadyRequested extends CancellationStatus
case object CancellationRequestSent extends CancellationStatus

object CancellationStatus {

  val CancellationAlreadyRequestedName = CancellationAlreadyRequested.toString
  val CancellationRequestSentName = CancellationRequestSent.toString

  def unapply(status: CancellationStatus): Option[(String, JsValue)] = {
    val (prod, sub) = status match {
      case CancellationAlreadyRequested => (CancellationAlreadyRequested, Json.toJson(CancellationAlreadyRequestedName))
      case CancellationRequestSent      => (CancellationRequestSent, Json.toJson(CancellationRequestSentName))
    }
    Some(prod.toString -> sub)
  }

  def apply(`class`: String, data: JsValue): CancellationStatus =
    `class` match {
      case CancellationAlreadyRequestedName => CancellationAlreadyRequested
      case CancellationRequestSentName      => CancellationRequestSent
    }

  implicit object CancellationStatusReads extends Reads[CancellationStatus] {
    def reads(jsValue: JsValue): JsResult[CancellationStatus] = jsValue match {
      case JsString(CancellationAlreadyRequestedName) => JsSuccess(CancellationAlreadyRequested)
      case JsString(CancellationRequestSentName)      => JsSuccess(CancellationRequestSent)
      case _                                          => JsError("Incorrect cancellation status")
    }
  }

  implicit object CancellationStatusWrites extends Writes[CancellationStatus] {
    def writes(status: CancellationStatus): JsValue = JsString(status.toString)
  }

  case class CancellationResult(status: CancellationStatus, conversationId: Option[String])
  object CancellationResult {
    implicit val cancellationResultFormat: Format[CancellationResult] = new Format[CancellationResult] {
      def reads(json: JsValue): JsResult[CancellationResult] =
        for {
          status <- (json \ "status").validate[CancellationStatus]
          conversationId <- (json \ "conversationId").validateOpt[String]
        } yield CancellationResult(status, conversationId)

      def writes(result: CancellationResult): JsValue = {
        val baseJson = Json.obj("status" -> Json.toJson(result.status))
        result.conversationId.map(msg => baseJson ++ Json.obj("conversationId" -> Json.toJson(msg))).getOrElse(baseJson)
      }
    }
  }
}
