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
import forms.Choice.AllowedChoiceValues
import forms.declaration.TransportInformationContainer
import forms.declaration.TransportInformationContainer.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.TransportInformationContainerData
import models.declaration.TransportInformationContainerData.{id, maxNumberOfItems}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import services.cache.{ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.add_transport_containers

import scala.concurrent.{ExecutionContext, Future}

class TransportContainerController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  transportContainersPage: add_transport_containers
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    cacheService.get(journeySessionId).map(_.flatMap(_.containerData)).map {
      case Some(data) => Ok(transportContainersPage(form(), data.containers))
      case _          => Ok(transportContainersPage(form(), Seq()))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()

    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded)

    val cachedData = cacheService
      .get(journeySessionId)
      .map(_.flatMap(_.containerData))
      .map(_.getOrElse(TransportInformationContainerData(Seq())))

    cachedData.flatMap { cache =>
      actionTypeOpt match {
        case Some(Add)             => addContainer(boundForm, maxNumberOfItems, cache)
        case Some(Remove(ids))     => removeContainer(cache, ids)
        case Some(SaveAndContinue) => saveContainer(boundForm, maxNumberOfItems, cache)
        case _                     => errorHandler.displayErrorPage()
      }
    }
  }

  private def saveContainer(
    boundForm: Form[TransportInformationContainer],
    elementLimit: Int,
    cache: TransportInformationContainerData
  )(implicit request: JourneyRequest[_], appConfig: AppConfig) =
    saveAndContinue(boundForm, cache.containers, true, elementLimit).fold(
      formWithErrors => Future.successful(BadRequest(transportContainersPage(formWithErrors, cache.containers))),
      updatedCache =>
        if (updatedCache != cache.containers)
          for {
            _ <- updateCache(journeySessionId, TransportInformationContainerData(updatedCache))
            _ <- customsCacheService
              .cache[TransportInformationContainerData](cacheId, id, TransportInformationContainerData(updatedCache))
          } yield redirect()
        else Future.successful(redirect())
    )

  private def redirect()(implicit request: JourneyRequest[_]) =
    if (request.choice.value == AllowedChoiceValues.StandardDec) Redirect(routes.SealController.displayForm())
    else Redirect(routes.SummaryPageController.displayPage())

  private def removeContainer(cache: TransportInformationContainerData, ids: Seq[String])(
    implicit request: JourneyRequest[_]
  ) =
    cacheAndRedirect(remove(ids.headOption, cache.containers))

  private def addContainer(
    boundForm: Form[TransportInformationContainer],
    elementLimit: Int,
    cache: TransportInformationContainerData
  )(implicit request: JourneyRequest[_], appConfig: AppConfig) =
    add(boundForm, cache.containers, elementLimit).fold(
      formWithErrors => Future.successful(BadRequest(transportContainersPage(formWithErrors, cache.containers))),
      updatedCache => cacheAndRedirect(updatedCache)
    )

  private def cacheAndRedirect(containers: Seq[TransportInformationContainer])(implicit request: JourneyRequest[_]) =
    for {
      _ <- updateCache(journeySessionId, TransportInformationContainerData(containers))
      _ <- customsCacheService
        .cache[TransportInformationContainerData](cacheId, id, TransportInformationContainerData(containers))
    } yield Redirect(routes.TransportContainerController.displayPage())

  private def updateCache(
    sessionId: String,
    formData: TransportInformationContainerData
  ): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => exportsCacheService.update(sessionId, model.copy(containerData = Some(formData)))
    )
}
