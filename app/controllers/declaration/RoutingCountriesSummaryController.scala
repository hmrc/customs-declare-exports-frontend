/*
 * Copyright 2019 HM Revenue & Customs
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
import forms.declaration.RoutingQuestionYesNo
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import services.Countries.retrieveCountriesFromCodes
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.destinationCountries.{remove_routing_country, routing_countries_summary}

import scala.concurrent.{ExecutionContext, Future}

class RoutingCountriesSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  routingCountriesSummaryPage: routing_countries_summary,
  removeRoutingCountryPage: remove_routing_country
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.declarationType match {
      case DeclarationType.SUPPLEMENTARY =>
        navigator.continueTo(controllers.declaration.routes.DestinationCountryController.displayPage(mode))
      case _ =>
        val countryCodes = request.cacheModel.locations.routingCountries
        val countries = retrieveCountriesFromCodes(countryCodes)

        if (countries.nonEmpty) {
          Ok(routingCountriesSummaryPage(mode, RoutingQuestionYesNo.form(), countries))
        } else {
          navigator.continueTo(controllers.declaration.routes.RoutingCountriesController.displayRoutingQuestion(mode))
        }
    }
  }

  def submit(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val countryCodes = request.cacheModel.locations.routingCountries
    val countries = retrieveCountriesFromCodes(countryCodes)

    RoutingQuestionYesNo
      .form()
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(routingCountriesSummaryPage(mode, formWithErrors, countries)),
        validAnswer =>
          if (validAnswer.answer)
            navigator.continueTo(controllers.declaration.routes.RoutingCountriesController.displayRoutingCountry(mode))
          else navigator.continueTo(controllers.declaration.routes.LocationController.displayPage(mode))
      )
  }

  def displayRemoveCountryPage(mode: Mode, countryCode: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val isCountryPresentedInCache = request.cacheModel.locations.routingCountries.contains(countryCode)
    val country = services.Countries.countryCodeMap(countryCode)

    if (isCountryPresentedInCache) Ok(removeRoutingCountryPage(mode, RoutingQuestionYesNo.form(), country))
    else navigator.continueTo(controllers.declaration.routes.RoutingCountriesSummaryController.displayPage(mode))
  }

  def submitRemoveCountry(mode: Mode, countryCode: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val country = services.Countries.countryCodeMap(countryCode)

    RoutingQuestionYesNo
      .form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(removeRoutingCountryPage(mode, formWithErrors, country))),
        validAnswer =>
          if (validAnswer.answer) {
            removeCountry(countryCode).map { _ =>
              navigator.continueTo(controllers.declaration.routes.RoutingCountriesSummaryController.displayPage(mode))
            }
          } else Future.successful(navigator.continueTo(controllers.declaration.routes.RoutingCountriesSummaryController.displayPage(mode)))
      )
  }

  private def removeCountry(countryCode: String)(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(_.removeCountryOfRouting(countryCode))
}
