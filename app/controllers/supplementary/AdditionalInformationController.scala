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

package controllers.supplementary

import config.AppConfig
import controllers.actions.AuthAction
import controllers.util.CacheIdGenerator.supplementaryCacheId
import controllers.util.MultipleItemsHelper._
import controllers.util._
import forms.supplementary.AdditionalInformation
import forms.supplementary.AdditionalInformation.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.supplementary.AdditionalInformationData
import models.declaration.supplementary.AdditionalInformationData.formId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.additional_information

import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[AdditionalInformationData](supplementaryCacheId, formId).map {
      case Some(data) => Ok(additional_information(appConfig, form, data.items))
      case _          => Ok(additional_information(appConfig, form, Seq()))
    }
  }

  def saveAdditionalInfo(): Action[AnyContent] = authenticate.async { implicit request =>
    val boundForm = form.bindFromRequest()

    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))

    val cachedData = customsCacheService
      .fetchAndGetEntry[AdditionalInformationData](supplementaryCacheId, formId)
      .map(_.getOrElse(AdditionalInformationData(Seq())))

    val elementLimit = 99

    cachedData.flatMap { cache =>
      actionTypeOpt match {
        case Some(Add) =>
          add(boundForm, cache.items, elementLimit).fold(
            formWithErrors => Future.successful(BadRequest(additional_information(appConfig, formWithErrors, cache.items))),
            updatedCache =>
              customsCacheService
                .cache[AdditionalInformationData](supplementaryCacheId, formId, AdditionalInformationData(updatedCache))
                .map(_ => Redirect(controllers.supplementary.routes.AdditionalInformationController.displayForm()))
          )

        case Some(Remove(ids)) => {
          val updatedCache = remove(ids.headOption, cache.items)

          customsCacheService
            .cache[AdditionalInformationData](supplementaryCacheId, formId, AdditionalInformationData(updatedCache))
            .map(_ => Redirect(controllers.supplementary.routes.AdditionalInformationController.displayForm()))
        }

        case Some(SaveAndContinue) =>
          saveAndContinue(boundForm, cache.items, true, elementLimit).fold(
            formWithErrors => Future.successful(BadRequest(additional_information(appConfig, formWithErrors, cache.items))),
            updatedCache =>
              if(updatedCache != cache.items)
                customsCacheService
                  .cache[AdditionalInformationData](supplementaryCacheId, formId, AdditionalInformationData(updatedCache))
                  .map(_ => Redirect(controllers.supplementary.routes.DocumentsProducedController.displayForm()))
              else Future.successful(Redirect(controllers.supplementary.routes.DocumentsProducedController.displayForm()))
          )

        case _ => errorHandler.displayErrorPage()
      }
    }
  }
}
