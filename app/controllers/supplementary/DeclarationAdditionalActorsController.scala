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

package controllers.supplementary

import config.AppConfig
import controllers.actions.AuthAction
import controllers.supplementary.routes.{DeclarationAdditionalActorsController, DeclarationHolderController}
import controllers.util.CacheIdGenerator.supplementaryCacheId
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.supplementary.DeclarationAdditionalActors
import forms.supplementary.DeclarationAdditionalActors.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.supplementary.DeclarationAdditionalActorsData
import models.declaration.supplementary.DeclarationAdditionalActorsData.{formId, maxNumberOfItems}
import models.requests.AuthenticatedRequest
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.CustomsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.declaration_additional_actors

import scala.concurrent.{ExecutionContext, Future}

class DeclarationAdditionalActorsController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  private val exceedMaximumNumberError = "supplementary.additionalActors.maximumAmount.error"
  private val duplicateActorError = "supplementary.additionalActors.duplicated.error"

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[DeclarationAdditionalActorsData](supplementaryCacheId, formId).map {
      case Some(data) => Ok(declaration_additional_actors(appConfig, form(), data.actors))
      case _          => Ok(declaration_additional_actors(appConfig, form(), Seq()))
    }
  }

  def saveForm(): Action[AnyContent] = authenticate.async { implicit request =>
    val boundForm = form().bindFromRequest()
    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded)
    val cachedData = customsCacheService
      .fetchAndGetEntry[DeclarationAdditionalActorsData](supplementaryCacheId, formId)
      .map(_.getOrElse(DeclarationAdditionalActorsData(Seq())))

    cachedData.flatMap { cache =>
      boundForm
        .fold(
          (formWithErrors: Form[DeclarationAdditionalActors]) =>
            Future.successful(BadRequest(declaration_additional_actors(appConfig, formWithErrors, cache.actors))),
          validForm =>
            actionTypeOpt match {
              case Some(Add)             => addItem(validForm, cache)
              case Some(SaveAndContinue) => saveAndContinue(validForm, cache)
              case Some(Remove(values))  => removeItem(retrieveItem(values.headOption.get), cache)
              case _                     => errorHandler.displayErrorPage()
          }
        )
    }
  }

  private def addItem(
    userInput: DeclarationAdditionalActors,
    cachedData: DeclarationAdditionalActorsData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cachedData.actors) match {
      case (_, actors) if actors.length >= maxNumberOfItems =>
        handleErrorPage(Seq(("", exceedMaximumNumberError)), userInput, cachedData.actors)

      case (actor, actors) if actors.contains(actor) =>
        handleErrorPage(Seq(("", duplicateActorError)), userInput, cachedData.actors)

      case (actor, actors) =>
        if (actor.isDefined) {
          val updatedCache = DeclarationAdditionalActorsData(actors :+ actor)
          customsCacheService
            .cache[DeclarationAdditionalActorsData](supplementaryCacheId, formId, updatedCache)
            .map(_ => Redirect(DeclarationAdditionalActorsController.displayForm()))
        } else
          handleErrorPage(Seq(("", "supplementary.additionalActors.eori.isNotDefined")), userInput, cachedData.actors)
    }

  private def retrieveItem(value: String): Option[DeclarationAdditionalActors] = DeclarationAdditionalActors.fromJsonString(value)

  private def saveAndContinue(
    userInput: DeclarationAdditionalActors,
    cacheData: DeclarationAdditionalActorsData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cacheData.actors) match {
      case (actor, Seq())  => saveAndRedirect(actor, Seq())
      case (actor, actors) => handleSaveAndContinueCache(actor, actors)
    }

  private def saveAndRedirect(
    actor: DeclarationAdditionalActors,
    actors: Seq[DeclarationAdditionalActors]
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    if (actor.isDefined)
      customsCacheService
        .cache[DeclarationAdditionalActorsData](
          supplementaryCacheId,
          formId,
          DeclarationAdditionalActorsData(actors :+ actor)
        )
        .map { _ =>
          Redirect(DeclarationHolderController.displayForm())
        } else Future.successful(Redirect(DeclarationHolderController.displayForm()))

  private def handleSaveAndContinueCache(actor: DeclarationAdditionalActors, actors: Seq[DeclarationAdditionalActors])(
    implicit request: AuthenticatedRequest[_]
  ) =
    if (actors.length >= maxNumberOfItems) {
      handleErrorPage(Seq(("", exceedMaximumNumberError)), actor, actors)
    } else if (actors.contains(actor)) {
      handleErrorPage(Seq(("", duplicateActorError)), actor, actors)
    } else {
      saveAndRedirect(actor, actors)
    }

  private def removeItem(
    actorToRemove: Option[DeclarationAdditionalActors],
    cachedData: DeclarationAdditionalActorsData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =

    actorToRemove match {
      case Some(actorToRemove) =>
        if (cachedData.containsItem(actorToRemove)) {
          val updatedCache = cachedData.copy(actors = cachedData.actors.filterNot(_ == actorToRemove))

          customsCacheService.cache[DeclarationAdditionalActorsData](supplementaryCacheId, formId, updatedCache).map { _ =>
            Redirect(DeclarationAdditionalActorsController.displayForm())
          }
        } else errorHandler.displayErrorPage()
      case _ => errorHandler.displayErrorPage()
    }


  private def handleErrorPage(
    fieldWithError: Seq[(String, String)],
    userInput: DeclarationAdditionalActors,
    actors: Seq[DeclarationAdditionalActors]
  )(implicit request: Request[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form().fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(declaration_additional_actors(appConfig, formWithError, actors)))
  }
}
