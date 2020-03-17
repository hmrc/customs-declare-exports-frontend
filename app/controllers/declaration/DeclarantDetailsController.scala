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
import forms.declaration.DeclarantDetails
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.declarant_details

import scala.concurrent.{ExecutionContext, Future}

class DeclarantDetailsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarantDetailsPage: declarant_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.parties.declarantDetails match {
      case Some(data) => Ok(declarantDetailsPage(mode, DeclarantDetails.form().fill(data)))
      case _          => Ok(declarantDetailsPage(mode, DeclarantDetails.form()))
    }
  }

  def saveAddress(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    DeclarantDetails
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[DeclarantDetails]) => Future.successful(BadRequest(declarantDetailsPage(mode, formWithErrors))),
        form =>
          updateCache(form)
            .map(_ => navigator.continueTo(mode, controllers.declaration.routes.ExporterDetailsController.displayPage))
      )
  }

  private def updateCache(formData: DeclarantDetails)(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedParties = model.parties.copy(declarantDetails = Some(formData))
      model.copy(parties = updatedParties)
    })
}
