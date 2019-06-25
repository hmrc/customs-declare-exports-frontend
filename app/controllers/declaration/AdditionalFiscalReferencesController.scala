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
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{Countries, CustomsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.additional_fiscal_references

import scala.concurrent.{ExecutionContext, Future}

class AdditionalFiscalReferencesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  mcc: MessagesControllerComponents
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {
  
  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[AdditionalFiscalReferencesData](goodsItemCacheId, formId).map {
      case Some(data) => Ok(additional_fiscal_references(form, Countries.allCountries, data.references))
      case _          => Ok(additional_fiscal_references(form, Countries.allCountries))
    }
  }

  def saveReferences(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))

    val cachedData = customsCacheService
      .fetchAndGetEntry[AdditionalFiscalReferencesData](goodsItemCacheId, formId)
      .map(_.getOrElse(AdditionalFiscalReferencesData(Seq.empty)))

    val boundForm = form.bindFromRequest()

    cachedData.flatMap { cache =>
      actionTypeOpt match {
        case Some(Add)             => addReference(boundForm, cache)
        case Some(SaveAndContinue) => saveAndContinue(boundForm, cache)
        case Some(Remove(values))  => removeReference(values, cache)
        case _                     => errorHandler.displayErrorPage()
      }
    }
  }

  private def addReference(form: Form[AdditionalFiscalReference], cachedData: AdditionalFiscalReferencesData)(
    implicit request: JourneyRequest[_]
  ): Future[Result] =
    MultipleItemsHelper
      .add(form, cachedData.references, limit)
      .fold(
        formWithErrors => Future.successful(badRequest(formWithErrors, cachedData.references)),
        updatedCache =>
          customsCacheService
            .cache[AdditionalFiscalReferencesData](
              goodsItemCacheId,
              formId,
              AdditionalFiscalReferencesData(updatedCache)
            )
            .map(_ => Redirect(routes.AdditionalFiscalReferencesController.displayPage()))
      )

  private def saveAndContinue(form: Form[AdditionalFiscalReference], cachedData: AdditionalFiscalReferencesData)(
    implicit request: JourneyRequest[_]
  ): Future[Result] =
    MultipleItemsHelper
      .saveAndContinue(form, cachedData.references, true, limit)
      .fold(
        formWithErrors => Future.successful(badRequest(formWithErrors, cachedData.references)),
        updatedCache =>
          if (updatedCache != cachedData.references)
            customsCacheService
              .cache[AdditionalFiscalReferencesData](
                goodsItemCacheId(),
                formId,
                AdditionalFiscalReferencesData(updatedCache)
              )
              .map(_ => Redirect(routes.ItemTypePageController.displayPage()))
          else Future.successful(Redirect(routes.ItemTypePageController.displayPage()))
      )

  private def removeReference(values: Seq[String], cachedData: AdditionalFiscalReferencesData)(
    implicit request: JourneyRequest[_]
  ): Future[Result] = {
    val updatedCache = MultipleItemsHelper.remove(values.headOption, cachedData.references)

    customsCacheService
      .cache[AdditionalFiscalReferencesData](goodsItemCacheId, formId, AdditionalFiscalReferencesData(updatedCache))
      .map(_ => Redirect(routes.AdditionalFiscalReferencesController.displayPage()))
  }

  private def badRequest(formWithErrors: Form[AdditionalFiscalReference], references: Seq[AdditionalFiscalReference])(
    implicit request: JourneyRequest[_]
  ): Result = BadRequest(additional_fiscal_references(formWithErrors, Countries.allCountries, references))
}
