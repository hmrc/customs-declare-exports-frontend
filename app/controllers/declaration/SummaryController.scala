/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.actions.{AuthAction, JourneyAction}
import forms.declaration.LegalDeclaration
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.ExportsSessionKeys
import models.responses.FlashKeys
import models.{ExportsDeclaration, Mode}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.summary.{summary_page, summary_page_no_data}

import scala.concurrent.{ExecutionContext, Future}

class SummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  submissionService: SubmissionService,
  mcc: MessagesControllerComponents,
  summaryPage: summary_page,
  summaryPageNoData: summary_page_no_data
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  private val logger = Logger(this.getClass)

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (containsMandatoryData(request.cacheModel, mode)) {
      Ok(summaryPage(mode, LegalDeclaration.form()))
    } else {
      Ok(summaryPageNoData())
    }
  }

  private def containsMandatoryData(data: ExportsDeclaration, mode: Mode): Boolean =
    mode.equals(Mode.Draft) || data.consignmentReferences.exists(references => references.lrn.nonEmpty)

  def submitDeclaration(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    LegalDeclaration
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[LegalDeclaration]) => Future.successful(BadRequest(summaryPage(Mode.Normal, formWithErrors))),
        legalDeclaration => {
          submissionService.submit(request.eori, request.cacheModel, legalDeclaration).map {
            case Some(lrn) =>
              Redirect(controllers.declaration.routes.ConfirmationController.displaySubmissionConfirmation())
                .flashing(Flash(Map(FlashKeys.lrn -> lrn, FlashKeys.decType -> request.declarationType.toString())))
                .removingFromSession(ExportsSessionKeys.declarationId)
            case _ => handleError(s"Error from Customs Declarations API")
          }
        }
      )
  }

  private def handleError(logMessage: String)(implicit request: Request[_]): Result = {
    logger.error(logMessage)
    InternalServerError(errorHandler.globalErrorPage())
  }
}
