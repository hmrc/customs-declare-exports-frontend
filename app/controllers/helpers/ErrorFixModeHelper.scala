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

package controllers.helpers

import models.requests.SessionHelper.{errorFixModeSessionKey, getValue}
import play.api.mvc.{Request, Result}

import scala.util.Try

object ErrorFixModeHelper {

  def inErrorFixMode(implicit request: Request[_]): Boolean =
    getValue(errorFixModeSessionKey).fold(false)(v => Try(v.toBoolean).getOrElse(false))

  def setErrorFixMode(result: Result)(implicit request: Request[_]): Result =
    result.addingToSession(errorFixModeSessionKey -> "true")
}
