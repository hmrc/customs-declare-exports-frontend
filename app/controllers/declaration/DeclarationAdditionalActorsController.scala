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
import controllers.util.MultipleItemsHelper.remove
import controllers.util._
import forms.declaration.DeclarationAdditionalActors
import forms.declaration.DeclarationAdditionalActors.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.ExportsDeclaration
import models.declaration.DeclarationAdditionalActorsData
import models.declaration.DeclarationAdditionalActorsData.maxNumberOfItems
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.declaration_additional_actors

import scala.concurrent.{ExecutionContext, Future}

class DeclarationAdditionalActorsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  declarationAdditionalActorsPage: declaration_additional_actors
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  private val exceedMaximumNumberError = "supplementary.additionalActors.maximumAmount.error"
  private val duplicateActorError = "supplementary.additionalActors.duplicated.error"

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.parties.declarationAdditionalActorsData match {
      case Some(data) => Ok(declarationAdditionalActorsPage(form(), data.actors))
      case _          => Ok(declarationAdditionalActorsPage(form(), Seq()))
    }
  }

  def saveForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    val actionTypeOpt = FormAction.bindFromRequest()

    val cachedData = exportsCacheService
      .get(journeySessionId)
      .map(
        _.flatMap(_.parties.declarationAdditionalActorsData)
          .getOrElse(DeclarationAdditionalActorsData(Seq()))
      )

    cachedData.flatMap { cache =>
      actionTypeOpt match {
        case Some(Add) if !boundForm.hasErrors             => addItem(boundForm.get, cache)
        case Some(SaveAndContinue) if !boundForm.hasErrors => saveAndContinue(boundForm.get, cache)
        case Some(Remove(values))                          => removeItem(retrieveItem(values.headOption.get), boundForm, cache)
        case _                                             => Future.successful(BadRequest(declarationAdditionalActorsPage(boundForm, cache.actors)))
      }
    }
  }

  private def addItem(
    userInput: DeclarationAdditionalActors,
    cachedData: DeclarationAdditionalActorsData
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cachedData.actors) match {
      case (_, actors) if actors.length >= maxNumberOfItems =>
        handleErrorPage(Seq(("", exceedMaximumNumberError)), userInput, cachedData.actors)

      case (actor, actors) if actors.contains(actor) =>
        handleErrorPage(Seq(("", duplicateActorError)), userInput, cachedData.actors)

      case (actor, actors) =>
        if (actor.isDefined) {
          val updatedCache = DeclarationAdditionalActorsData(actors :+ actor)
          updateCache(journeySessionId, updatedCache)
            .map(_ => Redirect(routes.DeclarationAdditionalActorsController.displayForm()))
        } else
          handleErrorPage(
            Seq(("eori", "supplementary.additionalActors.eori.isNotDefined")),
            userInput,
            cachedData.actors
          )
    }

  private def handleErrorPage(
    fieldWithError: Seq[(String, String)],
    userInput: DeclarationAdditionalActors,
    actors: Seq[DeclarationAdditionalActors]
  )(implicit request: JourneyRequest[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form().fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(declarationAdditionalActorsPage(formWithError, actors)))
  }

  private def updateCache(
    sessionId: String,
    formData: DeclarationAdditionalActorsData
  ): Future[Option[ExportsDeclaration]] =
    getAndUpdateExportsDeclaration(sessionId, model => {
      val updatedParties = model.parties.copy(declarationAdditionalActorsData = Some(formData))
      exportsCacheService.update(sessionId, model.copy(parties = updatedParties))
    })

  private def retrieveItem(value: String): Option[DeclarationAdditionalActors] =
    DeclarationAdditionalActors.fromJsonString(value)

  private def saveAndContinue(
    userInput: DeclarationAdditionalActors,
    cacheData: DeclarationAdditionalActorsData
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cacheData.actors) match {
      case (actor, Seq())  => saveAndRedirect(actor, Seq())
      case (actor, actors) => handleSaveAndContinueCache(actor, actors)
    }

  private def handleSaveAndContinueCache(actor: DeclarationAdditionalActors, actors: Seq[DeclarationAdditionalActors])(
    implicit request: JourneyRequest[_]
  ) =
    if (actors.length >= maxNumberOfItems) {
      handleErrorPage(Seq(("", exceedMaximumNumberError)), actor, actors)
    } else if (actors.contains(actor)) {
      handleErrorPage(Seq(("", duplicateActorError)), actor, actors)
    } else {
      saveAndRedirect(actor, actors)
    }

  private def saveAndRedirect(
    actor: DeclarationAdditionalActors,
    actors: Seq[DeclarationAdditionalActors]
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Result] =
    if (actor.isDefined) {
      val updatedCache = DeclarationAdditionalActorsData(actors :+ actor)
      updateCache(journeySessionId, updatedCache).map(_ => Redirect(routes.DeclarationHolderController.displayForm()))
    } else Future.successful(Redirect(routes.DeclarationHolderController.displayForm()))

  private def removeItem(
    actorToRemove: Option[DeclarationAdditionalActors],
    formData: Form[DeclarationAdditionalActors],
    cachedData: DeclarationAdditionalActorsData
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Result] = {
    val updatedCache = cachedData.copy(actors = remove(cachedData.actors, actorToRemove.contains(_: DeclarationAdditionalActors)))
    updateCache(journeySessionId, updatedCache)
      .map(_ => Ok(declarationAdditionalActorsPage(formData.discardingErrors, updatedCache.actors)))
  }
}
