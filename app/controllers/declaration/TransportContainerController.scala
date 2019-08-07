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
import controllers.util.MultipleItemsHelper.{add, remove, saveAndContinue}
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.Choice.AllowedChoiceValues
import forms.declaration.TransportInformationContainer
import forms.declaration.TransportInformationContainer.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.ExportsCacheModel
import models.declaration.TransportInformationContainerData
import models.declaration.TransportInformationContainerData.maxNumberOfItems
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.add_transport_containers

import scala.concurrent.{ExecutionContext, Future}

class TransportContainerController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  transportContainersPage: add_transport_containers
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    exportsCacheService.get(journeySessionId).map(_.flatMap(_.containerData)).map {
      case Some(data) => Ok(transportContainersPage(form, data.containers))
      case _          => Ok(transportContainersPage(form, Seq()))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    val actionTypeOpt = FormAction.bindFromRequest()

    val cachedData = exportsCacheService
      .get(journeySessionId)
      .map(_.flatMap(_.containerData))
      .map(_.getOrElse(TransportInformationContainerData(Seq())))

    cachedData.flatMap { cache =>
      actionTypeOpt match {
        case Some(Add)             => addContainer(boundForm, maxNumberOfItems, cache)
        case Some(Remove(ids))     => removeContainer(boundForm, cache, ids)
        case Some(SaveAndContinue) => saveContainer(boundForm, maxNumberOfItems, cache)
        case _                     => errorHandler.displayErrorPage()
      }
    }
  }

  private def saveContainer(
    boundForm: Form[TransportInformationContainer],
    elementLimit: Int,
    cache: TransportInformationContainerData
  )(implicit request: JourneyRequest[_]) =
    saveAndContinue(boundForm, cache.containers, isMandatory = true, elementLimit).fold(
      formWithErrors => Future.successful(BadRequest(transportContainersPage(formWithErrors, cache.containers))),
      updatedCache =>
        if (updatedCache != cache.containers)
          updateCache(journeySessionId, TransportInformationContainerData(updatedCache))
            .map(_ => redirect())
        else Future.successful(redirect())
    )

  private def redirect()(implicit request: JourneyRequest[_]) =
    if (request.choice.value == AllowedChoiceValues.StandardDec) Redirect(routes.SealController.displayForm())
    else Redirect(routes.SummaryController.displayPage())

  private def removeContainer(
    userInput: Form[TransportInformationContainer],
    cache: TransportInformationContainerData,
    ids: Seq[String]
  )(implicit request: JourneyRequest[_]) = {
    val updatedCache = remove(ids.headOption, cache.containers)
    updateCache(journeySessionId, TransportInformationContainerData(updatedCache)).map { _ =>
      Ok(transportContainersPage(userInput.discardingErrors, updatedCache))
    }
  }

  private def addContainer(
    boundForm: Form[TransportInformationContainer],
    elementLimit: Int,
    cache: TransportInformationContainerData
  )(implicit request: JourneyRequest[_]) =
    add(boundForm, cache.containers, elementLimit).fold(
      formWithErrors => Future.successful(BadRequest(transportContainersPage(formWithErrors, cache.containers))),
      updatedCache =>
        updateCache(journeySessionId, TransportInformationContainerData(updatedCache))
          .map(_ => Redirect(routes.TransportContainerController.displayPage()))
    )

  private def updateCache(
    sessionId: String,
    formData: TransportInformationContainerData
  ): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => exportsCacheService.update(sessionId, model.copy(containerData = Some(formData)))
    )
}
