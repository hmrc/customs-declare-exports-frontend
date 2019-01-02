/*
 * Copyright 2019 HM Revenue & Customs
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

sealed trait Status

object Status {

  implicit object StatusFormat extends Format[Status] {
    def reads(status: JsValue): JsResult[Status] = status match {
      case JsString("Accepted")  => JsSuccess(Accepted)
      case JsString("Arrived")   => JsSuccess(Arrived)
      case JsString("Departed")  => JsSuccess(Departed)
      case JsString("PreLodged") => JsSuccess(PreLodged)
      case JsString("Query")     => JsSuccess(Query)
      case _                     => JsError("Incorrect value")
    }

    def writes(status: Status): JsValue = status match {
      case Accepted  => JsString("Accepted")
      case Arrived   => JsString("Arrived")
      case Departed  => JsString("Departed")
      case PreLodged => JsString("PreLodged")
      case Query     => JsString("Query")
    }
  }

}

case object Accepted extends Status

case object Arrived extends Status

case object Departed extends Status

case object PreLodged extends Status

case object Query extends Status
