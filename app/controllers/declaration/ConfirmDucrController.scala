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
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
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
    request.cacheModel.ducr.fold {
      logger.warn("No generated DUCR found in cache!")
      errorHandler.displayErrorPage()
    } { ducr =>
      Future.successful(Ok(confirmDucrPage(form.withSubmissionErrors(), ducr)))
    }
  }

  def submitForm(): Action[AnyContent] = (authorise andThen getJourney(allDeclarationTypesExcluding(SUPPLEMENTARY))).async { implicit request =>
    request.cacheModel.ducr.fold {
      logger.warn("No generated DUCR found in cache!")
      errorHandler.displayErrorPage()
    } { ducr =>
      form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(confirmDucrPage(formWithErrors, ducr))),
        {
          case YesNoAnswer(YesNoAnswers.yes) => Future.successful(navigator.continueTo(routes.LocalReferenceNumberController.displayPage))
          case _                             => updateCache.map(_ => navigator.continueTo(routes.DucrEntryController.displayPage))
        }
      )
    }
  }

  private def form: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.confirmDucr.error.empty")

  private def updateCache(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.copy(consignmentReferences = None))
}
