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

package controllers.general

import models.requests.JourneyRequest
import play.api.data.Form

trait SubmissionErrors {

  class SubmissionForm[A](form: Form[A]) {
    def withSubmissionErrors(implicit request: JourneyRequest[_]): Form[A] = form.copy(errors = request.submissionErrors)
  }

  import scala.language.implicitConversions
  implicit def formToForm[A](form: Form[A]): SubmissionForm[A] = new SubmissionForm[A](form)
}
