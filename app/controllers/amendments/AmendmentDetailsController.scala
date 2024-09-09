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

package controllers.amendments

import com.google.inject.Inject
import config.featureFlags.DeclarationAmendmentsConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.helpers.AmendmentHelper
import controllers.general.ErrorHandler
import controllers.general.routes.RootController
import models.ExportsDeclaration
import models.declaration.submissions.{Action => ActionOfSubmission, Submission}
import models.requests.VerifiedEmailRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.amendments.{amendment_details, unavailable_amendment_details}

import scala.concurrent.{ExecutionContext, Future}

class AmendmentDetailsController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  mcc: MessagesControllerComponents,
  errorHandler: ErrorHandler,
  connector: CustomsDeclareExportsConnector,
  amendmentDetails: amendment_details,
  unavailableAmendmentDetails: unavailable_amendment_details,
  declarationAmendmentsConfig: DeclarationAmendmentsConfig,
  amendmentHelper: AmendmentHelper
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging {

  def displayPage(actionId: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    if (!declarationAmendmentsConfig.isEnabled) Future.successful(Redirect(RootController.displayPage))
    else
      connector.findSubmissionByAction(actionId).flatMap {
        case Some(submission) =>
          submission.action(actionId).map(displayPage(submission, _)).getOrElse {
            val msg = s"For the given actionId($actionId) on /amendment-details, the related Submission has not such action??"
            errorHandler.internalError(msg)
          }

        case _ =>
          val msg = s"For the given actionId($actionId) on /amendment-details, the related Submission was not found??"
          errorHandler.internalError(msg)
      }
  }

  private def displayPage(submission: Submission, action: ActionOfSubmission)(implicit request: VerifiedEmailRequest[_]): Future[Result] =
    action.decId match {
      case Some(declarationId) =>
        connector.findDeclaration(declarationId).flatMap {
          case Some(declaration) =>
            declaration.declarationMeta.parentDeclarationId.fold {
              errorHandler.internalError(s"No parentDeclarationId for Declaration($declarationId) on /amendment-details??")
            } {
              connector.findDeclaration(_).flatMap {
                case Some(parentDeclaration) =>
                  val reason = declaration.statementDescription
                  val amendmentRows = amendmentHelper.generateAmendmentRows(parentDeclaration, declaration)
                  Future.successful(Ok(amendmentDetails(submission.uuid, ducr(declaration), reason, action, amendmentRows)))

                case _ =>
                  errorHandler.internalError(s"No parent Declaration found for Declaration($declarationId) on /amendment-details??")
              }
            }
          case _ =>
            errorHandler.internalError(s"No Declaration found for Action(${action.id}) on /amendment-details??")
        }

      case _ => Future.successful(Ok(unavailableAmendmentDetails(submission.uuid, submission.ducr)))
    }

  private def ducr(declaration: ExportsDeclaration): String =
    declaration.consignmentReferences.flatMap(_.ducr).fold("")(_.ducr)
}
