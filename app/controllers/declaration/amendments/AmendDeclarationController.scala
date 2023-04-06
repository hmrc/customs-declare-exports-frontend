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

package controllers.declaration.amendments

import com.google.inject.Inject
import config.featureFlags.DeclarationAmendmentsConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.declaration.routes.SummaryController
import controllers.routes.RootController
import handlers.ErrorHandler
import models.requests.SessionHelper.{declarationUuid, getValue, submissionUuid}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class AmendDeclarationController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  connector: CustomsDeclareExportsConnector,
  declarationAmendmentsConfig: DeclarationAmendmentsConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) {

  val initAmendment: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    if (!declarationAmendmentsConfig.isEnabled) Future.successful(Redirect(RootController.displayPage))
    else
      getValue(submissionUuid) match {
        case Some(submissionId) =>
          connector.findOrCreateDraftForAmend(submissionId).map { declarationId =>
            Redirect(SummaryController.displayPage).addingToSession(declarationUuid -> declarationId)
          }

        case _ => errorHandler.redirectToErrorPage
      }
  }
}
