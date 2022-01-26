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

import scala.concurrent.{ExecutionContext, Future}

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.routes._
import controllers.helpers.DeclarationHolderHelper.declarationHolders
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import javax.inject.Inject
import models.declaration.DeclarationHoldersData
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_required

class DeclarationHolderRequiredController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarationHolderRequired: declaration_holder_required
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (declarationHolders.isEmpty) Ok(declarationHolderRequired(mode, formWithPreviousAnswer.withSubmissionErrors))
    else navigator.continueTo(mode, DeclarationHolderSummaryController.displayPage(_))
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form.bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(declarationHolderRequired(mode, formWithErrors))),
        validYesNo => updateCache(validYesNo).map(_ => navigator.continueTo(mode, nextPage(validYesNo)))
      )
  }

  private val form: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.declarationHolderRequired.empty")

  private def formWithPreviousAnswer(implicit request: JourneyRequest[AnyContent]): Form[YesNoAnswer] =
    request.cacheModel.parties.declarationHoldersData.flatMap(_.isRequired) match {
      case Some(answer) => form.fill(answer)
      case _            => form
    }

  private def nextPage(yesNoAnswer: YesNoAnswer): Mode => Call =
    yesNoAnswer.answer match {
      case YesNoAnswers.yes => DeclarationHolderAddController.displayPage
      case YesNoAnswers.no  => DestinationCountryController.displayPage
    }

  private def updateCache(yesNoAnswer: YesNoAnswer)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => {
      val declarationHoldersData = DeclarationHoldersData(declarationHolders, Some(yesNoAnswer))
      val updatedParties = model.parties.copy(declarationHoldersData = Some(declarationHoldersData))
      model.copy(parties = updatedParties)
    })
}
