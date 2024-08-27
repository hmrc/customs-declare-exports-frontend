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

package views.common

import models.DeclarationType.DeclarationType
import models.requests.JourneyRequest
import models.requests.SessionHelper.errorFixModeSessionKey
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

trait PageWithButtonsSpec extends UnitViewSpec {

  val typeAndViewInstance: (DeclarationType, (JourneyRequest[_], Messages) => Appendable)

  "display on the page the 'Save and return to summary' button if the Summary page was already visited" in {
    implicit val request = journeyRequest(aDeclaration(withType(typeAndViewInstance._1), withSummaryWasVisited()))
    val view = typeAndViewInstance._2(request, messages)
    val saveAndReturnToSummaryButton = view.getElementById("save_and_return_to_summary")
    saveAndReturnToSummaryButton must containMessage(saveAndReturnToSummaryCaption)
  }

  "display 'Save and return to errors' button when in error-fix mode" in {
    implicit val request = journeyRequest(aDeclaration(withType(typeAndViewInstance._1)), errorFixModeSessionKey -> "true")
    val view = typeAndViewInstance._2(request, messages)
    val saveAndReturnToErrorsButton = view.getElementById("save_and_return_to_errors")
    saveAndReturnToErrorsButton must containMessage(saveAndReturnToErrorsCaption)
  }
}
