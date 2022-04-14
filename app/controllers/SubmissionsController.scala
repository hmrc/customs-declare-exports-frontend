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
import controllers.helpers.SubmissionDisplayHelper
import models.Mode.ErrorFix
import models._
import models.requests.ExportsSessionKeys
import models.responses.FlashKeys
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.model.FieldNamePointer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.summary.submitted_declaration_page
import views.html.submissions

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionsController @Inject()(
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  submissionsPage: submissions,
  submittedDeclarationPage: submitted_declaration_page
)(implicit ec: ExecutionContext, paginationConfig: PaginationConfig)
    extends FrontendController(mcc) with I18nSupport {

  def displayListOfSubmissions(submissionsPages: SubmissionsPages = SubmissionsPages()): Action[AnyContent] =
    (authenticate andThen verifyEmail).async { implicit request =>
      for {
        submissions <- customsDeclareExportsConnector.fetchSubmissions
        notifications <- customsDeclareExportsConnector.fetchNotifications()

        result = SubmissionDisplayHelper.createSubmissionsWithSortedNotificationsMap(submissions, notifications)

      } yield Ok(submissionsPage(SubmissionsPagesElements(result, submissionsPages))).removingFromSession(ExportsSessionKeys.declarationId)
    }

  def amend(id: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    val redirect = Redirect(SummaryController.displayPageOnAmend)

    val actualDeclaration: Future[Option[ExportsDeclaration]] = request.declarationId.map { decId =>
      customsDeclareExportsConnector.findDeclaration(decId)
    }.getOrElse(Future.successful(None))

    actualDeclaration.flatMap {
      case Some(_) => Future.successful(Redirect(SummaryController.displayPageOnAmend))
      case _       => createDraftDeclaration(id, redirect)
    }
  }

  def viewDeclaration(id: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    customsDeclareExportsConnector.findDeclaration(id).flatMap {
      case Some(declaration) =>
        customsDeclareExportsConnector.findNotifications(id).map { notifications =>
          Ok(submittedDeclarationPage(notifications, declaration))
        }

      case None => Future.successful(Redirect(routes.SubmissionsController.displayListOfSubmissions()))
    }
  }

  def amendErrors(id: String, redirectUrl: String, pattern: String, message: String): Action[AnyContent] =
    (authenticate andThen verifyEmail).async { implicit request =>
      val redirectUrlWithMode = redirectUrl + ErrorFix.queryParameter
      val fieldName = FieldNamePointer.getFieldName(pattern)
      val flashData = fieldName match {
        case Some(name) if message.nonEmpty => Map(FlashKeys.fieldName -> name, FlashKeys.errorMessage -> message)
        case Some(name)                     => Map(FlashKeys.fieldName -> name)
        case None if message.nonEmpty       => Map(FlashKeys.errorMessage -> message)
        case _                              => Map.empty[String, String]
      }
      val redirect = Redirect(redirectUrlWithMode).flashing(Flash(flashData))

      val actualDeclaration: Future[Option[ExportsDeclaration]] = request.declarationId.map { decId =>
        customsDeclareExportsConnector.findDeclaration(decId)
      }.getOrElse(Future.successful(None))

      actualDeclaration.flatMap {
        case Some(dec) if dec.sourceId.contains(id) => Future.successful(redirect)
        case _                                      => createDraftDeclaration(id, redirect)
      }
    }

  private def createDraftDeclaration(id: String, redirect: Result)(implicit request: WrappedRequest[AnyContent]): Future[Result] =
    customsDeclareExportsConnector.findDeclaration(id) flatMap {
      case Some(declaration) =>
        customsDeclareExportsConnector
          .createDeclaration(declaration.asDraft)
          .map { draftDeclaration =>
            redirect.addingToSession(ExportsSessionKeys.declarationId -> draftDeclaration.id)
          }

      case _ => Future.successful(Redirect(routes.SubmissionsController.displayListOfSubmissions()))
    }
}
