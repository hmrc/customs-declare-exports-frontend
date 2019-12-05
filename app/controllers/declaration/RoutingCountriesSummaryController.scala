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
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration.destinationCountries.DestinationCountries.{FirstRoutingCountryPage, NextRoutingCountryPage}
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.Countries.findByCodes
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.destinationCountries.{change_routing_country, remove_routing_country, routing_countries_summary}

import scala.concurrent.{ExecutionContext, Future}

class RoutingCountriesSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  routingCountriesSummaryPage: routing_countries_summary,
  removeRoutingCountryPage: remove_routing_country,
  changeRoutingCountryPage: change_routing_country
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val countryCodes = request.cacheModel.locations.routingCountries
    val countries = findByCodes(countryCodes)

    if (countries.nonEmpty) {
      Ok(routingCountriesSummaryPage(mode, RoutingQuestionYesNo.form(), countries))
    } else {
      navigator.continueTo(controllers.declaration.routes.RoutingCountriesController.displayRoutingQuestion(mode))
    }
  }

  def submit(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val countryCodes = request.cacheModel.locations.routingCountries
    val countries = findByCodes(countryCodes)

    RoutingQuestionYesNo
      .form(countryCodes)
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(routingCountriesSummaryPage(mode, formWithErrors, countries)),
        validAnswer => redirectFromSummaryPage(mode, validAnswer)
      )
  }

  private def redirectFromSummaryPage(mode: Mode, answer: Boolean)(implicit request: JourneyRequest[AnyContent]): Result =
    if (answer) {
      navigator.continueTo(controllers.declaration.routes.RoutingCountriesController.displayRoutingCountry(mode))
    } else {
      navigator.continueTo(controllers.declaration.routes.LocationController.displayPage(mode))
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
          if (validAnswer) {
            removeCountry(countryCode).map { _ =>
              navigator.continueTo(controllers.declaration.routes.RoutingCountriesSummaryController.displayPage(mode))
            }
          } else Future.successful(navigator.continueTo(controllers.declaration.routes.RoutingCountriesSummaryController.displayPage(mode)))
      )
  }

  private def removeCountry(countryCode: String)(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(_.removeCountryOfRouting(countryCode))

  def displayChangeCountryPage(mode: Mode, countryCode: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val cachedCountries = request.cacheModel.locations.routingCountries
    val isCountryPresentedInCache = cachedCountries.contains(countryCode)

    val countryIndex = cachedCountries.indexOf(countryCode)
    val page = if (countryIndex > 0) NextRoutingCountryPage else FirstRoutingCountryPage

    if (isCountryPresentedInCache) Ok(changeRoutingCountryPage(mode, DestinationCountries.form(page).fill(countryCode), page, countryCode))
    else navigator.continueTo(controllers.declaration.routes.RoutingCountriesSummaryController.displayPage(mode))
  }

  def submitChangeCountry(mode: Mode, countryToChange: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val cachedCountries = request.cacheModel.locations.routingCountries
    val countriesForValidation = cachedCountries.filterNot(_ == countryToChange)
    val countryIndex = cachedCountries.indexOf(countryToChange)
    val page = if (countryIndex > 0) NextRoutingCountryPage else FirstRoutingCountryPage

    DestinationCountries
      .form(page, countriesForValidation)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(changeRoutingCountryPage(mode, formWithErrors, page, countryToChange))),
        validCountry => {
          val updatedCountries = cachedCountries.updated(countryIndex, validCountry)

          updateExportsDeclarationSyncDirect(_.updateCountriesOfRouting(updatedCountries)).map { _ =>
            navigator.continueTo(controllers.declaration.routes.RoutingCountriesSummaryController.displayPage(mode))
          }
        }
      )
  }
}
