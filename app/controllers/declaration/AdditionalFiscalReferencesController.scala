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
import controllers.util.{MultipleItemsHelper, _}
import forms.declaration.AdditionalFiscalReference.form
import forms.declaration.AdditionalFiscalReferencesData._
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData, FiscalInformation}
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import services.CustomsCacheService
import services.cache.{ExportItem, ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.additional_fiscal_references

import scala.concurrent.{ExecutionContext, Future}

class AdditionalFiscalReferencesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  additionalFiscalReferencesPage: additional_fiscal_references
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[AdditionalFiscalReferencesData](goodsItemCacheId, formId).map {
      case Some(data) => Ok(additionalFiscalReferencesPage(itemId, form, data.references))
      case _          => Ok(additionalFiscalReferencesPage(itemId, form))
    }
  }

  def saveReferences(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))

      val cachedData = customsCacheService
        .fetchAndGetEntry[AdditionalFiscalReferencesData](goodsItemCacheId, formId)
        .map(_.getOrElse(AdditionalFiscalReferencesData(Seq.empty)))

      val boundForm = form.bindFromRequest()

      cachedData.flatMap { cache =>
        actionTypeOpt match {
          case Some(Add)             => addReference(itemId, boundForm, cache)
          case Some(SaveAndContinue) => saveAndContinue(itemId, boundForm, cache)
          case Some(Remove(values))  => removeReference(itemId, values, cache)
          case _                     => errorHandler.displayErrorPage()
        }
      }
  }

  private def addReference(
    itemId: String,
    form: Form[AdditionalFiscalReference],
    cachedData: AdditionalFiscalReferencesData
  )(implicit request: JourneyRequest[_]): Future[Result] =
    MultipleItemsHelper
      .add(form, cachedData.references, limit)
      .fold(
        formWithErrors => Future.successful(badRequest(itemId, formWithErrors, cachedData.references)),
        updatedCache =>
          updateCacheModels(itemId, updatedCache, routes.AdditionalFiscalReferencesController.displayPage(itemId))
      )

  private def saveAndContinue(
    itemId: String,
    form: Form[AdditionalFiscalReference],
    cachedData: AdditionalFiscalReferencesData
  )(implicit request: JourneyRequest[_]): Future[Result] =
    MultipleItemsHelper
      .saveAndContinue(form, cachedData.references, isMandatory = true, limit)
      .fold(
        formWithErrors => Future.successful(badRequest(itemId, formWithErrors, cachedData.references)),
        updatedCache =>
          if (updatedCache != cachedData.references)
            updateCacheModels(itemId, updatedCache, routes.ItemTypePageController.displayPage(itemId))
          else Future.successful(Redirect(routes.ItemTypePageController.displayPage(itemId)))
      )

  private def removeReference(itemId: String, values: Seq[String], cachedData: AdditionalFiscalReferencesData)(
    implicit request: JourneyRequest[_]
  ): Future[Result] = {
    val updatedCache = MultipleItemsHelper.remove(values.headOption, cachedData.references)
    updateCacheModels(itemId, updatedCache, routes.AdditionalFiscalReferencesController.displayPage(itemId))
  }

  private def badRequest(
    itemId: String,
    formWithErrors: Form[AdditionalFiscalReference],
    references: Seq[AdditionalFiscalReference]
  )(implicit request: JourneyRequest[_]): Result =
    BadRequest(additionalFiscalReferencesPage(itemId, formWithErrors, references))

  private def updateCacheModels(itemId: String, updatedCache: Seq[AdditionalFiscalReference], redirect: Call)(
    implicit journeyRequest: JourneyRequest[_]
  ) =
    for {
      _ <- updateExportsCache(itemId, journeySessionId, AdditionalFiscalReferencesData(updatedCache))
      _ <- customsCacheService
        .cache[AdditionalFiscalReferencesData](goodsItemCacheId, formId, AdditionalFiscalReferencesData(updatedCache))
    } yield Redirect(redirect)

  private def updateExportsCache(
    itemId: String,
    sessionId: String,
    updatedAdditionalFiscalReferencesData: AdditionalFiscalReferencesData
  ): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => {
        val item: Option[ExportItem] = model.items
          .find(item => item.id.equals(itemId))
          .map(_.copy(additionalFiscalReferencesData = Some(updatedAdditionalFiscalReferencesData)))
        val itemList = item.fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
        exportsCacheService.update(sessionId, model.copy(items = itemList))
      }
    )
}
