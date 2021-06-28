/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import config.PaginationConfig
import config.featureFlags.QueryNotificationMessageConfig
import connectors.CustomsDeclareExportsConnector
import connectors.exchange.ExportsDeclarationExchange
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.util.SubmissionDisplayHelper
import models._
import models.Mode.ErrorFix
import models.requests.ExportsSessionKeys
import models.responses.FlashKeys
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.model.FieldNamePointer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{declaration_information, new_declaration_information, submissions}

class SubmissionsController @Inject()(
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  queryNotificationMessageConfig: QueryNotificationMessageConfig,
  submissionsPage: submissions,
  declarationInformationPage: declaration_information,
  newDeclarationInformationPage: new_declaration_information
)(implicit ec: ExecutionContext, paginationConfig: PaginationConfig)
    extends FrontendController(mcc) with I18nSupport {

  def displayListOfSubmissions(submissionsPages: SubmissionsPages = SubmissionsPages()): Action[AnyContent] =
    (authenticate andThen verifyEmail).async { implicit request =>
      for {
        submissions <- customsDeclareExportsConnector.fetchSubmissions()
        notifications <- customsDeclareExportsConnector.fetchNotifications()

        result = SubmissionDisplayHelper.createSubmissionsWithSortedNotificationsMap(submissions, notifications)

      } yield Ok(submissionsPage(SubmissionsPagesElements(result, submissionsPages))).removingFromSession(ExportsSessionKeys.declarationId)
    }

  def displayDeclarationWithNotifications(id: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    if (queryNotificationMessageConfig.isQueryNotificationMessageEnabled) displayNewInformationPage()
    else displayOldInformationPage(id)
  }

  private def displayNewInformationPage()(implicit request: Request[_]): Future[Result] = Future.successful(Ok(newDeclarationInformationPage()))

  private def displayOldInformationPage(submissionId: String)(implicit request: Request[_]): Future[Result] =
    customsDeclareExportsConnector.findSubmission(submissionId).flatMap {
      case Some(submission) =>
        customsDeclareExportsConnector.findNotifications(submissionId).map { notifications =>
          Ok(declarationInformationPage(submission, notifications))
        }
      case _ => Future.successful(Redirect(routes.SubmissionsController.displayListOfSubmissions()))
    }

  def amend(id: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    val redirect = Redirect(controllers.declaration.routes.SummaryController.displayPage(Mode.Amend))

    val actualDeclaration: Future[Option[ExportsDeclaration]] = request.declarationId.map { decId =>
      customsDeclareExportsConnector.findDeclaration(decId)
    }.getOrElse(Future.successful(None))

    actualDeclaration.flatMap {
      case Some(_) => Future.successful(Redirect(controllers.declaration.routes.SummaryController.displayPage(Mode.Amend)))
      case _       => createNewDraftDec(id, redirect)
    }
  }

  def viewDeclaration(id: String): Action[AnyContent] = (authenticate andThen verifyEmail) { implicit request =>
    Redirect(controllers.declaration.routes.SubmittedDeclarationController.displayPage()).addingToSession(ExportsSessionKeys.declarationId -> id)
  }

  def amendErrors(id: String, redirectUrl: String, pattern: String, messageKey: String): Action[AnyContent] =
    (authenticate andThen verifyEmail).async { implicit request =>
      val redirectUrlWithMode = redirectUrl + ErrorFix.queryParameter
      val fieldName = FieldNamePointer.getFieldName(pattern)
      val flashData = fieldName match {
        case Some(name) if messageKey.nonEmpty => Map(FlashKeys.fieldName -> name, FlashKeys.errorMessage -> messageKey)
        case Some(name)                        => Map(FlashKeys.fieldName -> name)
        case None if messageKey.nonEmpty       => Map(FlashKeys.errorMessage -> messageKey)
        case _                                 => Map.empty[String, String]
      }
      val redirect = Redirect(redirectUrlWithMode).flashing(Flash(flashData))

      val actualDeclaration: Future[Option[ExportsDeclaration]] = request.declarationId.map { decId =>
        customsDeclareExportsConnector.findDeclaration(decId)
      }.getOrElse(Future.successful(None))

      actualDeclaration.flatMap {
        case Some(dec) if dec.sourceId.contains(id) => Future.successful(redirect)
        case _                                      => createNewDraftDec(id, redirect)
      }
    }

  private def createNewDraftDec(id: String, redirect: Result)(implicit request: WrappedRequest[AnyContent]) =
    customsDeclareExportsConnector.findDeclaration(id) flatMap {
      case Some(declaration) =>
        val amendedDeclaration = ExportsDeclarationExchange.withoutId(declaration.amend())
        customsDeclareExportsConnector
          .createDeclaration(amendedDeclaration)
          .map { created =>
            redirect.addingToSession(ExportsSessionKeys.declarationId -> created.id)
          }
      case _ => Future.successful(Redirect(routes.SubmissionsController.displayListOfSubmissions()))
    }
}
