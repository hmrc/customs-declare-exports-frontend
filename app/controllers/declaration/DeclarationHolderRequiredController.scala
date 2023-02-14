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

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.routes._
import controllers.helpers.DeclarationHolderHelper.{declarationHolders, userCanLandOnIsAuthRequiredPage}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.declaration.DeclarationHoldersData
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_required

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationHolderRequiredController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarationHolderRequired: declaration_holder_required
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (declarationHolders.nonEmpty) navigator.continueTo(DeclarationHolderSummaryController.displayPage)
    else if (userCanLandOnIsAuthRequiredPage(request.cacheModel)) Ok(declarationHolderRequired(formWithPreviousAnswer.withSubmissionErrors))
    else navigator.continueTo(DeclarationHolderAddController.displayPage)
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(declarationHolderRequired(formWithErrors))),
        validYesNo => updateCache(Some(validYesNo)).map(_ => navigator.continueTo(nextPage(validYesNo)))
      )
  }

  private val form: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.declarationHolderRequired.empty")

  private def formWithPreviousAnswer(implicit request: JourneyRequest[AnyContent]): Form[YesNoAnswer] =
    request.cacheModel.parties.declarationHoldersData.flatMap(_.isRequired) match {
      case Some(answer) => form.fill(answer)
      case _            => form
    }

  private def nextPage(yesNoAnswer: YesNoAnswer): Call =
    yesNoAnswer.answer match {
      case YesNoAnswers.yes => DeclarationHolderAddController.displayPage
      case YesNoAnswers.no  => DestinationCountryController.displayPage
    }

  private def updateCache(yesNoAnswer: Option[YesNoAnswer])(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      val holders = if (yesNoAnswer == YesNoAnswer.Yes) declarationHolders else Seq.empty
      val holdersData = Some(DeclarationHoldersData(holders, yesNoAnswer))
      declaration.copy(parties = declaration.parties.copy(declarationHoldersData = holdersData))
    }
}
