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
import controllers.util.CacheIdGenerator.cacheId
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.Choice.AllowedChoiceValues.{StandardDec, SupplementaryDec}
import forms.declaration.destinationCountries.DestinationCountries.{formId, Standard, Supplementary}
import forms.declaration.destinationCountries._
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.CustomsCacheService
import services.cache.{ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.collections.Removable.RemovableSeq
import utils.validators.forms.supplementary.DestinationCountriesValidator
import utils.validators.forms.{Invalid, Valid}
import views.html.declaration.{destination_countries_standard, destination_countries_supplementary}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DestinationCountriesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  destinationCountriesSupplementaryPage: destination_countries_supplementary,
  destinationCountriesStandardPage: destination_countries_standard
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    request.choice.value match {
      case SupplementaryDec => displayFormSupplementary()
      case StandardDec      => displayFormStandard()
    }
  }

  private def displayFormSupplementary()(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    exportsCacheService.get(journeySessionId).map(_.flatMap(_.locations.destinationCountries)).map {
      case Some(data) => Ok(destinationCountriesSupplementaryPage(Supplementary.form.fill(data)))
      case _          => Ok(destinationCountriesSupplementaryPage(Supplementary.form))
    }

  private def displayFormStandard()(implicit request: JourneyRequest[AnyContent], hc: HeaderCarrier): Future[Result] =
    exportsCacheService.get(journeySessionId).map(_.flatMap(_.locations.destinationCountries)).map {
      case Some(data) => Ok(destinationCountriesStandardPage(Standard.form.fill(data), data.countriesOfRouting))
      case _          => Ok(destinationCountriesStandardPage(Standard.form, Seq.empty))
    }

  def saveCountries(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    request.choice.value match {
      case SupplementaryDec => handleSubmitSupplementary()
      case StandardDec      => handleSubmitStandard()
    }
  }

  private def handleSubmitSupplementary()(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    Supplementary.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[DestinationCountries]) =>
          Future.successful(BadRequest(destinationCountriesSupplementaryPage(formWithErrors))),
        formData =>
          for {
            _ <- updateCache(journeySessionId, formData)
            _ <- customsCacheService.cache[DestinationCountries](cacheId, formId, formData)
          } yield Redirect(controllers.declaration.routes.LocationController.displayForm())
      )

  private def handleSubmitStandard()(implicit request: JourneyRequest[AnyContent]): Future[Result] = {
    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded)

    val cachedData = exportsCacheService
      .get(journeySessionId)
      .map(_.flatMap(_.locations.destinationCountries))
      .map(_.getOrElse(DestinationCountries.empty()))

    cachedData.flatMap { cache =>
      actionTypeOpt match {
        case Some(Add)             => addRoutingCountry(cache)
        case Some(SaveAndContinue) => saveAndContinue(cache)
        case Some(Remove(values))  => removeRoutingCountry(values, cache)
        case _                     => errorHandler.displayErrorPage()
      }
    }
  }

  private def addRoutingCountry(
    cachedData: DestinationCountries
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Result] = {
    val countriesStandardForm = Standard.form.bindFromRequest()
    val countriesStandardInput = countriesStandardForm.value.getOrElse(DestinationCountries.empty())
    val countriesStandardUpdated =
      cachedData.copy(countriesOfRouting = cachedData.countriesOfRouting ++ countriesStandardInput.countriesOfRouting)

    DestinationCountriesValidator.validateOnAddition(countriesStandardUpdated) match {
      case Valid =>
        for {
          _ <- updateCache(journeySessionId, countriesStandardUpdated)
          _ <- customsCacheService.cache[DestinationCountries](cacheId, formId, countriesStandardUpdated)
          result <- refreshPage(countriesStandardInput)
        } yield result
      case Invalid(errors) =>
        Future.successful(
          BadRequest(
            destinationCountriesStandardPage(
              adjustDataKeys(countriesStandardForm).copy(errors = errors.map(adjustErrorKey)),
              cachedData.countriesOfRouting
            )
          )
        )
    }
  }

  private def saveAndContinue(
    cachedData: DestinationCountries
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Result] = {
    val countriesStandardForm = Standard.form.bindFromRequest()
    val countriesStandardInput = countriesStandardForm.value.getOrElse(DestinationCountries.empty())
    val countriesStandardUpdated = countriesStandardInput.copy(
      countriesOfRouting = cachedData.countriesOfRouting ++ countriesStandardInput.countriesOfRouting
    )

    DestinationCountriesValidator.validateOnSaveAndContinue(countriesStandardUpdated) match {
      case Valid =>
        for {
          _ <- updateCache(journeySessionId, countriesStandardUpdated)
          _ <- customsCacheService.cache[DestinationCountries](cacheId, formId, countriesStandardUpdated)
        } yield Redirect(controllers.declaration.routes.LocationController.displayForm())
      case Invalid(errors) =>
        Future.successful(
          BadRequest(
            destinationCountriesStandardPage(
              adjustDataKeys(countriesStandardForm).copy(errors = errors.map(adjustErrorKey)),
              cachedData.countriesOfRouting
            )
          )
        )
    }
  }

  private def adjustErrorKey(error: FormError): FormError =
    if (error.key.contains("countriesOfRouting"))
      error.copy(key = error.key.replaceAll("\\[.*?\\]", "").concat("[]"))
    else
      error

  private def adjustDataKeys(form: Form[DestinationCountries]): Form[DestinationCountries] =
    form.copy(data = form.data.map {
      case (key, value) if key.contains("countriesOfRouting") => ("countriesOfRouting[]", value)
      case (key, value)                                       => (key, value)
    })

  private def removeRoutingCountry(keys: Seq[String], cachedData: DestinationCountries)(
    implicit request: JourneyRequest[_]
  ): Future[Result] = {
    val key = if (isKeysFormatCorrect(keys)) {
      keys.head.toInt
    } else throw new IllegalArgumentException("Data format for removal request is incorrect")

    val updatedCountries = removeElement(cachedData.countriesOfRouting, key)

    val updatedCache = cachedData.copy(countriesOfRouting = updatedCountries)

    for {
      _ <- updateCache(journeySessionId, updatedCache)
      _ <- customsCacheService.cache[DestinationCountries](cacheId, formId, updatedCache)
      result <- refreshPage(Standard.form.bindFromRequest().value.getOrElse(DestinationCountries.empty()))
    } yield result
  }

  private def isKeysFormatCorrect(keys: Seq[String]): Boolean = keys.length == 1 && Try(keys.head.toInt).isSuccess

  private def removeElement[A](collection: Seq[A], indexToRemove: Int): Seq[A] = collection.removeByIdx(indexToRemove)

  private def refreshPage(
    inputDestinationCountries: DestinationCountries
  )(implicit request: JourneyRequest[_]): Future[Result] =
    exportsCacheService.get(journeySessionId).map(_.flatMap(_.locations.destinationCountries)).map {
      case Some(cachedData) =>
        Ok(
          destinationCountriesStandardPage(Standard.form.fill(inputDestinationCountries), cachedData.countriesOfRouting)
        )
      case _ =>
        Ok(destinationCountriesStandardPage(Standard.form))
    }

  private def updateCache(sessionId: String, formData: DestinationCountries): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model =>
        exportsCacheService
          .update(sessionId, model.copy(locations = model.locations.copy(destinationCountries = Some(formData))))
    )

}
