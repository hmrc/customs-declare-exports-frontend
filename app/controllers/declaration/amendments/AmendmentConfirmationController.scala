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

import config.featureFlags.DeclarationAmendmentsConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.declaration.amendments.AmendmentResult.AmendmentResult
import controllers.routes.RootController
import models.requests.{ExportsSessionKeys, VerifiedEmailRequest}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.dashboard.DashboardHelper.toDashboard
import views.helpers.Confirmation
import views.html.declaration.amendments._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendmentConfirmationController @Inject() (
  mcc: MessagesControllerComponents,
  declarationAmendmentsConfig: DeclarationAmendmentsConfig,
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  amendment_accepted: amendment_accepted,
  amendment_rejection: amendment_rejection,
  amendment_failed: amendment_failed
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging {

  val displayHoldingPage: Action[AnyContent] = Action {
    if (!declarationAmendmentsConfig.isEnabled) Redirect(RootController.displayPage)
    else NotImplemented
  }

  val displayAcceptedPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    displayPage(AmendmentResult.Accepted, "/amendment-accepted")
  }

  val displayFailedPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    displayPage(AmendmentResult.Failed, "/amendment-failed")
  }

  val displayRejectedPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    displayPage(AmendmentResult.Rejected, "/amendment-rejected")
  }

  private def displayPage(result: AmendmentResult, url: String)(implicit request: VerifiedEmailRequest[AnyContent]): Future[Result] =
    request.session.data
      .get(ExportsSessionKeys.submissionId)
      .fold {
        logger.warn(s"Session on $url does not include the submission's uuid!?")
        Future.successful(Redirect(toDashboard))
      } { submissionId =>
        for {
          submission <- customsDeclareExportsConnector.findSubmission(submissionId)
          declaration <- customsDeclareExportsConnector.findDeclaration(submissionId) recover { case _ => None }
        } yield submission match {
          case submission =>
            val declarationType = declaration.flatMap(_.additionalDeclarationType).fold("")(_.toString)
            val confirmation = Confirmation(request.email, declarationType, submission, None)
            result match {
              case AmendmentResult.Accepted => Ok(amendment_accepted(confirmation))
              case AmendmentResult.Failed   => Ok(amendment_failed(confirmation))
              case AmendmentResult.Rejected => Ok(amendment_rejection(confirmation))
            }
        }
      }
}

object AmendmentResult extends Enumeration {
  type AmendmentResult = Value
  val Accepted, Failed, Rejected = Value
}
