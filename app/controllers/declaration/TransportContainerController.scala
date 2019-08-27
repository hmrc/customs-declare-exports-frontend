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
import controllers.util.MultipleItemsHelper.{add, remove, saveAndContinue}
import controllers.util._
import forms.Choice.AllowedChoiceValues
import forms.declaration.TransportInformationContainer
import forms.declaration.TransportInformationContainer.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.TransportInformationContainerData
import models.declaration.TransportInformationContainerData.maxNumberOfItems
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
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
  navigator: Navigator,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  transportContainersPage: add_transport_containers
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.containerData match {
      case Some(data) => Ok(transportContainersPage(mode, form(), data.containers))
      case _          => Ok(transportContainersPage(mode, form(), Seq()))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    val actionTypeOpt = FormAction.bindFromRequest()

    val cache = request.cacheModel.containerData.getOrElse(TransportInformationContainerData(Seq()))

    actionTypeOpt match {
      case Some(Add)                                   => addContainer(mode, boundForm, maxNumberOfItems, cache)
      case Some(Remove(ids))                           => removeContainer(mode, boundForm, cache, ids)
      case Some(SaveAndContinue) | Some(SaveAndReturn) => saveContainer(mode, boundForm, maxNumberOfItems, cache)
      case _                                           => errorHandler.displayErrorPage()
    }
  }

  private def saveContainer(
    mode: Mode,
    boundForm: Form[TransportInformationContainer],
    elementLimit: Int,
    cache: TransportInformationContainerData
  )(implicit request: JourneyRequest[AnyContent]) =
    saveAndContinue(boundForm, cache.containers, isMandatory = true, elementLimit).fold(
      formWithErrors => Future.successful(BadRequest(transportContainersPage(mode, formWithErrors, cache.containers))),
      updatedCache =>
        if (updatedCache != cache.containers)
          updateCache(TransportInformationContainerData(updatedCache))
            .map(_ => redirect(mode))
        else Future.successful(redirect(mode))
    )

  private def redirect(mode: Mode)(implicit request: JourneyRequest[AnyContent]) =
    if (request.choice.value == AllowedChoiceValues.StandardDec)
      navigator.continueTo(controllers.declaration.routes.SealController.displayForm(mode))
    else navigator.continueTo(controllers.declaration.routes.SummaryController.displayPage(mode))

  private def updateCache(
    formData: TransportInformationContainerData
  )(implicit r: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => model.copy(containerData = Some(formData)))

  private def removeContainer(
    mode: Mode,
    userInput: Form[TransportInformationContainer],
    cache: TransportInformationContainerData,
    ids: Seq[String]
  )(implicit request: JourneyRequest[_]) = {
    val updatedCache = remove(cache.containers, { container: TransportInformationContainer =>
      ids.contains(container.id)
    })
    updateCache(TransportInformationContainerData(updatedCache)).map { _ =>
      Ok(transportContainersPage(mode, userInput.discardingErrors, updatedCache))
    }
  }

  private def addContainer(
    mode: Mode,
    boundForm: Form[TransportInformationContainer],
    elementLimit: Int,
    cache: TransportInformationContainerData
  )(implicit request: JourneyRequest[AnyContent]) =
    add(boundForm, cache.containers, elementLimit).fold(
      formWithErrors => Future.successful(BadRequest(transportContainersPage(mode, formWithErrors, cache.containers))),
      updatedCache =>
        updateCache(TransportInformationContainerData(updatedCache))
          .map(_ => navigator.continueTo(controllers.declaration.routes.TransportContainerController.displayPage(mode)))
    )
}
