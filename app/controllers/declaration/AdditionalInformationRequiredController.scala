/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.declaration.AdditionalInformation.form
import forms.declaration.DispatchLocation.AllowedDispatchLocations
import forms.declaration.DispatchLocation
import forms.declaration.AdditionalInformationRequired
//import forms.declaration.DispatchLocation.AllowedDispatchLocations
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.additional_information_required

import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationRequiredController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalInfoReqPage: additional_information_required
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId) match {
      case Some(data) => Ok(additionalInfoReqPage(mode, itemId, form().fill(data)))
      case _          => Ok(additionalInfoReqPage(mode, itemId, form()))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    DispatchLocation
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[AdditionalInformationRequiredController]) => Future.successful(BadRequest(additionalInfoReqPage(mode, formWithErrors))),
        validDispatchLocation =>
          updateCache(validDispatchLocation)
            .map(_ => navigator.continueTo(mode, nextPage(validDispatchLocation)))
      )
  }

  private def nextPage(providedAdditionalInfoReq: DispatchLocation): Mode => Call =
    providedAdditionalInfoReq.dispatchLocation match {
      case AllowedDispatchLocations.OutsideEU =>
        controllers.declaration.routes.AdditionalInformationController.displayPage
      case AllowedDispatchLocations.SpecialFiscalTerritory =>
        _ =>
          controllers.declaration.routes.NotEligibleController.displayNotEligible()
    }

  private def updateCache(formData: DispatchLocation)(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => model.copy(dispatchLocation = Some(formData)))

}
j