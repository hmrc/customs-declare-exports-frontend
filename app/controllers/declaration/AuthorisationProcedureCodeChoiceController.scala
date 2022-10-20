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
import controllers.declaration.routes.DeclarationHolderRequiredController
import controllers.helpers.DeclarationHolderHelper.userCanLandOnIsAuthRequiredPage
import controllers.navigation.Navigator
import forms.declaration.AuthorisationProcedureCodeChoice
import models.DeclarationType._
import models.declaration.DeclarationHoldersData
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.authorisation_procedure_code_choice

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisationProcedureCodeChoiceController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  authorisationProcedureCodeChoice: authorisation_procedure_code_choice
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.declarationType match {
      case CLEARANCE if request.cacheModel.isNotEntryIntoDeclarantsRecords =>
        navigator.continueTo(DeclarationHolderRequiredController.displayPage)

      case OCCASIONAL =>
        navigator.continueTo(DeclarationHolderRequiredController.displayPage)

      case _ =>
        val form = AuthorisationProcedureCodeChoice.form.withSubmissionErrors
        request.cacheModel.parties.authorisationProcedureCodeChoice match {
          case Some(data) => Ok(authorisationProcedureCodeChoice(form.fill(data)))
          case _          => Ok(authorisationProcedureCodeChoice(form))
        }
    }
  }

  private val validTypes = Seq(STANDARD, SUPPLEMENTARY, SIMPLIFIED, CLEARANCE)

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    AuthorisationProcedureCodeChoice.form.bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(authorisationProcedureCodeChoice(formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(DeclarationHolderRequiredController.displayPage))
      )
  }

  private def updateCache(choice: AuthorisationProcedureCodeChoice)(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      def holdersData(maybeHoldersData: Option[DeclarationHoldersData]): Option[DeclarationHoldersData] =
        if (userCanLandOnIsAuthRequiredPage(declaration)) maybeHoldersData else maybeHoldersData.map(_.copy(isRequired = None))

      declaration.copy(parties =
        declaration.parties
          .copy(authorisationProcedureCodeChoice = Some(choice), declarationHoldersData = holdersData(declaration.parties.declarationHoldersData))
      )
    }
}
