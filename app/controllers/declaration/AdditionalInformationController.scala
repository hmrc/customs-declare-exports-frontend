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
import controllers.util.CacheIdGenerator.goodsItemCacheId
import controllers.util._
import forms.declaration.AdditionalInformation
import forms.declaration.AdditionalInformation.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.AdditionalInformationData
import models.declaration.AdditionalInformationData.formId
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import services.CustomsCacheService
import services.cache.{ExportItem, ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.additional_information

import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationController @Inject()(
  appConfig: AppConfig,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  legacyCustomsCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  additionalInformationPage: additional_information
)(implicit ec: ExecutionContext)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  val elementLimit = 99

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    exportsCacheService.getItemByIdAndSession(itemId, journeySessionId).map(_.flatMap(_.additionalInformation)).map {
      case Some(data) => Ok(additionalInformationPage(itemId, appConfig, form(), data.items))
      case _          => Ok(additionalInformationPage(itemId, appConfig, form(), Seq()))
    }
  }

  def saveAdditionalInfo(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      val boundForm = form().bindFromRequest()

      val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded)

      val cachedData = exportsCacheService
        .getItemByIdAndSession(itemId, journeySessionId)
        .map(_.flatMap(_.additionalInformation).getOrElse(AdditionalInformationData(Seq())))

      cachedData.flatMap { cache =>
        actionTypeOpt match {
          case Some(Add)             => handleAdd(itemId, boundForm, cache.items)
          case Some(Remove(ids))     => handleRemove(itemId, ids, boundForm, cache.items)
          case Some(SaveAndContinue) => handleSaveAndContinue(itemId, boundForm, cache.items)
          case _                     => errorHandler.displayErrorPage()
        }
      }
  }

  private def handleAdd(itemId: String, boundForm: Form[AdditionalInformation], cachedData: Seq[AdditionalInformation])(
    implicit request: JourneyRequest[_]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, elementLimit)
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(additionalInformationPage(itemId, appConfig, formWithErrors, cachedData))),
        updatedCache =>
          updateCacheModelAndRedirect(
            itemId,
            updatedCache,
            controllers.declaration.routes.AdditionalInformationController.displayPage(itemId)
        )
      )

  private def handleSaveAndContinue(
    itemId: String,
    boundForm: Form[AdditionalInformation],
    cachedData: Seq[AdditionalInformation]
  )(implicit request: JourneyRequest[_]): Future[Result] =
    MultipleItemsHelper
      .saveAndContinue(boundForm, cachedData, true, elementLimit)
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(additionalInformationPage(itemId, appConfig, formWithErrors, cachedData))),
        updatedCache =>
          if (updatedCache != cachedData)
            updateCacheModelAndRedirect(
              itemId,
              updatedCache,
              controllers.declaration.routes.DocumentsProducedController.displayPage(itemId)
            )
          else
            Future.successful(Redirect(controllers.declaration.routes.DocumentsProducedController.displayPage(itemId)))
      )

  private def handleRemove(
    itemId: String,
    ids: Seq[String],
    boundForm: Form[AdditionalInformation],
    items: Seq[AdditionalInformation]
  )(implicit request: JourneyRequest[_]): Future[Result] = {
    val updatedCache = MultipleItemsHelper.remove(ids.headOption, items)
    updateCacheModelAndRedirect(
      itemId,
      updatedCache,
      controllers.declaration.routes.AdditionalInformationController.displayPage(itemId)
    )
  }

  private def updateCacheModelAndRedirect(itemId: String, updatedCache: Seq[AdditionalInformation], redirectCall: Call)(
    implicit request: JourneyRequest[_]
  ): Future[Result] =
    for {
      _ <- updateCache(itemId, journeySessionId, AdditionalInformationData(updatedCache))
      _ <- legacyCustomsCacheService
        .cache[AdditionalInformationData](goodsItemCacheId, formId, AdditionalInformationData(updatedCache))
    } yield Redirect(redirectCall)

  private def updateCache(
    itemId: String,
    sessionId: String,
    updatedAdditionalInformation: AdditionalInformationData
  ): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => {
        val item: Option[ExportItem] = model.items
          .find(item => item.id.equals(itemId))
          .map(_.copy(additionalInformation = Some(updatedAdditionalInformation)))
        val itemList = item.fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
        exportsCacheService.update(sessionId, model.copy(items = itemList))
      }
    )
}
