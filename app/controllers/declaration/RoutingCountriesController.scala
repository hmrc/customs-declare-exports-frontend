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
import controllers.declaration.routes.{LocationOfGoodsController, RoutingCountriesController}
import controllers.helpers.{Add, FormAction, Remove, SaveAndContinue, SaveAndReturn}
import controllers.navigation.Navigator
import forms.declaration.RoutingCountryQuestionYesNo._
import forms.declaration.countries.{Countries, Country}
import forms.declaration.countries.Countries.RoutingCountryPage
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import services.{Countries => ServiceCountries}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.helpers.CountryHelper
import views.html.declaration.destinationCountries.{country_of_routing, routing_country_question}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RoutingCountriesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  routingQuestionPage: routing_country_question,
  countryOfRoutingPage: country_of_routing
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector, countryHelper: CountryHelper)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayRoutingQuestion(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = formFirst().withSubmissionErrors()
    request.cacheModel.locations.hasRoutingCountries match {
      case Some(answer) => Ok(routingQuestionPage(mode, frm.fill(answer), destinationCountryNameFromLocations))
      case None         => Ok(routingQuestionPage(mode, frm, destinationCountryNameFromLocations))
    }
  }

  def submitRoutingAnswer(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val destinationCountry = request.cacheModel.locations.destinationCountry.flatMap(_.code).getOrElse("-")
    val cachedCountries = request.cacheModel.locations.routingCountries

    formFirst(cachedCountries)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(routingQuestionPage(mode, formWithErrors, destinationCountry))),
        validAnswer => {
          updateDeclarationFromRequest { dec =>
            if (validAnswer) dec.updateRoutingQuestion(validAnswer)
            else dec.clearRoutingCountries()
          } map { _ =>
            if (validAnswer) navigator.continueTo(mode, RoutingCountriesController.displayRoutingCountry, mode.isErrorFix)
            else navigator.continueTo(mode, LocationOfGoodsController.displayPage)
          }
        }
      )
  }

  def displayRoutingCountry(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val routingAnswer = request.cacheModel.locations.hasRoutingCountries

    routingAnswer match {
      case Some(answer) if answer =>
        Ok(countryOfRoutingPage(mode, Countries.form(RoutingCountryPage), destinationCountryNameFromConsigneeDetails, routingCountriesList))
      case _ =>
        navigator.continueTo(mode, RoutingCountriesController.displayRoutingQuestion)
    }
  }

  def submitRoutingCountry(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val isFormEmpty = Countries
      .form(RoutingCountryPage, request.cacheModel.locations.routingCountries)
      .bindFromRequest()
      .apply("countryCode")
      .value
      .forall(_.isEmpty)

    FormAction.bindFromRequest() match {
      case Add =>
        validateAndRedirect(navigator.continueTo(mode, RoutingCountriesController.displayRoutingCountry, mode.isErrorFix), mode)

      case SaveAndContinue | SaveAndReturn if isFormEmpty && request.cacheModel.containRoutingCountries =>
        Future.successful(navigator.continueTo(mode, LocationOfGoodsController.displayPage))

      case SaveAndContinue | SaveAndReturn =>
        validateAndRedirect(navigator.continueTo(mode, LocationOfGoodsController.displayPage), mode)

      case Remove(values) =>
        val continueTo = navigator.continueTo(mode, RoutingCountriesController.displayRoutingCountry, mode.isErrorFix)

        values.headOption
          .map(services.Countries.findByCode)
          .fold(Future.successful(continueTo))({ country =>
            updateAndRedirect(_.removeCountryOfRouting(Country(Some(country.countryCode))), continueTo)
          })
    }
  }

  private def validateAndRedirect(redirect: Result, mode: Mode)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    Countries
      .form(RoutingCountryPage, request.cacheModel.locations.routingCountries)
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(countryOfRoutingPage(mode, formWithErrors, destinationCountryNameFromConsigneeDetails, routingCountriesList))),
        validCountry => {
          val newRoutingCountries = request.cacheModel.locations.routingCountries :+ validCountry
          updateAndRedirect(_.updateCountriesOfRouting(newRoutingCountries), redirect)
        }
      )

  private def updateAndRedirect(
    update: ExportsDeclaration => ExportsDeclaration,
    redirect: Result
  )(implicit request: JourneyRequest[AnyContent], hc: HeaderCarrier): Future[Result] =
    updateDeclarationFromRequest(update).map(_ => redirect)

  private def routingCountriesList(implicit request: JourneyRequest[_]): Seq[models.codes.Country] =
    request.cacheModel.locations.routingCountries
      .flatMap(_.code)
      .map(ServiceCountries.findByCode(_))

  private def destinationCountryNameFromLocations(implicit request: JourneyRequest[_]): String =
    request.cacheModel.locations.destinationCountry
      .flatMap(_.code)
      .map(ServiceCountries.findByCode(_))
      .map(countryHelper.getShortNameForCountry)
      .getOrElse("")

  private def destinationCountryNameFromConsigneeDetails(implicit request: JourneyRequest[_]): String =
    request.cacheModel.parties.consigneeDetails
      .map(_.details)
      .flatMap(_.address)
      .map(_.country)
      .getOrElse("")

}
