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

import controllers.actions.ItemActionBuilder
import controllers.navigation.Navigator
import controllers.util.{MultipleItemsHelper, _}
import forms.declaration.AdditionalFiscalReference.form
import forms.declaration.AdditionalFiscalReferencesData._
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.additional_fiscal_references

import scala.concurrent.{ExecutionContext, Future}

class AdditionalFiscalReferencesController @Inject()(
  itemAction: ItemActionBuilder,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalFiscalReferencesPage: additional_fiscal_references
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = itemAction(itemId) { implicit request =>
    request.item.additionalFiscalReferencesData.fold(Ok(additionalFiscalReferencesPage(mode, itemId, form()))) { data =>
      Ok(additionalFiscalReferencesPage(mode, itemId, form(), data.references))
    }
  }

  def saveReferences(mode: Mode, itemId: String): Action[AnyContent] = itemAction(itemId).async { implicit request =>
    val actionTypeOpt = FormAction.bindFromRequest()
    val cache = request.item.additionalFiscalReferencesData.getOrElse(AdditionalFiscalReferencesData(Seq.empty))
    val boundForm = form().bindFromRequest()

    actionTypeOpt match {
      case Add                             => addReference(mode, itemId, boundForm, cache)
      case SaveAndContinue | SaveAndReturn => saveAndContinue(mode, itemId, boundForm, cache)
      case _                               => errorHandler.displayErrorPage()
    }

  }

  private def addReference(mode: Mode, itemId: String, form: Form[AdditionalFiscalReference], cachedData: AdditionalFiscalReferencesData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(form, cachedData.references, limit)
      .fold(
        formWithErrors => Future.successful(badRequest(mode, itemId, formWithErrors, cachedData.references)),
        updatedCache =>
          updateExportsCache(itemId, AdditionalFiscalReferencesData(updatedCache))
            .map(_ => navigator.continueTo(routes.AdditionalFiscalReferencesController.displayPage(mode, itemId)))
      )

  private def saveAndContinue(mode: Mode, itemId: String, form: Form[AdditionalFiscalReference], cachedData: AdditionalFiscalReferencesData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .saveAndContinue(form, cachedData.references, isMandatory = true, limit)
      .fold(
        formWithErrors => Future.successful(badRequest(mode, itemId, formWithErrors, cachedData.references)),
        updatedCache =>
          if (updatedCache != cachedData.references)
            updateExportsCache(itemId, AdditionalFiscalReferencesData(updatedCache))
              .map(_ => navigator.continueTo(routes.CommodityDetailsController.displayPage(mode, itemId)))
          else Future.successful(navigator.continueTo(routes.CommodityDetailsController.displayPage(mode, itemId)))
      )

  def removeReference(mode: Mode, itemId: String, value: String) = itemAction(itemId).async { implicit request =>
    val cacheModel = request.item.additionalFiscalReferencesData
      .getOrElse(AdditionalFiscalReferencesData(Seq.empty))
    val updatedCache = cacheModel.removeReference(value)
    val valueOnPage = form().bindFromRequest()
    updateExportsCache(itemId, updatedCache).map {
      case Some(_) =>
        Ok(additionalFiscalReferencesPage(mode, itemId, valueOnPage.discardingErrors, updatedCache.references))
      case None => navigator.continueTo(routes.ItemsSummaryController.displayPage())
    }
  }

  private def badRequest(mode: Mode, itemId: String, formWithErrors: Form[AdditionalFiscalReference], references: Seq[AdditionalFiscalReference])(
    implicit request: JourneyRequest[AnyContent]
  ): Result =
    BadRequest(additionalFiscalReferencesPage(mode, itemId, formWithErrors, references))

  private def updateExportsCache(itemId: String, updatedAdditionalFiscalReferencesData: AdditionalFiscalReferencesData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect { model =>
      model.updateItem(itemId, item => item.copy(additionalFiscalReferencesData = Some(updatedAdditionalFiscalReferencesData)))
    }
}
