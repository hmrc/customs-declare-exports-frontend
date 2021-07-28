/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.declaration.AuthorisationProcedureCodeChoice
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.STANDARD_PRE_LODGED
import models.DeclarationType._
import models.declaration.AuthorisationProcedureCode._
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
        navigator.continueTo(mode, routes.DeclarationHolderRequiredController.displayPage)
      case OCCASIONAL =>
        navigator.continueTo(mode, routes.DeclarationHolderRequiredController.displayPage)
      case _ =>
        val form = AuthorisationProcedureCodeChoice.form().withSubmissionErrors()
        request.cacheModel.parties.authorisationProcedureCodeChoice match {
          case Some(data) => Ok(authorisationProcedureCodeChoice(form.fill(data), mode))
          case _          => Ok(authorisationProcedureCodeChoice(form, mode))
        }
    }
  }

  private val validTypes = Seq(STANDARD, SUPPLEMENTARY, SIMPLIFIED, CLEARANCE)

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    AuthorisationProcedureCodeChoice
      .form()
      .bindFromRequest()
      .fold(
        errors => Future.successful(BadRequest(authorisationProcedureCodeChoice(errors, mode))),
        updateCache(_).map(exportsDeclarationUpdated => navigator.continueTo(mode, nextPage(exportsDeclarationUpdated)))
      )
  }

  private def updateCache(
    choice: AuthorisationProcedureCodeChoice
  )(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(_.updateAuthorisationProcedureCodeChoice(choice))

  private def nextPage(exportsDeclarationOpt: Option[ExportsDeclaration]): Mode => Call =
    (for {
      exportsDeclaration <- exportsDeclarationOpt
      authProcedureCodeChoice <- exportsDeclaration.parties.authorisationProcedureCodeChoice.map(_.code)

    } yield (exportsDeclaration.`type`, exportsDeclaration.additionalDeclarationType, authProcedureCodeChoice)) match {
      case Some((STANDARD, Some(STANDARD_PRE_LODGED), Code1040 | CodeOther)) => routes.DeclarationHolderRequiredController.displayPage
      case _                                                                 => routes.DeclarationHolderSummaryController.displayPage
    }

}
