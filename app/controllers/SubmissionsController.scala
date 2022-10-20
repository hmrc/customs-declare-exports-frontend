/*
 * Copyright 2022 HM Revenue & Customs
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

import config.PaginationConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.declaration.routes._
import controllers.helpers.ErrorFixModeHelper.setErrorFixMode
import models._
import models.declaration.submissions.Submission
import models.requests.ExportsSessionKeys.{declarationId, errorFixModeSessionKey}
import models.responses.FlashKeys
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.model.FieldNamePointer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.summary.submitted_declaration_page
import views.html.submissions

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionsController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  submissionsPage: submissions,
  submittedDeclarationPage: submitted_declaration_page
)(implicit ec: ExecutionContext, paginationConfig: PaginationConfig)
    extends FrontendController(mcc) with I18nSupport {

  def amend(rejectedId: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    findOrCreateDraftForRejected(rejectedId, Redirect(SummaryController.displayPage))
  }

  def amendErrors(rejectedId: String, redirectUrl: String, pattern: String, message: String): Action[AnyContent] =
    (authenticate andThen verifyEmail).async { implicit request =>
      val flashData = FieldNamePointer.getFieldName(pattern) match {
        case Some(name) if message.nonEmpty => Map(FlashKeys.fieldName -> name, FlashKeys.errorMessage -> message)
        case Some(name)                     => Map(FlashKeys.fieldName -> name)
        case None if message.nonEmpty       => Map(FlashKeys.errorMessage -> message)
        case _                              => Map.empty[String, String]
      }

      findOrCreateDraftForRejected(rejectedId, setErrorFixMode(Redirect(redirectUrl).flashing(Flash(flashData))))
    }

  def displayListOfSubmissions(submissionsPages: SubmissionsPages = SubmissionsPages()): Action[AnyContent] =
    (authenticate andThen verifyEmail).async { implicit request =>
      for {
        submissions <- customsDeclareExportsConnector.fetchSubmissions
        submissionsInDescOrder = submissions.sorted(Submission.newestEarlierOrdering)
      }
      yield Ok(submissionsPage(SubmissionsPagesElements(submissionsInDescOrder, submissionsPages)))
        .removingFromSession(declarationId, errorFixModeSessionKey)
    }

  def viewDeclaration(id: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    customsDeclareExportsConnector.findDeclaration(id).flatMap {
      case Some(declaration) =>
        customsDeclareExportsConnector.findSubmission(id).map { maybeSubmission =>
          Ok(submittedDeclarationPage(maybeSubmission, declaration))
        }

      case None => Future.successful(Redirect(routes.SubmissionsController.displayListOfSubmissions()))
    }
  }

  private def findOrCreateDraftForRejected(rejectedId: String, redirect: Result)(implicit request: WrappedRequest[AnyContent]): Future[Result] =
    customsDeclareExportsConnector.findOrCreateDraftForRejected(rejectedId).map { id =>
      redirect.addingToSession(declarationId -> id)
    }
}
