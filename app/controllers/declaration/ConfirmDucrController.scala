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

import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import forms.Ducr
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.IntermediaryConsignmentReferences
import handlers.ErrorHandler
import models.DeclarationType.{allDeclarationTypesExcluding, SUPPLEMENTARY}
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.i18n.Lang.logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.confirm_ducr

import java.time.ZoneId
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmDucrController @Inject() (
  authorise: AuthAction,
  getJourney: JourneyAction,
  navigator: Navigator,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  override val exportsCacheService: ExportsCacheService,
  confirmDucrPage: confirm_ducr
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithDefaultFormBinding with ModelCacheable with SubmissionErrors {

  def displayPage(): Action[AnyContent] = (authorise andThen getJourney(allDeclarationTypesExcluding(SUPPLEMENTARY))).async { implicit request =>
    request.cacheModel.traderReference.fold {
      logger.warn("No trader reference found in cache to generate DUCR!")
      errorHandler.displayErrorPage()
    }(_ => Future.successful(Ok(confirmDucrPage(form.withSubmissionErrors(), generatedDucr))))
  }

  def submitForm(): Action[AnyContent] = (authorise andThen getJourney(allDeclarationTypesExcluding(SUPPLEMENTARY))).async { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(confirmDucrPage(formWithErrors, generatedDucr))),
      {
        case YesNoAnswer(YesNoAnswers.yes) => updateCache.map(_ => navigator.continueTo(???))
        case _                             => Future.successful(navigator.continueTo(routes.DucrEntryController.displayPage))
      }
    )
  }

  private def form: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.confirmDucr.error.empty")

  private def generatedDucr(implicit request: JourneyRequest[_]): Ducr = {
    val lastDigitOfYear = request.cacheModel.createdDateTime.atZone(ZoneId.of("Europe/London")).getYear.toString.last
    val eori = request.eori.toUpperCase
    val tradeRef = request.cacheModel.traderReference.get.value

    Ducr(lastDigitOfYear + "GB" + eori.takeRight(12) + "-" + tradeRef)
  }

  private def updateCache(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] = {
    val existingTraderReference = request.cacheModel.intermediaryConsignmentReferences.flatMap(_.traderReference)
    updateDeclarationFromRequest(
      _.copy(intermediaryConsignmentReferences = Some(IntermediaryConsignmentReferences(Some(generatedDucr), existingTraderReference)))
    )
  }
}
