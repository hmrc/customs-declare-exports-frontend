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

import play.api.mvc.{Request, Session}

object SessionHelper {

  val declarationUuid = "declarationUuid"
  val declarationType = "declarationType"

  val errorFixModeSessionKey = "in-error-fix-mode"

  val submissionActionId = "submission.actionId"
  val submissionDucr = "submission.ducr"
  val submissionUuid = "submission.uuid"
  val submissionLrn = "submission.lrn"
  val submissionMrn = "submission.mrn"

  def getValue(key: String)(implicit request: Request[_]): Option[String] =
    request.session.data.get(key)

  def getOrElse(key: String, default: String = "")(implicit request: Request[_]): String =
    request.session.data.getOrElse(key, default)

  def removeValue(key: String)(implicit request: Request[_]): Session =
    request.session - key
}
