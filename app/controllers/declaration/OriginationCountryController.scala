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
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration.destinationCountries.DestinationCountries.OriginationCountryPage
import javax.inject.{Inject, Singleton}
import models.Mode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.destinationCountries.origination_country

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OriginationCountryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  originationCountryPage: origination_country
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val form = request.cacheModel.locations.originationCountry match {
      case Some(originateCountry) =>
        DestinationCountries.form(OriginationCountryPage).fill(originateCountry)
      case None => DestinationCountries.form(OriginationCountryPage)
    }

    Ok(originationCountryPage(mode, form))
  }

  def submit(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    DestinationCountries
      .form(OriginationCountryPage)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(originationCountryPage(mode, formWithErrors))),
        validCountry =>
          updateExportsDeclarationSyncDirect(_.updateOriginationCountry(validCountry)).map { _ =>
            navigator.continueTo(mode, controllers.declaration.routes.DestinationCountryController.displayPage)
        }
      )
  }
}
