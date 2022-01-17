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
import forms.declaration.LegalDeclaration
import forms.{Lrn, LrnValidator}
import handlers.ErrorHandler
import models.declaration.submissions.Submission
import models.requests.ExportsSessionKeys._
import models.{ExportsDeclaration, Mode}
import play.api.Logging
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services._
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

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType) { implicit request =>
    if (containsMandatoryData(request.cacheModel, mode)) {

      val containersSectionAnswered = request.cacheModel.readyForSubmission

      mode match {
        case Mode.Normal | Mode.Draft if containersSectionAnswered => Ok(normalSummaryPage(mode))
        case Mode.Normal | Mode.Draft                              => Ok(draftSummaryPage())
        case Mode.Amend                                            => Ok(amendSummaryPage())
        case _                                                     => handleError("Invalid mode on summary page")
      }
    } else {
      Ok(summaryPageNoData())
    }
  }

  def displayDeclarationPage(mode: Mode): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType) { implicit request =>
    mode match {
      case Mode.Draft | Mode.Normal => Ok(legalDeclarationPage(form, mode))
      case Mode.Amend               => InternalServerError
      case _                        => handleError("Invalid mode on summary page")
    }

  }

  def submitDeclaration(mode: Mode): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType).async { implicit request =>
    def submit(form: Form[LegalDeclaration]): Future[Result] =
      form.bindFromRequest.fold(
        (formWithErrors: Form[LegalDeclaration]) => Future.successful(BadRequest(legalDeclarationPage(formWithErrors, mode))),
        legalDeclaration => {
          submissionService.submit(request.eori, request.cacheModel, legalDeclaration).map {
            case Some(submission) =>
              Redirect(routes.ConfirmationController.displayHoldingPage).withSession(session(submission))
            case _ => handleError(s"Error from Customs Declarations API")
          }
        }
      )

    val maybeLrn = request.cacheModel.lrn.map(Lrn(_))

    isLrnADuplicate(maybeLrn).flatMap {
      case true => {
        val allErrors = form.errors ++ Seq(FormError("lrn", "declaration.consignmentReferences.lrn.error.notExpiredYet"))
        submit(form.copy(errors = allErrors))
      }
      case _ => submit(form)
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

  private def session(submission: Submission)(implicit request: Request[_]): Session =
    request.session - declarationId +
      (submissionId -> submission.uuid) +
      (submissionDucr -> submission.ducr.fold("")(identity)) +
      (submissionLrn -> submission.lrn)
}
