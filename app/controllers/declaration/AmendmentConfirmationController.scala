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

package controllers.declaration

import config.featureFlags.DeclarationAmendmentsConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.routes.RootController
import models.requests.{ExportsSessionKeys, VerifiedEmailRequest}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.dashboard.DashboardHelper.toDashboard
import views.helpers.Confirmation
import views.html.declaration.confirmation.{amendment_confirmation_page, amendment_rejection_page}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendmentConfirmationController @Inject() (
  mcc: MessagesControllerComponents,
  declarationAmendmentsConfig: DeclarationAmendmentsConfig,
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  amendment_confirmation_page: amendment_confirmation_page,
  amendment_rejection_page: amendment_rejection_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging {

  val displayHoldingPage: Action[AnyContent] = Action {
    if (!declarationAmendmentsConfig.isEnabled) Redirect(RootController.displayPage)
    else NotImplemented
  }

  val displayConfirmationPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    getSubmissionDeclaration(confirmation => Ok(amendment_confirmation_page(confirmation)))
  }

  val displayConfirmationRejectionPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    getSubmissionDeclaration(confirmation => Ok(amendment_rejection_page(confirmation)))
  }

  private def getSubmissionDeclaration(view: Confirmation => Result)(implicit request: VerifiedEmailRequest[_]): Future[Result] =
    request.session.data
      .get(ExportsSessionKeys.submissionId)
      .fold {
        logger.warn("Session on /amendment-confirmation does not include the submission's uuid!?")
        Future.successful(Redirect(toDashboard))
      } { submissionId =>
        for {
          submission <- customsDeclareExportsConnector.findSubmission(submissionId)
          declaration <- customsDeclareExportsConnector.findDeclaration(submissionId) recover { case _ => None }
        } yield submission match {
          case submission =>
            val confirmation =
              Confirmation(request.email, declaration.flatMap(_.additionalDeclarationType).fold("")(_.toString), submission, None)
            view(confirmation)
        }
      }
}
