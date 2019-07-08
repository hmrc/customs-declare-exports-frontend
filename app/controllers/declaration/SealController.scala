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
import controllers.util.MultipleItemsHelper.{add, remove, saveAndContinue}
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.declaration.Seal._
import forms.declaration.{Seal, TransportDetails}
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.CustomsCacheService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.seal

import scala.concurrent.{ExecutionContext, Future}

class SealController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  override val cacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  sealPage: seal
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[Seq[Seal]](cacheId, formId)
      .flatMap { seals =>
        customsCacheService
          .fetchAndGetEntry[TransportDetails](cacheId, TransportDetails.formId)
          .map(data => Ok(sealPage(form, seals.getOrElse(Seq.empty), data.fold(false)(_.container))))
      }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))

    customsCacheService
      .fetchAndGetEntry[Seq[Seal]](cacheId, formId)
      .flatMap(cache => processRequest(cache.getOrElse(Seq.empty), actionTypeOpt))
  }

  private def processRequest(cachedSeals: Seq[Seal], action: Option[FormAction])(
    implicit request: JourneyRequest[_]
  ): Future[Result] = {
    val boundForm = form.bindFromRequest()
    action match {
      case Some(Add)             => addSeal(boundForm, sealsAllowed, cachedSeals)
      case Some(Remove(ids))     => removeSeal(cachedSeals, ids)
      case Some(SaveAndContinue) => saveSeal(boundForm, sealsAllowed, cachedSeals)
      case _                     => errorHandler.displayErrorPage()
    }
  }

  private def saveSeal(boundForm: Form[Seal], elementLimit: Int, cachedSeals: Seq[Seal])(
    implicit request: JourneyRequest[_],
    appConfig: AppConfig
  ): Future[Result] =
    saveAndContinue(boundForm, cachedSeals, false, elementLimit).fold(
      formWithErrors => badRequest(formWithErrors, cachedSeals),
      updatedCache =>
        if (updatedCache != cachedSeals) {
          updateCache(journeySessionId, updatedCache).map { _ =>
            Redirect(routes.SummaryPageController.displayPage())
          }
        } else Future.successful(Redirect(routes.SummaryPageController.displayPage()))
    )

  private def badRequest(
    formWithErrors: Form[Seal],
    cachedSeals: Seq[Seal]
  )(implicit request: JourneyRequest[_], appConfig: AppConfig) =
    customsCacheService
      .fetchAndGetEntry[TransportDetails](cacheId, TransportDetails.formId)
      .map(data => BadRequest(sealPage(formWithErrors, cachedSeals, data.fold(false)(_.container))))

  private def removeSeal(cachedSeals: Seq[Seal], ids: Seq[String])(
    implicit request: JourneyRequest[_]
  ): Future[Result] =
    updateCache(journeySessionId, remove(ids.headOption, cachedSeals)).map { _ =>
      Redirect(routes.SealController.displayForm())
    }

  private def updateCache(sessionId: String, formData: Seq[Seal])(implicit req: JourneyRequest[_]): Future[Unit] =
    for {
      _ <- getAndUpdateExportCacheModel(
        sessionId,
        model =>
          cacheService
            .update(sessionId, model.copy(seals = formData))
      )
      _ <- customsCacheService.cache[Seq[Seal]](cacheId, formId, formData)
    } yield Unit

  private def addSeal(boundForm: Form[Seal], elementLimit: Int, seals: Seq[Seal])(
    implicit request: JourneyRequest[_],
    appConfig: AppConfig
  ): Future[Result] =
    add(boundForm, seals, elementLimit)
      .fold(
        formWithErrors => badRequest(formWithErrors, seals),
        updatedCache =>
          updateCache(journeySessionId, updatedCache).map { _ =>
            Redirect(routes.SealController.displayForm())
        }
      )
}
