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
import controllers.actions.AuthAction
import controllers.declaration.routes.{TotalNumberOfItemsController, TransportInformationContainersPageController}
import controllers.util.CacheIdGenerator.supplementaryCacheId
import controllers.util.MultipleItemsHelper.{add, remove, saveAndContinue}
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.declaration.TransportInformationContainer
import forms.declaration.TransportInformationContainer.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.TransportInformationContainerData
import models.declaration.TransportInformationContainerData.{id, maxNumberOfItems}
import models.requests.AuthenticatedRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.add_transport_containers

import scala.concurrent.{ExecutionContext, Future}

class TransportInformationContainersPageController @Inject()(
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController with I18nSupport {

  implicit val countries = services.Countries.allCountries

  def displayPage(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[TransportInformationContainerData](supplementaryCacheId, id)
      .map {
        case Some(data) => Ok(add_transport_containers(form, data.containers))
        case _          => Ok(add_transport_containers(form, Seq()))
      }
  }

  def handlePost(): Action[AnyContent] = authenticate.async { implicit request =>
    val boundForm = form.bindFromRequest()

    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))

    val cachedData = customsCacheService
      .fetchAndGetEntry[TransportInformationContainerData](supplementaryCacheId, id)
      .map(_.getOrElse(TransportInformationContainerData(Seq())))

    cachedData.flatMap { cache =>
      actionTypeOpt match {
        case Some(Add) => addContainer(boundForm, maxNumberOfItems, cache)

        case Some(Remove(ids)) => removeContainer(cache, ids)

        case Some(SaveAndContinue) => saveContainer(boundForm, maxNumberOfItems, cache)

        case _ => errorHandler.displayErrorPage()
      }
    }
  }

  private def saveContainer(
    boundForm: Form[TransportInformationContainer],
    elementLimit: Int,
    cache: TransportInformationContainerData
  )(implicit request: AuthenticatedRequest[_], appConfig: AppConfig) =
    saveAndContinue(boundForm, cache.containers, true, elementLimit).fold(
      formWithErrors => Future.successful(BadRequest(add_transport_containers(formWithErrors, cache.containers))),
      updatedCache =>
        if (updatedCache != cache.containers)
          customsCacheService
            .cache[TransportInformationContainerData](
              supplementaryCacheId,
              id,
              TransportInformationContainerData(updatedCache)
            )
            .map(_ => Redirect(TotalNumberOfItemsController.displayForm()))
        else Future.successful(Redirect(TotalNumberOfItemsController.displayForm()))
    )

  private def removeContainer(cache: TransportInformationContainerData, ids: Seq[String])(
    implicit request: AuthenticatedRequest[_]
  ) = {
    val updatedCache = remove(ids.headOption, cache.containers)

    customsCacheService
      .cache[TransportInformationContainerData](
        supplementaryCacheId,
        id,
        TransportInformationContainerData(updatedCache)
      )
      .map(_ => Redirect(TransportInformationContainersPageController.displayPage()))
  }

  private def addContainer(
    boundForm: Form[TransportInformationContainer],
    elementLimit: Int,
    cache: TransportInformationContainerData
  )(implicit request: AuthenticatedRequest[_], appConfig: AppConfig) =
    add(boundForm, cache.containers, elementLimit).fold(
      formWithErrors => Future.successful(BadRequest(add_transport_containers(formWithErrors, cache.containers))),
      updatedCache =>
        customsCacheService
          .cache[TransportInformationContainerData](
            supplementaryCacheId,
            id,
            TransportInformationContainerData(updatedCache)
          )
          .map(_ => Redirect(TransportInformationContainersPageController.displayPage()))
    )
}
