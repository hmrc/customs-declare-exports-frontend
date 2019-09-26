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

package controllers.navigation

import config.AppConfig
import controllers.util.{FormAction, SaveAndReturn}
import javax.inject.Inject
import models.requests.{ExportsSessionKeys, JourneyRequest}
import models.responses.FlashKeys
import play.api.mvc.{AnyContent, Call, Result, Results}
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.HeaderCarrier

class Navigator @Inject()(appConfig: AppConfig, auditService: AuditService) {

  def continueTo(call: Call)(implicit req: JourneyRequest[AnyContent], hc: HeaderCarrier): Result =
    FormAction.bindFromRequest match {
      case SaveAndReturn =>
        auditService.auditAllPagesUserInput(AuditTypes.SaveAndReturnSubmission, req.cacheModel)
        goToDraftConfirmation()
      case _ => Results.Redirect(call)
    }

  private def goToDraftConfirmation()(implicit req: JourneyRequest[_]): Result = {
    val updatedDateTime = req.cacheModel.updatedDateTime
    val expiry = updatedDateTime.plusSeconds(appConfig.draftTimeToLive.toSeconds)
    Results
      .Redirect(controllers.declaration.routes.ConfirmationController.displayDraftConfirmation())
      .flashing(FlashKeys.expiryDate -> expiry.toEpochMilli.toString)
      .removingFromSession(ExportsSessionKeys.declarationId)
  }

}
