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
import controllers.declaration.routes.SummaryPageController
import controllers.util.CacheIdGenerator.cacheId
import controllers.util.MultipleItemsHelper.{add, remove, saveAndContinue}
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.declaration.{Seal, TransportDetails}
import forms.declaration.Seal._
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.seal

import scala.concurrent.{ExecutionContext, Future}

class SealController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  cacheService: CustomsCacheService,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    cacheService
      .fetchAndGetEntry[Seq[Seal]](cacheId, formId)
      .flatMap { seals =>
        cacheService
          .fetchAndGetEntry[TransportDetails](cacheId, TransportDetails.formId)
          .map(data => Ok(seal(form, seals.getOrElse(Seq.empty), data.fold(false)(_.container))))
      }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))

    cacheService
      .fetchAndGetEntry[Seq[Seal]](cacheId, formId)
      .flatMap(cache => processRequest(cache.getOrElse(Seq.empty), actionTypeOpt))
  }

  private def processRequest(cachedSeals: Seq[Seal], action: Option[FormAction])(
    implicit request: JourneyRequest[_]
  ): Future[Result] = {
    val boundForm = form.bindFromRequest()
    action match {
      case Some(Add) => addSeal(boundForm, sealsAllowed, cachedSeals)

      case Some(Remove(ids)) => removeSeal(cachedSeals, ids)

      case Some(SaveAndContinue) => saveSeal(boundForm, sealsAllowed, cachedSeals)

      case _ => errorHandler.displayErrorPage()
    }
  }

  private def saveSeal(boundForm: Form[Seal], elementLimit: Int, cachedSeals: Seq[Seal])(
    implicit request: JourneyRequest[_],
    appConfig: AppConfig
  ): Future[Result] =
    saveAndContinue(boundForm, cachedSeals, false, elementLimit).fold(
      formWithErrors => badRequest(formWithErrors, cachedSeals),
      updatedCache =>
        if (updatedCache != cachedSeals)
          cacheService
            .cache[Seq[Seal]](cacheId, formId, updatedCache)
            .map(_ => Redirect(SummaryPageController.displayPage()))
        else Future.successful(Redirect(SummaryPageController.displayPage()))
    )

  private def removeSeal(cachedSeals: Seq[Seal], ids: Seq[String])(
    implicit request: JourneyRequest[_]
  ): Future[Result] =
    cacheAndRedirect(remove(ids.headOption, cachedSeals))

  private def addSeal(boundForm: Form[Seal], elementLimit: Int, seals: Seq[Seal])(
    implicit request: JourneyRequest[_],
    appConfig: AppConfig
  ): Future[Result] =
    add(boundForm, seals, elementLimit)
      .fold(formWithErrors => badRequest(formWithErrors, seals), updatedCache => cacheAndRedirect(updatedCache))

  private def badRequest(
    formWithErrors: Form[Seal],
    cachedSeals: Seq[Seal]
  )(implicit request: JourneyRequest[_], appConfig: AppConfig) =
    cacheService
      .fetchAndGetEntry[TransportDetails](cacheId, TransportDetails.formId)
      .map(data => BadRequest(seal(formWithErrors, cachedSeals, data.fold(false)(_.container))))

  private def cacheAndRedirect(seals: Seq[Seal])(implicit request: JourneyRequest[_]): Future[Result] =
    cacheService
      .cache[Seq[Seal]](cacheId, formId, seals)
      .map(_ => Redirect(routes.SealController.displayForm()))
}
