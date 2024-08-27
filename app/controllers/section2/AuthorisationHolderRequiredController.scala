/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section2

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.helpers.AuthorisationHolderHelper.{authorisationHolders, userCanLandOnIsAuthRequiredPage}
import controllers.navigation.Navigator
import controllers.section2.routes.{AuthorisationHolderAddController, AuthorisationHolderSummaryController}
import controllers.summary.routes.SectionSummaryController
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.ExportsDeclaration
import models.declaration.AuthorisationHolders
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section2.authorisationHolder.authorisation_holder_required

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisationHolderRequiredController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  authorisationHolderRequired: authorisation_holder_required
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (authorisationHolders.nonEmpty) navigator.continueTo(AuthorisationHolderSummaryController.displayPage)
    else if (userCanLandOnIsAuthRequiredPage(request.cacheModel)) Ok(authorisationHolderRequired(formWithPreviousAnswer.withSubmissionErrors))
    else navigator.continueTo(AuthorisationHolderAddController.displayPage)
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(authorisationHolderRequired(formWithErrors))),
        validYesNo => updateCache(Some(validYesNo)).map(_ => navigator.continueTo(nextPage(validYesNo)))
      )
  }

  private val form: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.authorisationHolderRequired.empty")

  private def formWithPreviousAnswer(implicit request: JourneyRequest[AnyContent]): Form[YesNoAnswer] =
    request.cacheModel.parties.declarationHoldersData.flatMap(_.isRequired) match {
      case Some(answer) => form.fill(answer)
      case _            => form
    }

  private def nextPage(yesNoAnswer: YesNoAnswer): Call =
    yesNoAnswer.answer match {
      case YesNoAnswers.yes => AuthorisationHolderAddController.displayPage
      case YesNoAnswers.no  => SectionSummaryController.displayPage(2)
    }

  private def updateCache(yesNoAnswer: Option[YesNoAnswer])(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      val holders = if (yesNoAnswer == YesNoAnswer.Yes) authorisationHolders else Seq.empty
      val holdersData = Some(AuthorisationHolders(holders, yesNoAnswer))
      declaration.copy(parties = declaration.parties.copy(declarationHoldersData = holdersData))
    }
}
