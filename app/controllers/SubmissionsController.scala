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
import controllers.declaration.routes._
import controllers.helpers.ErrorFixModeHelper.setErrorFixMode
import handlers.ErrorHandler
import models.declaration.submissions.EnhancedStatus.ERRORS
import models.requests.AuthenticatedRequest
import models.requests.SessionHelper.declarationUuid
import models.responses.FlashKeys
import controllers.routes.RootController
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.Logging
import services.cache.ExportsCacheService
import services.model.FieldNamePointer
import uk.gov.hmrc.play.bootstrap.binders.{OnlyRelative, RedirectUrl}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.summary.submitted_declaration_page

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionsController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  cacheService: ExportsCacheService,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  submittedDeclarationPage: submitted_declaration_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging {

  private val authAndEmailActions = authenticate andThen verifyEmail

  def amend(rejectedParentId: String, isAmendment: Boolean): Action[AnyContent] = authAndEmailActions.async { implicit request =>
    val redirect = Redirect(SummaryController.displayPage)
    if (isAmendment) findOrCreateDraftForAmendment(rejectedParentId, redirect)
    else findOrCreateDraftForRejection(rejectedParentId, redirect)
  }

  def amendErrors(rejectedParentId: String, pattern: String, message: String, isAmendment: Boolean, redirectUrl: RedirectUrl): Action[AnyContent] =
    authAndEmailActions.async { implicit request =>
      val flashData = FieldNamePointer.getFieldName(pattern) match {
        case Some(name) if message.nonEmpty => Map(FlashKeys.fieldName -> name, FlashKeys.errorMessage -> message)
        case Some(name)                     => Map(FlashKeys.fieldName -> name)
        case None if message.nonEmpty       => Map(FlashKeys.errorMessage -> message)
        case _                              => Map.empty[String, String]
      }

      val redirect = setErrorFixMode(Redirect(redirectUrl.get(OnlyRelative).url).flashing(Flash(flashData)))
      if (isAmendment) findOrCreateDraftForAmendment(rejectedParentId, redirect)
      else findOrCreateDraftForRejection(rejectedParentId, redirect)
    }

  def viewDeclaration(id: String): Action[AnyContent] = authAndEmailActions.async { implicit request =>
    customsDeclareExportsConnector.findDeclaration(id).flatMap {
      case Some(declaration) =>
        customsDeclareExportsConnector.findSubmissionByLatestDecId(id) flatMap {
          case Some(submission) =>
            Future.successful(Ok(submittedDeclarationPage(submission, declaration)))
          case _ =>
            errorHandler.internalError(s"Cannot find submission from latestDecId $id")
        }
      case None => errorHandler.internalError(s"Cannot find declaration from $id")
    }
  }

  private def findOrCreateDraftForAmendment(rejectedParentId: String, redirect: Result)(implicit request: AuthenticatedRequest[_]): Future[Result] =
    cacheService.get(rejectedParentId).flatMap {
      case Some(srcDec) =>
        customsDeclareExportsConnector.findOrCreateDraftForAmendment(rejectedParentId, ERRORS, request.user.eori, srcDec).map { id =>
          redirect.addingToSession(declarationUuid -> id)
        }

      case None =>
        logger.warn(s"Could not retrieve from cache, for eori ${request.user.eori}, the declaration with id $rejectedParentId")
        Future.successful(Results.Redirect(RootController.displayPage))
    }

  private def findOrCreateDraftForRejection(rejectedParentId: String, redirect: Result)(implicit request: AuthenticatedRequest[_]): Future[Result] =
    cacheService.get(rejectedParentId).flatMap {
      case Some(srcDec) =>
        customsDeclareExportsConnector.findOrCreateDraftForRejection(rejectedParentId, request.user.eori, srcDec).map { id =>
          redirect.addingToSession(declarationUuid -> id)
        }

      case None =>
        logger.warn(s"Could not retrieve source declaration from cache, for eori ${request.user.eori}, the declaration with id $rejectedParentId")
        Future.successful(Results.Redirect(RootController.displayPage))
    }
}
