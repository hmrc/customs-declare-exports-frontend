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
import forms.declaration.RoutingQuestion
import forms.declaration.RoutingQuestion._
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration.destinationCountries.DestinationCountries.{FirstRoutingCountryPage, NextRoutingCountryPage}
import javax.inject.Inject
import models.{ExportsDeclaration, Mode}
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.Countries.retrieveCountryNameFromCode
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
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayRoutingQuestion(mode: Mode, fastForward: Boolean): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val containsRoutingCountries: Boolean = request.cacheModel.locations.routingCountries.nonEmpty

    if (fastForward && containsRoutingCountries) {
      navigator.continueTo(routes.RoutingCountriesSummaryController.displayPage(mode))
    } else {
      val destinationCountryCode = request.cacheModel.locations.destinationCountry
      val destinationCountryName = destinationCountryCode.flatMap(retrieveCountryNameFromCode(_)).getOrElse("")

      request.cacheModel.locations.hasRoutingCountries.map(answerFromBoolean) match {
        case Some(answer) => Ok(routingQuestionPage(mode, form.fill(answer), destinationCountryName))
        case None         => Ok(routingQuestionPage(mode, form, destinationCountryName))
      }
    }
  }

  def submitRoutingAnswer(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val destinationCountry = request.cacheModel.locations.destinationCountry.getOrElse("-")

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(routingQuestionPage(mode, formWithErrors, destinationCountry))),
        validAnswer => updateRoutingAnswer(validAnswer).map(_ => redirectFromRoutingAnswer(mode, validAnswer))
      )
  }

  private def updateRoutingAnswer(answer: RoutingQuestion)(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    if (answer.toBoolean) updateExportsDeclarationSyncDirect(_.updateRoutingQuestion(answerToBoolean(answer)))
    else updateExportsDeclarationSyncDirect(_.clearRoutingCountries)

  private def redirectFromRoutingAnswer(mode: Mode, answer: RoutingQuestion)(implicit request: JourneyRequest[AnyContent]): Result =
    if (answer.toBoolean) navigator.continueTo(controllers.declaration.routes.RoutingCountriesController.displayRoutingCountry(mode))
    else navigator.continueTo(controllers.declaration.routes.LocationController.displayPage(mode))

  def displayRoutingCountry(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val routingAnswer = request.cacheModel.locations.hasRoutingCountries
    val page = if (request.cacheModel.locations.routingCountries.nonEmpty) NextRoutingCountryPage else FirstRoutingCountryPage

    routingAnswer match {
      case Some(answer) if answer => Ok(countryOfRoutingPage(mode, DestinationCountries.form(page), page.id))
      case _                      => navigator.continueTo(controllers.declaration.routes.RoutingCountriesController.displayRoutingQuestion(mode, false))
    }
  }

  def submitRoutingCountry(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val hasCountriesAdded = request.cacheModel.locations.routingCountries.nonEmpty
    val page = if (hasCountriesAdded) NextRoutingCountryPage else FirstRoutingCountryPage

    DestinationCountries
      .form(page)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(countryOfRoutingPage(mode, formWithErrors, page.id))),
        validCountry => {
          val newRoutingCountries = request.cacheModel.locations.routingCountries :+ validCountry

          updateRoutingCountries(newRoutingCountries).map { _ =>
            navigator.continueTo(controllers.declaration.routes.RoutingCountriesSummaryController.displayPage(mode))
          }
        }
      )
  }

  private def updateRoutingCountries(
    routingCountries: Seq[String]
  )(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(_.updateCountriesOfRouting(routingCountries))
}
