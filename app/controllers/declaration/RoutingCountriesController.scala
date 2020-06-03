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
import forms.declaration.RoutingQuestionYesNo._
import forms.declaration.countries.Countries
import forms.declaration.countries.Countries.{FirstRoutingCountryPage, NextRoutingCountryPage}
import javax.inject.Inject
import models.Mode.ErrorFix
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.Countries.findByCode
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.destinationCountries.{country_of_routing, routing_country_question}

import scala.concurrent.{ExecutionContext, Future}

class RoutingCountriesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  routingQuestionPage: routing_country_question,
  countryOfRoutingPage: country_of_routing
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayRoutingQuestion(mode: Mode, fastForward: Boolean): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (fastForward && request.cacheModel.containRoutingCountries()) {
      navigator.continueTo(mode, routes.RoutingCountriesSummaryController.displayPage)
    } else {
      val destinationCountryCode = request.cacheModel.locations.destinationCountry.flatMap(_.code)
      val destinationCountryName = destinationCountryCode.map(findByCode(_)).map(_.asString()).getOrElse("")

      val frm = form().withSubmissionErrors()
      request.cacheModel.locations.hasRoutingCountries match {
        case Some(answer) => Ok(routingQuestionPage(mode, frm.fill(answer), destinationCountryName))
        case None         => Ok(routingQuestionPage(mode, frm, destinationCountryName))
      }
    }
  }

  def submitRoutingAnswer(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val destinationCountry = request.cacheModel.locations.destinationCountry.flatMap(_.code).getOrElse("-")
    val cachedCountries = request.cacheModel.locations.routingCountries

    form(cachedCountries)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(routingQuestionPage(mode, formWithErrors, destinationCountry))),
        validAnswer => updateRoutingAnswer(validAnswer).map(_ => redirectFromRoutingAnswer(mode, validAnswer))
      )
  }

  private def updateRoutingAnswer(answer: Boolean)(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    if (answer) {
      updateExportsDeclarationSyncDirect(_.updateRoutingQuestion(answer))
    } else {
      updateExportsDeclarationSyncDirect(_.clearRoutingCountries)
    }

  private def redirectFromRoutingAnswer(mode: Mode, answer: Boolean)(implicit request: JourneyRequest[AnyContent]): Result =
    mode match {
      case ErrorFix if answer => Redirect(controllers.declaration.routes.RoutingCountriesController.displayRoutingCountry(mode))
      case _ if answer        => navigator.continueTo(mode, controllers.declaration.routes.RoutingCountriesController.displayRoutingCountry)
      case _                  => navigator.continueTo(mode, controllers.declaration.routes.LocationController.displayPage)
    }

  def displayRoutingCountry(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val routingAnswer = request.cacheModel.locations.hasRoutingCountries
    val page = if (request.cacheModel.locations.routingCountries.nonEmpty) NextRoutingCountryPage else FirstRoutingCountryPage

    routingAnswer match {
      case Some(answer) if answer => Ok(countryOfRoutingPage(mode, Countries.form(page), page))
      case _ =>
        navigator.continueTo(mode, controllers.declaration.routes.RoutingCountriesController.displayRoutingQuestion(_, fastForward = false))
    }
  }

  def submitRoutingCountry(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val hasCountriesAdded = request.cacheModel.locations.routingCountries.nonEmpty
    val cachedCountries = request.cacheModel.locations.routingCountries
    val page = if (hasCountriesAdded) NextRoutingCountryPage else FirstRoutingCountryPage

    Countries
      .form(page, cachedCountries)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(countryOfRoutingPage(mode, formWithErrors, page))),
        validCountry => {
          val newRoutingCountries = request.cacheModel.locations.routingCountries :+ validCountry

          updateExportsDeclarationSyncDirect(_.updateCountriesOfRouting(newRoutingCountries)).map { _ =>
            if (mode == ErrorFix) {
              Redirect(controllers.declaration.routes.RoutingCountriesSummaryController.displayPage(mode))
            } else {
              navigator.continueTo(mode, controllers.declaration.routes.RoutingCountriesSummaryController.displayPage)
            }
          }
        }
      )
  }
}
