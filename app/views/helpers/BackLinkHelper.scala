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

package views.helpers

import controllers.navigation.Navigator
import controllers.section4.routes.NatureOfTransactionController
import controllers.summary.routes.SectionSummaryController
import forms.section4.Document
import models.DeclarationType
import models.requests.JourneyRequest

object BackLinkHelper {
  def getBackLink(navigator: Navigator)(implicit request: JourneyRequest[_]): String = {
    val backLink = navigator.backLink(Document)
    val documentSize = request.cacheModel.previousDocuments.map(_.documents.size).getOrElse(0)
    if (backLink == NatureOfTransactionController.displayPage && request.declarationType != DeclarationType.OCCASIONAL)
      "site.backToPreviousQuestion"
    else if (request.declarationType == DeclarationType.OCCASIONAL && documentSize == 0)
      "site.backToPreviousSection"
    else if (backLink == SectionSummaryController.displayPage(3) && request.declarationType != DeclarationType.CLEARANCE && documentSize >= 1)
      "site.backToPreviousQuestion"
    else if (documentSize >= 1)
      "site.backToPreviousQuestion"
    else
      "site.backToPreviousSection"
  }

}
