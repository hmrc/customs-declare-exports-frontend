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

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.cacheId
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.Choice.AllowedChoiceValues.{StandardDec, SupplementaryDec}
import forms.declaration.destinationCountries.DestinationCountries.{formId, standardForm, supplementaryForm}
import forms.declaration.destinationCountries._
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import play.twirl.api.Html
import services.CustomsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.validators.forms.supplementary.DestinationCountriesValidator
import utils.validators.forms.{Invalid, Valid}
import views.html.declaration.{destination_countries_standard, destination_countries_supplementary}

import scala.concurrent.{ExecutionContext, Future}

class DestinationCountriesController @Inject()(
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  implicit val countries = services.Countries.allCountries

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    (request.choice.value match {
      case SupplementaryDec => displayFormSupplementary()
      case StandardDec      => displayFormStandard()
    }).map(resultView => Ok(resultView))
  }

  private def displayFormSupplementary()(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Html] =
    customsCacheService.fetchAndGetEntry[DestinationCountriesSupplementary](cacheId, formId).map {
      case Some(data) => destination_countries_supplementary(supplementaryForm.fill(data))
      case _          => destination_countries_supplementary(supplementaryForm)
    }

  private def displayFormStandard()(implicit request: JourneyRequest[AnyContent], hc: HeaderCarrier): Future[Html] =
    customsCacheService.fetchAndGetEntry[DestinationCountriesStandard](cacheId, formId).map {
      case Some(data) => destination_countries_standard(standardForm.fill(data), data.countriesOfRouting)
      case _          => destination_countries_standard(standardForm, Seq.empty)
    }

  def saveCountries(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    (request.choice.value match {
      case SupplementaryDec => handleSavingSupplementary()
      case StandardDec      => handleSavingStandard()
    }).flatMap {
      case Left(resultView) => Future.successful(BadRequest(resultView))
      case Right(call)      => Future.successful(Redirect(call))
    }
  }

  private def handleSavingSupplementary()(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Either[Html, Call]] =
    supplementaryForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[DestinationCountriesSupplementary]) =>
          Future.successful(Left(destination_countries_supplementary(formWithErrors))),
        form =>
          customsCacheService.cache[DestinationCountriesSupplementary](cacheId, formId, form).map { _ =>
            Right(controllers.declaration.routes.LocationController.displayForm())
        }
      )

  private def handleSavingStandard()(implicit request: JourneyRequest[AnyContent]): Future[Either[Html, Call]] = {
    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))

    val cachedData = customsCacheService
      .fetchAndGetEntry[DestinationCountriesStandard](cacheId, formId)
      .map(_.getOrElse(DestinationCountriesStandard.empty))

    cachedData.flatMap { cache =>
      actionTypeOpt match {
        case Some(Add)             => addRoutingCountry(cache)
        case Some(Remove(values))  => remove(values, cache)
        case Some(SaveAndContinue) => saveAndContinue(cache)
        case _                     => Future.successful(Left(errorHandler.globalErrorTemplate()))
      }
    }
  }

  private def addRoutingCountry(
    cachedData: DestinationCountriesStandard
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Either[Html, Call]] = {
    val countriesStandardForm = Form(DestinationCountriesStandard.mapping).bindFromRequest()
    val countriesStandardInput = countriesStandardForm.value.getOrElse(DestinationCountriesStandard.empty())
    val countriesStandardUpdated =
      cachedData.copy(countriesOfRouting = cachedData.countriesOfRouting ++ countriesStandardInput.countriesOfRouting)

    DestinationCountriesValidator.validateOnAddition(countriesStandardUpdated) match {
      case Valid =>
        customsCacheService.cache[DestinationCountriesStandard](cacheId, formId, countriesStandardUpdated).map { _ =>
          Right(controllers.declaration.routes.DestinationCountriesController.displayForm())
        }
      case Invalid(errors) =>
        Future.successful(
          Left(
            destination_countries_standard(countriesStandardForm.copy(errors = errors), cachedData.countriesOfRouting)
          )
        )
    }
  }

  private def remove(keys: Seq[String], cachedData: DestinationCountriesStandard)(
    implicit request: JourneyRequest[_]
  ): Future[Either[Html, Call]] = {
    val key = keys.headOption.getOrElse("")

    val updatedCountries = cachedData.countriesOfRouting.zipWithIndex.filter(_._2.toString != key).map(_._1)

    val updatedCache = cachedData.copy(countriesOfRouting = updatedCountries)

    customsCacheService.cache[DestinationCountriesStandard](cacheId, formId, updatedCache).map { _ =>
      Right(controllers.declaration.routes.DestinationCountriesController.displayForm())
    }
  }

  private def saveAndContinue(
    cachedData: DestinationCountriesStandard
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Either[Html, Call]] = {
    val countriesStandardForm = Form(DestinationCountriesStandard.mapping).bindFromRequest()
    val countriesStandardInput = countriesStandardForm.value.getOrElse(DestinationCountriesStandard.empty())
    val countriesStandardUpdated = countriesStandardInput.copy(
      countriesOfRouting = cachedData.countriesOfRouting ++ countriesStandardInput.countriesOfRouting
    )

    DestinationCountriesValidator.validateOnSaveAndContinue(countriesStandardUpdated) match {
      case Valid =>
        customsCacheService.cache[DestinationCountriesStandard](cacheId, formId, countriesStandardUpdated).map { _ =>
          Right(controllers.declaration.routes.LocationController.displayForm())
        }
      case Invalid(errors) =>
        Future.successful(
          Left(
            destination_countries_standard(countriesStandardForm.copy(errors = errors), cachedData.countriesOfRouting)
          )
        )
    }
  }
}
