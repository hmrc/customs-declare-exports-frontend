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
import controllers.helpers._
import controllers.navigation.Navigator
import forms.declaration.RoutingCountryQuestionYesNo._
import forms.declaration.countries.Countries.RoutingCountryPage
import forms.declaration.countries.{Countries, Country}
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.destinationCountries.{country_of_routing, routing_country_question}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RoutingCountriesController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  routingQuestionPage: routing_country_question,
  countryOfRoutingPage: country_of_routing
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayRoutingQuestion(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val form = formFirst().withSubmissionErrors
    request.cacheModel.locations.hasRoutingCountries match {
      case Some(answer) => Ok(routingQuestionPage(form.fill(answer)))
      case None         => Ok(routingQuestionPage(form))
    }
  }

  def submitRoutingAnswer(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val cachedCountries = request.cacheModel.locations.routingCountries

    formFirst(cachedCountries).bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(routingQuestionPage(formWithErrors))),
        answer =>
          updateDeclarationFromRequest { declaration =>
            if (answer) declaration.updateRoutingQuestion(answer)
            else declaration.clearRoutingCountries()
          } map { _ =>
            if (answer) navigator.continueTo(RoutingCountriesController.displayRoutingCountry)
            else navigator.continueTo(LocationOfGoodsController.displayPage)
          }
      )
  }

  def displayRoutingCountry(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.locations.hasRoutingCountries match {
      case Some(answer) if answer => Ok(countryOfRoutingPage(Countries.form(RoutingCountryPage).withSubmissionErrors))

      case _ => navigator.continueTo(RoutingCountriesController.displayRoutingQuestion)
    }
  }

  def submitRoutingCountry(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val isFormEmpty = Countries
      .form(RoutingCountryPage, request.cacheModel.locations.routingCountries)
      .bindFromRequest()
      .apply("countryCode")
      .value
      .forall(_.isEmpty)

    FormAction.bindFromRequest() match {
      case Add =>
        validateAndRedirect(navigator.continueTo(RoutingCountriesController.displayRoutingCountry))

      case SaveAndContinue | SaveAndReturnToSummary | SaveAndReturnToErrors if isFormEmpty && request.cacheModel.containRoutingCountries =>
        Future.successful(navigator.continueTo(LocationOfGoodsController.displayPage))

      case SaveAndContinue | SaveAndReturnToSummary | SaveAndReturnToErrors =>
        validateAndRedirect(navigator.continueTo(LocationOfGoodsController.displayPage))

      case Remove(values) =>
        val continueTo = navigator.continueTo(RoutingCountriesController.displayRoutingCountry)

        values.headOption
          .map(services.Countries.findByCode)
          .fold(Future.successful(continueTo)) { country =>
            updateAndRedirect(_.removeCountryOfRouting(Country(Some(country.countryCode))), continueTo)
          }
    }
  }

  private def validateAndRedirect(redirect: Result)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    Countries
      .form(RoutingCountryPage, request.cacheModel.locations.routingCountries)
      .bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(countryOfRoutingPage(formWithErrors))),
        validCountry => {
          val newRoutingCountries = request.cacheModel.locations.routingCountries :+ validCountry
          updateAndRedirect(_.updateCountriesOfRouting(newRoutingCountries), redirect)
        }
      )

  private def updateAndRedirect(update: ExportsDeclaration => ExportsDeclaration, redirect: Result)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    updateDeclarationFromRequest(update).map(_ => redirect)
}
