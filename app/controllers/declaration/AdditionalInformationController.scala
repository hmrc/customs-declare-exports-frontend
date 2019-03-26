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
import controllers.util.MultipleItemsHelper._
import controllers.util._
import forms.declaration.AdditionalInformation.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.AdditionalInformationData
import models.declaration.AdditionalInformationData.formId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.additional_information

import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[AdditionalInformationData](goodsItemCacheId, formId).map {
      case Some(data) => Ok(additional_information(appConfig, form, data.items))
      case _          => Ok(additional_information(appConfig, form, Seq()))
    }
  }

  def saveAdditionalInfo(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form.bindFromRequest()

    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))

    val cachedData = customsCacheService
      .fetchAndGetEntry[AdditionalInformationData](goodsItemCacheId, formId)
      .map(_.getOrElse(AdditionalInformationData(Seq())))

    val elementLimit = 99

    cachedData.flatMap { cache =>
      actionTypeOpt match {
        case Some(Add) =>
          add(boundForm, cache.items, elementLimit).fold(
            formWithErrors =>
              Future.successful(BadRequest(additional_information(appConfig, formWithErrors, cache.items))),
            updatedCache =>
              customsCacheService
                .cache[AdditionalInformationData](goodsItemCacheId, formId, AdditionalInformationData(updatedCache))
                .map(_ => Redirect(controllers.declaration.routes.AdditionalInformationController.displayForm()))
          )

        case Some(Remove(ids)) => {
          val updatedCache = remove(ids.headOption, cache.items)

          customsCacheService
            .cache[AdditionalInformationData](goodsItemCacheId, formId, AdditionalInformationData(updatedCache))
            .map(_ => Redirect(controllers.declaration.routes.AdditionalInformationController.displayForm()))
        }

        case Some(SaveAndContinue) =>
          saveAndContinue(boundForm, cache.items, true, elementLimit).fold(
            formWithErrors =>
              Future.successful(BadRequest(additional_information(appConfig, formWithErrors, cache.items))),
            updatedCache =>
              if (updatedCache != cache.items)
                customsCacheService
                  .cache[AdditionalInformationData](goodsItemCacheId, formId, AdditionalInformationData(updatedCache))
                  .map(_ => Redirect(controllers.declaration.routes.DocumentsProducedController.displayForm()))
              else
                Future.successful(Redirect(controllers.declaration.routes.DocumentsProducedController.displayForm()))
          )

        case _ => errorHandler.displayErrorPage()
      }
    }
  }
}
