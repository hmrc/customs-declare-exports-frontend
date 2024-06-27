/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.section3

import connectors.CodeListConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.{ModelCacheable, SubmissionErrors}
import controllers.section3.routes.{LocationOfGoodsController, RoutingCountriesController}
import controllers.helpers._
import controllers.helpers.SequenceIdHelper.handleSequencing
import controllers.navigation.Navigator
import forms.declaration.countries.Countries.RoutingCountryPage
import forms.declaration.countries.{Countries, Country}
import forms.section3.RoutingCountryQuestionYesNo._
import models.ExportsDeclaration
import models.declaration.RoutingCountry
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.validators.forms.AutoCompleteFieldBinding
import views.html.section3.{country_of_routing, routing_country_question}

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
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector, auditService: AuditService)
    extends FrontendController(mcc) with AutoCompleteFieldBinding with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val displayRoutingQuestion: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val form = formFirst().withSubmissionErrors
    request.cacheModel.locations.hasRoutingCountries match {
      case Some(answer) => Ok(routingQuestionPage(form.fill(answer)))
      case None         => Ok(routingQuestionPage(form))
    }
  }

  val submitRoutingAnswer: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    formFirst(cachedCountries)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(routingQuestionPage(formWithErrors))),
        answer =>
          updateDeclarationFromRequest { declaration =>
            if (answer) declaration.updateRoutingQuestion(answer)
            else declaration.clearRoutingCountries()
          } map { _ =>
            val call = if (answer) RoutingCountriesController.displayRoutingCountry else LocationOfGoodsController.displayPage
            navigator.continueTo(call)
          }
      )
  }

  val displayRoutingCountry: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.locations.hasRoutingCountries match {
      case Some(answer) if answer => Ok(countryOfRoutingPage(Countries.form(RoutingCountryPage).withSubmissionErrors))

      case _ => navigator.continueTo(RoutingCountriesController.displayRoutingQuestion)
    }
  }

  val submitRoutingCountry: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val isFormEmpty = Countries
      .form(RoutingCountryPage, cachedCountries)
      .bindFromRequest(formValuesFromRequest(Countries.fieldId))
      .apply(Countries.fieldId)
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
        val redirect = navigator.continueTo(RoutingCountriesController.displayRoutingCountry)

        values.headOption
          .flatMap(services.Countries.findByCode)
          .fold(Future.successful(redirect)) { country =>
            val countryToRemove = Country(Some(country.countryCode))
            val updatedCountries = cachedRoutingCountries.filterNot(_.country == countryToRemove)
            updateCache(updatedCountries).map(_ => redirect)
          }
    }
  }

  private def cachedRoutingCountries(implicit request: JourneyRequest[AnyContent]): Seq[RoutingCountry] =
    request.cacheModel.locations.routingCountries

  private def cachedCountries(implicit request: JourneyRequest[AnyContent]): Seq[Country] =
    cachedRoutingCountries.map(_.country)

  private def validateAndRedirect(redirect: Result)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    Countries
      .form(RoutingCountryPage, cachedCountries)
      .bindFromRequest(formValuesFromRequest(Countries.fieldId))
      .fold(
        formWithErrors => Future.successful(BadRequest(countryOfRoutingPage(formWithErrors))),
        routingCountry => {
          val updatedCountries = cachedRoutingCountries :+ RoutingCountry(country = routingCountry)
          updateCache(updatedCountries).map(_ => redirect)
        }
      )

  private def updateCache(routingCountries: Seq[RoutingCountry])(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] = {
    val declarationMeta = request.cacheModel.declarationMeta
    val (updatedRoutingCountries, updatedMeta) = handleSequencing(routingCountries, declarationMeta)

    updateDeclarationFromRequest(
      _.updateCountriesOfRouting(updatedRoutingCountries)
        .copy(declarationMeta = updatedMeta)
    )
  }
}
