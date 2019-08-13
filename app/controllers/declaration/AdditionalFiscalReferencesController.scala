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
import controllers.util.MultipleItemsHelper.remove
import controllers.util.{MultipleItemsHelper, _}
import forms.declaration.AdditionalFiscalReference.form
import forms.declaration.AdditionalFiscalReferencesData._
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import handlers.ErrorHandler
import javax.inject.Inject
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.additional_fiscal_references

import scala.concurrent.{ExecutionContext, Future}

class AdditionalFiscalReferencesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  additionalFiscalReferencesPage: additional_fiscal_references
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId) match {
      case Some(model) => {
        model.additionalFiscalReferencesData.fold(Ok(additionalFiscalReferencesPage(itemId, form()))) { data =>
          Ok(additionalFiscalReferencesPage(itemId, form(), data.references))
        }
      }
      case _ => Ok(additionalFiscalReferencesPage(itemId, form()))
    }
  }

  def saveReferences(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      val actionTypeOpt = FormAction.bindFromRequest()

      val cache = request.cacheModel
        .itemBy(itemId)
        .flatMap(_.additionalFiscalReferencesData)
        .getOrElse(AdditionalFiscalReferencesData(Seq.empty))

      val boundForm = form.bindFromRequest()

      actionTypeOpt match {
        case Some(Add)             => addReference(itemId, boundForm, cache)
        case Some(SaveAndContinue) => saveAndContinue(itemId, boundForm, cache)
        case Some(Remove(values))  => removeReference(itemId, values, boundForm, cache)
        case _                     => errorHandler.displayErrorPage()
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
          updateExportsCache(itemId, AdditionalFiscalReferencesData(updatedCache))
            .map(_ => Redirect(routes.AdditionalFiscalReferencesController.displayPage(itemId)))
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
            updateExportsCache(itemId, AdditionalFiscalReferencesData(updatedCache))
              .map(_ => Redirect(routes.ItemTypeController.displayPage(itemId)))
          else Future.successful(Redirect(routes.ItemTypeController.displayPage(itemId)))
      )

  private def removeReference(
    itemId: String,
    values: Seq[String],
    form: Form[AdditionalFiscalReference],
    cachedData: AdditionalFiscalReferencesData
  )(implicit request: JourneyRequest[_]): Future[Result] = {
    val updatedCache = cachedData.removeReferences(values)
    updateExportsCache(itemId, updatedCache).map {
      case Some(_) => Ok(additionalFiscalReferencesPage(itemId, form.discardingErrors, updatedCache.references))
      case None    => Redirect(routes.ItemsSummaryController.displayPage())
    }
  }

  private def badRequest(
    itemId: String,
    formWithErrors: Form[AdditionalFiscalReference],
    references: Seq[AdditionalFiscalReference]
  )(implicit request: JourneyRequest[_]): Result =
    BadRequest(additionalFiscalReferencesPage(itemId, formWithErrors, references))

  private def updateExportsCache(itemId: String, updatedAdditionalFiscalReferencesData: AdditionalFiscalReferencesData)(
    implicit request: JourneyRequest[_]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect { model =>
      model.updatedItem(
        itemId,
        item => item.copy(additionalFiscalReferencesData = Some(updatedAdditionalFiscalReferencesData))
      )
    }
}
