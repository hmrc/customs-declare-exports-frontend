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
import controllers.declaration.routes.{DeclarationHolderRequiredController, DeclarationHolderSummaryController}
import controllers.navigation.Navigator
import forms.declaration.AuthorisationProcedureCodeChoice
import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1040, ChoiceOthers}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.STANDARD_PRE_LODGED
import models.DeclarationType._
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.authorisation_procedure_code_choice

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisationProcedureCodeChoiceController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  authorisationProcedureCodeChoice: authorisation_procedure_code_choice
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.declarationType match {
      case CLEARANCE if request.cacheModel.isNotEntryIntoDeclarantsRecords =>
        navigator.continueTo(mode, DeclarationHolderRequiredController.displayPage)

      case OCCASIONAL =>
        navigator.continueTo(mode, DeclarationHolderRequiredController.displayPage)

      case _ =>
        val form = AuthorisationProcedureCodeChoice.form.withSubmissionErrors
        request.cacheModel.parties.authorisationProcedureCodeChoice match {
          case Some(data) => Ok(authorisationProcedureCodeChoice(form.fill(data), mode))
          case _          => Ok(authorisationProcedureCodeChoice(form, mode))
        }
    }
  }

  private val validTypes = Seq(STANDARD, SUPPLEMENTARY, SIMPLIFIED, CLEARANCE)

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    AuthorisationProcedureCodeChoice
      .form
      .bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(authorisationProcedureCodeChoice(formWithErrors, mode))),
        updateCache(_).map(exportsDeclarationUpdated => navigator.continueTo(mode, nextPage(exportsDeclarationUpdated)))
      )
  }

  private def nextPage(declaration: ExportsDeclaration): Mode => Call =
    (declaration.`type`, declaration.additionalDeclarationType, declaration.parties.authorisationProcedureCodeChoice) match {
      case (STANDARD, Some(STANDARD_PRE_LODGED), Choice1040 | ChoiceOthers) => DeclarationHolderRequiredController.displayPage
      case _                                                                => DeclarationHolderSummaryController.displayPage
    }

  private def updateCache(
    choice: AuthorisationProcedureCodeChoice
  )(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updateAuthorisationProcedureCodeChoice(choice))
}
