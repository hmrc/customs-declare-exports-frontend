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
import controllers.util.MultipleItemsHelper.remove
import controllers.util._
import forms.declaration.destinationCountries._
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.destination_countries_standard

import scala.concurrent.{ExecutionContext, Future}

class DestinationCountriesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  destinationCountriesStandardPage: destination_countries_standard
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.declarationType match {
      case DeclarationType.SUPPLEMENTARY => Redirect(routes.DestinationCountryController.displayPage())
      case DeclarationType.STANDARD | DeclarationType.SIMPLIFIED =>
        Ok(
          destinationCountriesStandardPage(
            mode,
            DestinationCountries.form(DestinationCountries.FirstRoutingCountryPage),
            request.cacheModel.locations.routingCountries
          )
        )
    }
  }

  def saveCountries(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    request.declarationType match {
      case DeclarationType.SUPPLEMENTARY => Future.successful(Redirect(routes.DestinationCountryController.displayPage()))
      case DeclarationType.STANDARD | DeclarationType.SIMPLIFIED =>
        handleSubmitStandard(mode)
    }
  }

  private def handleSubmitStandard(mode: Mode)(implicit request: JourneyRequest[AnyContent]): Future[Result] = {
    val actionTypeOpt = FormAction.bindFromRequest()
    val boundForm = DestinationCountries.form(DestinationCountries.FirstRoutingCountryPage).bindFromRequest()

    val cache = request.cacheModel.locations.routingCountries

    actionTypeOpt match {
      case Add if !boundForm.hasErrors     => addRoutingCountry(mode, cache, boundForm)
      case SaveAndContinue | SaveAndReturn => saveAndContinue(mode, cache, boundForm)
      case Remove(values)                  => removeRoutingCountry(mode, values, boundForm, cache)
      case _                               => Future.successful(BadRequest(destinationCountriesStandardPage(mode, boundForm)))
    }
  }

  private def addRoutingCountry(mode: Mode, cachedData: Seq[String], boundForm: Form[String], withRedirection: Boolean = false)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] = {
    val newRoutingCountry = boundForm.value.getOrElse("")

    if (cachedData.contains(newRoutingCountry)) {
      val formWithError = boundForm.copy(errors = Seq(FormError("country", "declaration.destinationCountries.duplication")))
      Future.successful(BadRequest(destinationCountriesStandardPage(mode, formWithError, cachedData)))
    } else {
      val newCountries = cachedData :+ newRoutingCountry

      updateCache(newCountries).map { _ =>
        if (withRedirection)
          navigator.continueTo(controllers.declaration.routes.LocationController.displayPage(mode))
        else Ok(destinationCountriesStandardPage(mode, DestinationCountries.form(DestinationCountries.FirstRoutingCountryPage), newCountries))
      }
    }
  }

  private def saveAndContinue(mode: Mode, cachedData: Seq[String], boundForm: Form[String])(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] = {

    val formWithoutEmptyError = boundForm.copy(errors = boundForm.errors.filterNot(_.message contains "empty"))

    if (formWithoutEmptyError.errors.nonEmpty) {
      Future.successful(BadRequest(destinationCountriesStandardPage(mode, formWithoutEmptyError, cachedData)))
    } else {
      boundForm.value match {
        case Some(country) if country.nonEmpty => addRoutingCountry(mode, cachedData, boundForm, true)
        case _                                 => Future.successful(navigator.continueTo(controllers.declaration.routes.LocationController.displayPage(mode)))
      }
    }
  }

  private def removeRoutingCountry(mode: Mode, keys: Seq[String], userInput: Form[String], cachedData: Seq[String])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {

    val updatedCountries = remove(cachedData, keys.contains(_: String))

    updateCache(updatedCountries)
      .map(_ => Ok(destinationCountriesStandardPage(mode, userInput.discardingErrors, updatedCountries)))
  }

  private def updateCache(formData: Seq[String])(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => model.copy(locations = model.locations.copy(routingCountries = formData)))

}
