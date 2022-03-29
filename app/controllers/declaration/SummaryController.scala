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

package controllers.declaration

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction, VerifiedEmailAction}
import controllers.declaration.SummaryController.{continuePlaceholder, lrnDuplicateError}
import controllers.declaration.routes.TransportContainerController
import controllers.routes.SavedDeclarationsController
import forms.declaration.LegalDeclaration
import forms.{Lrn, LrnValidator}
import handlers.ErrorHandler
import models.declaration.submissions.Submission
import models.requests.ExportsSessionKeys._
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.Logging
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import services.SubmissionService
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.summary._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SummaryController @Inject()(
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  submissionService: SubmissionService,
  mcc: MessagesControllerComponents,
  normalSummaryPage: normal_summary_page,
  amendSummaryPage: amend_summary_page,
  draftSummaryPage: draft_summary_page,
  summaryPageNoData: summary_page_no_data,
  legalDeclarationPage: legal_declaration_page,
  lrnValidator: LrnValidator
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport with Logging with ModelCacheable {

  val form: Form[LegalDeclaration] = LegalDeclaration.form()

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType).async { implicit request =>
    if (containsMandatoryData(request.cacheModel, mode)) displaySummaryPage(mode)
    else Future.successful(Ok(summaryPageNoData()))
  }

  def displayDeclarationPage(mode: Mode): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType) { implicit request =>
    mode match {
      case Mode.Draft | Mode.Normal | Mode.Amend => Ok(legalDeclarationPage(form, mode))
      case _                                     => handleError("Invalid mode on summary page")
    }
  }

  def submitDeclaration(mode: Mode): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType).async { implicit request =>
    form.bindFromRequest.fold(
      (formWithErrors: Form[LegalDeclaration]) => Future.successful(BadRequest(legalDeclarationPage(formWithErrors, mode))),
      legalDeclaration => {
        submissionService.submit(request.eori, request.cacheModel, legalDeclaration).map {
          case Some(submission) => Redirect(routes.ConfirmationController.displayHoldingPage).withSession(session(submission))
          case _                => handleError(s"Error from Customs Declarations API")
        }
      }
    )
  }

  private def displaySummaryPage(mode: Mode)(implicit request: JourneyRequest[_]): Future[Result] = {

    val readyForSubmission = request.cacheModel.readyForSubmission.contains(true)
    val maybeLrn = request.cacheModel.lrn.map(Lrn(_))

    isLrnADuplicate(maybeLrn).map { lrnIsDuplicate =>
      (mode, lrnIsDuplicate) match {
        case (Mode.Normal, true) if readyForSubmission =>
          Ok(normalSummaryPage(TransportContainerController.displayContainerSummary(Mode.Normal), Seq(lrnDuplicateError)))
        case (Mode.Normal, _) if readyForSubmission =>
          Ok(normalSummaryPage(TransportContainerController.displayContainerSummary(Mode.Normal)))
        case (Mode.Normal, _) =>
          Ok(draftSummaryPage(TransportContainerController.displayContainerSummary(Mode.Normal)))
        case (Mode.Draft, true) if readyForSubmission =>
          Ok(normalSummaryPage(SavedDeclarationsController.displayDeclarations(), Seq(lrnDuplicateError)))
        case (Mode.Draft, _) if readyForSubmission =>
          Ok(normalSummaryPage(SavedDeclarationsController.displayDeclarations()))
        case (Mode.Draft, _) =>
          Ok(amendDraftSummaryPage)
        case (Mode.Amend, true) =>
          Ok(amendSummaryPage(Seq(lrnDuplicateError)))
        case (Mode.Amend, _) =>
          Ok(amendSummaryPage())
        case _ =>
          handleError("Invalid mode on summary page")
      }
    }
  }

  private val hrefSourcePattern = """href="/customs-declare-exports/declaration/.+\?mode=Draft"""".r
  private val hrefDestPattern = s"""href="$continuePlaceholder""""

  private def amendDraftSummaryPage(implicit request: JourneyRequest[_]): Html = {
    val page = draftSummaryPage(SavedDeclarationsController.displayDeclarations(), Some(continuePlaceholder)).toString
    hrefSourcePattern.findAllIn(page).toList.lastOption.fold(Html(page)) { lastChangeLink =>
      Html(page.replaceFirst(hrefDestPattern, lastChangeLink))
    }
  }

  private def containsMandatoryData(declaration: ExportsDeclaration, mode: Mode): Boolean =
    mode.equals(Mode.Draft) || declaration.consignmentReferences.exists(references => references.lrn.nonEmpty)

  private def isLrnADuplicate(lrn: Option[Lrn])(implicit hc: HeaderCarrier): Future[Boolean] =
    lrn.fold(Future.successful(false))(lrnValidator.hasBeenSubmittedInThePast48Hours)

  private def handleError(logMessage: String)(implicit request: Request[_]): Result = {
    logger.error(logMessage)
    InternalServerError(errorHandler.globalErrorPage())
  }

  private def session(submission: Submission)(implicit request: JourneyRequest[_]): Session =
    request.session - declarationId +
      (declarationType -> request.cacheModel.additionalDeclarationType.fold("")(_.toString)) +
      (submissionId -> submission.uuid) +
      (submissionDucr -> submission.ducr.fold("")(identity)) +
      (submissionLrn -> submission.lrn)
}

object SummaryController {

  val lrnDuplicateError: FormError = FormError("lrn", "declaration.consignmentReferences.lrn.error.notExpiredYet")
  val continuePlaceholder = "continue-saved-declaration"
}
