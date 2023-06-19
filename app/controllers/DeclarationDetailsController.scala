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

package controllers

import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import handlers.ErrorHandler
import models.declaration.submissions.Submission
import models.requests.SessionHelper._
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{declaration_details, unavailable_timeline_actions}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationDetailsController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  errorHandler: ErrorHandler,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  declarationDetailsPage: declaration_details,
  unavailableTimelineActions: unavailable_timeline_actions
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(submissionId: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    customsDeclareExportsConnector.findSubmission(submissionId).flatMap {
      case Some(submission) =>
        retrieveAdditionalDeclarationType(submission).map {
          case Right(declarationType) =>
            Ok(declarationDetailsPage(submission, declarationType)).addingToSession(sessionKeys(submission): _*)

          case Left(message) => errorHandler.internalServerError(message)
        }

      case _ => errorHandler.internalError(s"Cannot found Submission($submissionId) for the Timeline page??")
    }
  }

  def unavailableActions(submissionId: String): Action[AnyContent] = (authenticate andThen verifyEmail) { implicit request =>
    Ok(unavailableTimelineActions(submissionId))
  }

  private type ErrorOrDecId = Either[String, String]

  private def declarationIdOrError(submission: Submission): ErrorOrDecId =
    if (submission.hasExternalAmendments) Right(submission.uuid) else latestDecIdOrError(submission)

  private def latestDecIdOrError(submission: Submission): ErrorOrDecId =
    submission.latestDecId.fold[ErrorOrDecId] {
      Left(s"Submission(${submission.uuid}) with undefined latestDecId for the Timeline page??")
    } { latestDecId =>
      // Using 'latestDecId', in place of submission.uuid (which is indeed eq to the declaration's id), to retrieve
      // the declaration as in future we could need to retrieve, from the declaration, other data (which might have
      // been amended) in addition to the ADT.
      // If that will be the case, we could even directly pass the full declaration to the Timeline page.
      Right(latestDecId)
    }

  private type ErrorOrADT = Either[String, AdditionalDeclarationType]

  private def retrieveAdditionalDeclarationType(submission: Submission)(implicit request: Request[_]): Future[ErrorOrADT] =
    declarationIdOrError(submission) match {
      case Right(declarationId) =>
        customsDeclareExportsConnector.findDeclaration(declarationId).map {
          case Some(declaration) =>
            declaration.additionalDeclarationType match {
              case Some(additionalDeclarationType) => Right(additionalDeclarationType)
              case _                               => Left(s"Submitted declaration(${declaration.id}) has no additionalDeclarationType??")
            }

          case _ => Left(s"Cannot found latest declaration(${declarationId}) for the Timeline page??")
        }

      case Left(error) => Future.successful(Left(error))
    }

  private def sessionKeys(submission: Submission): Seq[(String, String)] = {
    val submissionId = Some(submissionUuid -> submission.uuid)
    val lrn = Some(submissionLrn -> submission.lrn)
    val mrn = submission.mrn.map(submissionMrn -> _)
    val ducr = submission.ducr.map(submissionDucr -> _)

    Seq(submissionId, lrn, mrn, ducr).flatten
  }
}
