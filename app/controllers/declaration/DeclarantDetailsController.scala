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
import controllers.declaration.routes.{ConsignmentReferencesController, DeclarantExporterController, DucrChoiceController, NotEligibleController}
import controllers.navigation.Navigator
import forms.common.Eori
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.DeclarantEoriConfirmation.form
import forms.declaration.{DeclarantDetails, DeclarantEoriConfirmation, EntityDetails}
import models.requests.ExportsSessionKeys._
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarant_details

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarantDetailsController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarantDetailsPage: declarant_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyAction) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.parties.declarantDetails match {
      case Some(_) => Ok(declarantDetailsPage(frm.fill(DeclarantEoriConfirmation(YesNoAnswers.yes))))
      case _       => Ok(declarantDetailsPage(frm))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyAction).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(declarantDetailsPage(formWithErrors))),
        validForm =>
          if (validForm.answer == YesNoAnswers.yes)
            updateCache(DeclarantDetails(EntityDetails(Some(Eori(request.eori)), None))).map(_ => navigator.continueTo(nextPage))
          else
            Future(Redirect(NotEligibleController.displayNotDeclarant).removingFromSession(declarationId, errorFixModeSessionKey))
      )
  }

  private def updateCache(declarant: DeclarantDetails)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.copy(parties = model.parties.copy(declarantDetails = Some(declarant))))

  private def nextPage(implicit request: JourneyRequest[_]): Call = request.declarationType match {
    case DeclarationType.CLEARANCE     => DeclarantExporterController.displayPage
    case DeclarationType.SUPPLEMENTARY => ConsignmentReferencesController.displayPage
    case _                             => DucrChoiceController.displayPage
  }
}
