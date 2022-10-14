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
import controllers.routes.{SavedDeclarationsController, SubmissionsController}
import forms.declaration.LegalDeclaration
import forms.{Lrn, LrnValidator}
import handlers.ErrorHandler
import models.Mode.{Amend, Draft, Normal}
import models.declaration.submissions.EnhancedStatus.ERRORS
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
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.summary._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SummaryController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  submissionService: SubmissionService,
  mcc: MessagesControllerComponents,
  normalSummaryPage: normal_summary_page,
  summaryPageNoData: summary_page_no_data,
  legalDeclarationPage: legal_declaration_page,
  lrnValidator: LrnValidator
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport with Logging with ModelCacheable with WithDefaultFormBinding {

  val form: Form[LegalDeclaration] = LegalDeclaration.form()

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType).async { implicit request =>
    updateDeclarationFromRequest(_.copy(summaryWasVisited = Some(true))).flatMap { _ =>
      if (containsMandatoryData(request.cacheModel, mode)) displaySummaryPage(mode)
      else Future.successful(Ok(summaryPageNoData()))
    }
  }

  def displayDeclarationPage(mode: Mode): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType) { implicit request =>
    mode match {
      case Draft | Normal | Amend => Ok(legalDeclarationPage(form))
      case _                      => handleError("Invalid mode on summary page")
    }
  }

  def submitDeclaration(): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType).async { implicit request =>
    form.bindFromRequest.fold(
      (formWithErrors: Form[LegalDeclaration]) => Future.successful(BadRequest(legalDeclarationPage(formWithErrors))),
      legalDeclaration =>
        submissionService.submit(request.eori, request.cacheModel, legalDeclaration).map {
          case Some(submission) => Redirect(routes.ConfirmationController.displayHoldingPage).withSession(session(submission))
          case _                => handleError(s"Error from Customs Declarations API")
        }
    )
  }

  private def displaySummaryPage(mode: Mode)(implicit request: JourneyRequest[_]): Future[Result] = {
    val maybeLrn = request.cacheModel.lrn.map(Lrn(_))
    val backlink =
      if (request.cacheModel.parentDeclarationEnhancedStatus.contains(ERRORS)) SubmissionsController.displayListOfSubmissions()
      else SavedDeclarationsController.displayDeclarations()
    val duplicateLrnError = Seq(lrnDuplicateError)

    isLrnADuplicate(maybeLrn).map { lrnIsDuplicate =>
      if (lrnIsDuplicate) Ok(normalSummaryPage(mode, backlink, duplicateLrnError))
      else Ok(amendDraftSummaryPage(mode, backlink))
    }
  }

  private val hrefSourcePattern = """href="/customs-declare-exports/declaration/.+\?mode=Change"""".r
  private val hrefDestPattern = s"""href="$continuePlaceholder""""

  private def amendDraftSummaryPage(mode: Mode, backlink: Call)(implicit request: JourneyRequest[_]): Html = {
    val page = normalSummaryPage(mode, backlink, Seq.empty, Some(continuePlaceholder)).toString
    hrefSourcePattern.findAllIn(page).toList.lastOption.fold(Html(page)) { lastChangeLink =>
      Html(page.replaceFirst(hrefDestPattern, lastChangeLink))
    }
  }

  private def containsMandatoryData(declaration: ExportsDeclaration, mode: Mode): Boolean =
    mode.equals(Draft) || declaration.consignmentReferences.exists(references => references.lrn.nonEmpty)

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
