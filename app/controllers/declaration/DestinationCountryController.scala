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

import connectors.CodeListConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import forms.declaration.countries.Countries
import forms.declaration.countries.Countries.DestinationCountryPage
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.destinationCountries.destination_country

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DestinationCountryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  destinationCountryPage: destination_country
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val form = (request.cacheModel.locations.destinationCountry match {
      case Some(destinationCountry) =>
        Countries.form(DestinationCountryPage).fill(destinationCountry)
      case None => Countries.form(DestinationCountryPage)
    }).withSubmissionErrors()

    Ok(destinationCountryPage(mode, form))
  }

  def submit(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    Countries
      .form(DestinationCountryPage)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(destinationCountryPage(mode, formWithErrors))),
        validCountry =>
          updateDeclarationFromRequest(_.updateDestinationCountry(validCountry)).map { _ =>
            redirectToNextPage(mode)
        }
      )
  }

  private def redirectToNextPage(mode: Mode)(implicit request: JourneyRequest[AnyContent]): Result =
    request.declarationType match {
      case DeclarationType.SUPPLEMENTARY | DeclarationType.CLEARANCE =>
        navigator.continueTo(mode, controllers.declaration.routes.LocationController.displayPage)
      case DeclarationType.STANDARD | DeclarationType.SIMPLIFIED | DeclarationType.OCCASIONAL =>
        navigator.continueTo(mode, controllers.declaration.routes.RoutingCountriesController.displayRoutingQuestion(_))
    }
}
