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
import controllers.actions.{AuthAction, JourneyAction, VerifiedEmailAction}
import controllers.declaration.routes.SummaryController
import controllers.declaration.{ModelCacheable, SubmissionErrors}
import controllers.routes.{CopyDeclarationController, DeclarationDetailsController}
import forms.CopyDeclaration.form
import forms.declaration.ConsignmentReferences
import forms.{CopyDeclaration, Ducr, LrnValidator}
import handlers.ErrorHandler
import models.declaration.DeclarationStatus.DRAFT
import models.requests.SessionHelper._
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.helpers.DeclarationDetailsHelper.isDeclarationRejected
import views.html.copy_declaration

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CopyDeclarationController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  override val exportsCacheService: ExportsCacheService,
  lrnValidator: LrnValidator,
  mcc: MessagesControllerComponents,
  copyDeclarationPage: copy_declaration
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def redirectToReceiveJourneyRequest(submissionId: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    customsDeclareExportsConnector.findSubmission(submissionId).map {
      case Some(submission) =>
        if (isDeclarationRejected(submission)) Redirect(DeclarationDetailsController.displayPage(submissionId))
        else
          Redirect(CopyDeclarationController.displayPage)
            .addingToSession(declarationUuid -> submissionId)
            .removingFromSession(submissionDucr, submissionUuid, submissionLrn, submissionMrn)

      case _ => errorHandler.internalServerError(s"Cannot found Submission($submissionId) while redirecting to a declaration copy?!")
    }
  }

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    Future.successful(Ok(copyDeclarationPage(form.withSubmissionErrors)))
  }

  val submitPage: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .verifyLrnValidity(lrnValidator)
      .flatMap {
        _.fold(formWithErrors => Future.successful(BadRequest(copyDeclarationPage(formWithErrors))), copyDeclaration)
      }
  }

  private def copyDeclaration(data: CopyDeclaration)(implicit request: JourneyRequest[_]): Future[Result] = {
    val submissionId = request.cacheModel.id
    customsDeclareExportsConnector.findSubmission(submissionId).flatMap {
      case Some(submission) =>
        submission.latestDecId.fold {
          errorHandler.internalError(s"Submission(${submissionId}) with undefined latestDecId while a declaration copy?!")
        } { latestDecId =>
          customsDeclareExportsConnector.findDeclaration(latestDecId).flatMap {
            case Some(latestDeclaration) =>
              val declaration = latestDeclaration.copy(
                declarationMeta = latestDeclaration.declarationMeta.copy(
                  parentDeclarationId = Some(latestDeclaration.id),
                  parentDeclarationEnhancedStatus = submission.latestEnhancedStatus,
                  status = DRAFT,
                  createdDateTime = Instant.now,
                  updatedDateTime = Instant.now
                ),
                consignmentReferences = Some(ConsignmentReferences(Some(Ducr(data.ducr.ducr.toUpperCase)), Some(data.lrn))),
                linkDucrToMucr = None,
                mucr = None
              )
              exportsCacheService.create(declaration).map { declaration =>
                Redirect(SummaryController.displayPage).addingToSession(declarationUuid -> declaration.id)
              }

            case _ => errorHandler.internalError(s"Cannot found latest declaration(${latestDecId}) while a declaration copy?!")
          }
        }

      case _ => errorHandler.internalError(s"Cannot found Submission(${submissionId}) while a declaration copy?!")
    }
  }
}
