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
import forms.declaration.RoutingCountryQuestionYesNo
import forms.declaration.countries.Countries.{FirstRoutingCountryPage, NextRoutingCountryPage}
import forms.declaration.countries.{Countries, Country}
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.Countries.{findByCode, findByCodes}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.destinationCountries.{change_routing_country, remove_routing_country, routing_countries_summary}

import javax.inject.Inject
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
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val countryCodes = request.cacheModel.locations.routingCountries.flatMap(_.code)
    val countries = findByCodes(countryCodes)

    if (countries.nonEmpty) {
      Ok(routingCountriesSummaryPage(mode, RoutingCountryQuestionYesNo.formAdd().withSubmissionErrors(), countries))
    } else {
      navigator.continueTo(mode, routes.RoutingCountriesController.displayRoutingQuestion(_))
    }
  }

  def submit(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val countryCodes = request.cacheModel.locations.routingCountries
    val countries = findByCodes(countryCodes.flatMap(_.code))

    RoutingCountryQuestionYesNo
      .formAdd(countryCodes)
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(routingCountriesSummaryPage(mode, formWithErrors, countries)),
        validAnswer => redirectFromSummaryPage(mode, validAnswer)
      )
  }

  private def redirectFromSummaryPage(mode: Mode, answer: Boolean)(implicit request: JourneyRequest[AnyContent]): Result =
    if (answer)
      navigator.continueTo(mode, routes.RoutingCountriesController.displayRoutingCountry, mode.isErrorFix)
    else navigator.continueTo(mode, routes.LocationController.displayPage)

  def displayRemoveCountryPage(mode: Mode, countryCode: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val isCountryPresentedInCache = request.cacheModel.locations.routingCountries.flatMap(_.code).contains(countryCode)
    val country = findByCode(countryCode)

    if (isCountryPresentedInCache) Ok(removeRoutingCountryPage(mode, RoutingCountryQuestionYesNo.formRemove(), country))
    else navigator.continueTo(mode, routes.RoutingCountriesSummaryController.displayPage)
  }

  def submitRemoveCountry(mode: Mode, countryCode: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val country = findByCode(countryCode)

    RoutingCountryQuestionYesNo
      .formRemove()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(removeRoutingCountryPage(mode, formWithErrors, country))),
        validAnswer =>
          if (validAnswer) removeCountry(Country(Some(countryCode))).map(_ => removeRedirect(mode))
          else Future.successful(removeRedirect(mode))
      )
  }

  private def removeRedirect(mode: Mode)(implicit request: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(mode, routes.RoutingCountriesSummaryController.displayPage, mode.isErrorFix)

  private def removeCountry(country: Country)(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(_.removeCountryOfRouting(country))

  def displayChangeCountryPage(mode: Mode, countryCode: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val cachedCountries = request.cacheModel.locations.routingCountries.flatMap(_.code)
    val isCountryPresentedInCache = cachedCountries.contains(countryCode)

    val countryIndex = cachedCountries.indexOf(countryCode)
    val page = if (countryIndex > 0) NextRoutingCountryPage else FirstRoutingCountryPage

    if (isCountryPresentedInCache) Ok(changeRoutingCountryPage(mode, Countries.form(page).fill(Country(Some(countryCode))), page, countryCode))
    else navigator.continueTo(mode, routes.RoutingCountriesSummaryController.displayPage, mode.isErrorFix)
  }

  def submitChangeCountry(mode: Mode, countryToChange: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val cachedCountries = request.cacheModel.locations.routingCountries
    val cachedCountryCodes = cachedCountries.flatMap(_.code)
    val countriesForValidation = cachedCountries.filterNot(_.code == Some(countryToChange))
    val countryIndex = cachedCountryCodes.indexOf(countryToChange)
    val page = if (countryIndex > 0) NextRoutingCountryPage else FirstRoutingCountryPage

    Countries
      .form(page, countriesForValidation)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(changeRoutingCountryPage(mode, formWithErrors, page, countryToChange))),
        validCountry => {
          val updatedCountries = cachedCountries.updated(countryIndex, validCountry)

          updateExportsDeclarationSyncDirect(_.updateCountriesOfRouting(updatedCountries)).map { _ =>
            navigator.continueTo(mode, routes.RoutingCountriesSummaryController.displayPage, mode.isErrorFix)
          }
        }
      )
  }
}
