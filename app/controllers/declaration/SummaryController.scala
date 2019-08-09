/*
 * Copyright 2019 HM Revenue & Customs
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

import controllers.actions.{AuthAction, JourneyAction}
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.SupplementaryDeclarationData
import models.requests.JourneyRequest
import models.{DeclarationStatus, ExportsDeclaration}
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services._
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
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
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  private val logger = Logger(this.getClass())

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (containsMandatoryData(request.cacheModel)) {
      Ok(summaryPage(SupplementaryDeclarationData(request.cacheModel)))
    } else {
      Ok(summaryPageNoData())
    }
  }

  private def containsMandatoryData(data: ExportsDeclaration): Boolean =
    data.consignmentReferences.exists(references => references.lrn.nonEmpty)

  def submitSupplementaryDeclaration(): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      handleDecSubmission(request.cacheModel)

  }

  private def handleDecSubmission(
    exportsCacheModel: ExportsDeclaration
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Result] =
    submissionService.submit(journeySessionId, exportsCacheModel.copy(status = DeclarationStatus.COMPLETE)).map {
      case Some(lrn) =>
        Redirect(controllers.declaration.routes.ConfirmationController.displayPage())
          .flashing(Flash(Map("LRN" -> lrn)))
      case _ => handleError(s"Error from Customs Declarations API")
    }

  private def handleError(logMessage: String)(implicit request: Request[_]): Result = {
    logger.error(logMessage)
    InternalServerError(
      errorHandler.standardErrorTemplate(
        pageTitle = Messages("global.error.title"),
        heading = Messages("global.error.heading"),
        message = Messages("global.error.message")
      )
    )
  }
}
