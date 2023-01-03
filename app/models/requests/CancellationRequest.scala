/*
 * Copyright 2023 HM Revenue & Customs
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

package models.requests

import play.api.libs.json._

sealed trait CancellationStatus

case object CancellationRequestExists extends CancellationStatus

case object CancellationRequested extends CancellationStatus

case object MissingDeclaration extends CancellationStatus

object CancellationStatus {

  def unapply(status: CancellationStatus): Option[(String, JsValue)] = {
    val (prod: Product, sub) = status match {
      case CancellationRequestExists => (CancellationRequestExists, Json.toJson(CancellationRequestExists.toString))
      case CancellationRequested     => (CancellationRequested, Json.toJson(CancellationRequested.toString))
      case MissingDeclaration        => (MissingDeclaration, Json.toJson(MissingDeclaration.toString))
    }
    Some(prod.productPrefix -> sub)
  }

  def apply(`class`: String, data: JsValue): CancellationStatus =
    `class` match {
      case "CancellationRequestExists" => CancellationRequestExists
      case "CancellationRequested"     => CancellationRequested
      case "MissingDeclaration"        => MissingDeclaration
    }

  implicit object CancellationStatusReads extends Reads[CancellationStatus] {
    def reads(jsValue: JsValue): JsResult[CancellationStatus] = jsValue match {
      case JsString("CancellationRequestExists") => JsSuccess(CancellationRequestExists)
      case JsString("CancellationRequested")     => JsSuccess(CancellationRequested)
      case JsString("MissingDeclaration")        => JsSuccess(MissingDeclaration)
      case _                                     => JsError("Incorrect cancellation status")
    }
  }

  implicit object CancellationStatusWrites extends Writes[CancellationStatus] {
    def writes(status: CancellationStatus): JsValue = JsObject(Seq("status" -> JsString(status.toString)))
  }
}
