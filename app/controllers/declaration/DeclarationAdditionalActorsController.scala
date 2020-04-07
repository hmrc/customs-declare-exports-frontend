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
import controllers.util.MultipleItemsHelper.remove
import controllers.util._
import forms.declaration.DeclarationAdditionalActors
import forms.declaration.DeclarationAdditionalActors.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.DeclarationAdditionalActorsData
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
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
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarationAdditionalActorsPage: declaration_additional_actors
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  val validTypes =
    Seq(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL)

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    request.cacheModel.parties.declarationAdditionalActorsData match {
      case Some(data) => Ok(declarationAdditionalActorsPage(mode, form(), data.actors))
      case _          => Ok(declarationAdditionalActorsPage(mode, form(), Seq()))
    }
  }

  def saveForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    val actionTypeOpt = FormAction.bindFromRequest()

    val cache =
      request.cacheModel.parties.declarationAdditionalActorsData.getOrElse(DeclarationAdditionalActorsData(Seq()))

    actionTypeOpt match {
      case Add if !boundForm.hasErrors => addItem(mode, boundForm.get, cache)
      case SaveAndContinue | SaveAndReturn if !boundForm.hasErrors =>
        saveAndContinue(mode, boundForm.get, cache)
      case Remove(values) => removeItem(mode, retrieveItem(values.headOption.get), boundForm, cache)
      case _              => Future.successful(BadRequest(declarationAdditionalActorsPage(mode, boundForm, cache.actors)))
    }
  }

  private def addItem(mode: Mode, userInput: DeclarationAdditionalActors, cachedData: DeclarationAdditionalActorsData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    actorsValidator(mode, userInput, cachedData.actors) {
      val updatedCache = DeclarationAdditionalActorsData(cachedData.actors).addActor(userInput)
      updateCache(updatedCache)
        .map(_ => navigator.continueTo(mode, routes.DeclarationAdditionalActorsController.displayPage))
    }

  private def actorsValidator(mode: Mode, actor: DeclarationAdditionalActors, existingActors: Seq[DeclarationAdditionalActors])(
    onSuccess: Future[Result]
  )(implicit request: JourneyRequest[AnyContent], hc: HeaderCarrier): Future[Result] =
    DeclarationAdditionalActorsData
      .actorsValidator(actor, existingActors)
      .fold[Future[Result]](onSuccess)(errors => handleErrorPage(mode, errors, actor, existingActors))

  private def handleErrorPage(
    mode: Mode,
    fieldWithError: Seq[(String, String)],
    userInput: DeclarationAdditionalActors,
    actors: Seq[DeclarationAdditionalActors]
  )(implicit request: JourneyRequest[AnyContent]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form().fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(declarationAdditionalActorsPage(mode, formWithError, actors)))
  }

  private def updateCache(formData: DeclarationAdditionalActorsData)(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedParties = model.parties.copy(declarationAdditionalActorsData = Some(formData))
      model.copy(parties = updatedParties)
    })

  private def retrieveItem(value: String): Option[DeclarationAdditionalActors] =
    DeclarationAdditionalActors.fromJsonString(value)

  private def saveAndContinue(mode: Mode, userInput: DeclarationAdditionalActors, cacheData: DeclarationAdditionalActorsData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    (userInput, cacheData.actors) match {
      case (actor, Seq())  => saveAndRedirect(mode, actor, Seq())
      case (actor, actors) => handleSaveAndContinueCache(mode, actor, actors)
    }

  private def handleSaveAndContinueCache(mode: Mode, actor: DeclarationAdditionalActors, existingActors: Seq[DeclarationAdditionalActors])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = actorsValidator(mode, actor, existingActors)(saveAndRedirect(mode, actor, existingActors))

  private def saveAndRedirect(mode: Mode, actor: DeclarationAdditionalActors, actors: Seq[DeclarationAdditionalActors])(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    if (actor.isDefined) {
      updateCache(DeclarationAdditionalActorsData(actors).addActor(actor))
        .map(_ => navigator.continueTo(mode, routes.DeclarationHolderController.displayPage))
    } else updateCache(DeclarationAdditionalActorsData(actors)).map(_ => navigator.continueTo(mode, routes.DeclarationHolderController.displayPage))

  private def removeItem(
    mode: Mode,
    actorToRemove: Option[DeclarationAdditionalActors],
    formData: Form[DeclarationAdditionalActors],
    cachedData: DeclarationAdditionalActorsData
  )(implicit request: JourneyRequest[AnyContent], hc: HeaderCarrier): Future[Result] = {
    val updatedCache =
      cachedData.copy(actors = remove(cachedData.actors, actorToRemove.contains(_: DeclarationAdditionalActors)))
    updateCache(updatedCache)
      .map(_ => navigator.continueTo(mode, routes.DeclarationAdditionalActorsController.displayPage))
  }
}
