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

package controllers.declaration

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction, VerifiedEmailAction}
import controllers.declaration.SummaryController.{continuePlaceholder, lrnDuplicateError}
import controllers.routes.SavedDeclarationsController
import forms.{Lrn, LrnValidator}
import handlers.ErrorHandler
import models.declaration.submissions.EnhancedStatus.ERRORS
import models.requests.JourneyRequest
import models.requests.SessionHelper._
import play.api.Logging
import play.api.data.FormError
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.dashboard.DashboardHelper.toDashboard
import views.helpers.ActionItemBuilder.lastUrlPlaceholder
import views.html.declaration.amendments.amendment_summary
import views.html.declaration.summary._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SummaryController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  amendment_summary: amendment_summary,
  normalSummaryPage: normal_summary_page,
  summaryPageNoData: summary_page_no_data,
  lrnValidator: LrnValidator
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport with Logging with ModelCacheable {

  val displayPage: Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType).async { implicit request =>
    if (request.cacheModel.isAmendmentDraft) amendmentSummaryPage()
    else if (request.cacheModel.declarationMeta.summaryWasVisited.contains(true)) continueToDisplayPage
    else
      updateDeclarationFromRequest(declaration =>
        declaration.copy(declarationMeta = declaration.declarationMeta.copy(summaryWasVisited = Some(true)))
      ).flatMap(_ => continueToDisplayPage)
  }

  private def amendmentSummaryPage()(implicit request: JourneyRequest[_]): Future[Result] =
    getValue(submissionUuid).fold {
      errorHandler.internalError("Session on /saved-summary (for draft amendment) does not include 'submissionUuid'??")
    } { submissionId =>
      val html = Html(
        amendment_summary(submissionId)
          .toString()
          .replace(s"?$lastUrlPlaceholder", "")
      )
      Future.successful(Ok(html))
    }

  private def continueToDisplayPage(implicit request: JourneyRequest[_]): Future[Result] = {
    val hasMandatoryData = request.cacheModel.consignmentReferences.exists(refs => refs.ducr.nonEmpty && refs.lrn.nonEmpty)
    if (hasMandatoryData) displaySummaryPage()
    else Future.successful(Ok(summaryPageNoData()).removingFromSession(errorFixModeSessionKey))
  }

  private def displaySummaryPage()(implicit request: JourneyRequest[_]): Future[Result] = {
    val maybeLrn = request.cacheModel.lrn.map(Lrn(_))
    isLrnADuplicate(maybeLrn) map { isDuplicate =>
      val isDeclarationWithErrors = request.cacheModel.declarationMeta.parentDeclarationEnhancedStatus.contains(ERRORS)
      val backlink = if (isDeclarationWithErrors) toDashboard else SavedDeclarationsController.displayDeclarations()

      val result = if (isDuplicate) normalSummaryPage(backlink, Seq(lrnDuplicateError)) else summaryPageWithPlaceholdersReplaced(backlink)
      Ok(result).removingFromSession(errorFixModeSessionKey)
    }
  }

  private def isLrnADuplicate(lrn: Option[Lrn])(implicit hc: HeaderCarrier): Future[Boolean] =
    lrn.fold(Future.successful(false))(lrnValidator.hasBeenSubmittedInThePast48Hours)

  private val hrefSource = """href="/customs-declare-exports/declaration/.+\?"""
  private val hrefDest = s"""href="$continuePlaceholder""""

  private def summaryPageWithPlaceholdersReplaced(backlink: Call)(implicit request: JourneyRequest[_]): Html = {
    val page = normalSummaryPage(backlink, Seq.empty, Some(continuePlaceholder)).toString
    val hrefSourceRegex = s"""$hrefSource$lastUrlPlaceholder"""".r
    val finalPage = hrefSourceRegex.findAllIn(page).toList.lastOption.fold(page) { lastChangeLink =>
      page
        .replace(s"?$lastUrlPlaceholder", "")
        .replaceFirst(hrefDest, lastChangeLink.replace(s"?$lastUrlPlaceholder", ""))
    }
    Html(finalPage)
  }
}

object SummaryController {

  val lrnDuplicateError: FormError = FormError("lrn", "declaration.consignmentReferences.lrn.error.notExpiredYet")
  val continuePlaceholder = "continue-saved-declaration"
}
