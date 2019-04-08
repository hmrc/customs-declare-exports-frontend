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
import controllers.util.CacheIdGenerator.cacheId
import controllers.util._
import forms.declaration.Document._
import forms.declaration.PreviousDocumentsData
import forms.declaration.PreviousDocumentsData._
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.previous_documents

import scala.concurrent.{ExecutionContext, Future}

class PreviousDocumentsController @Inject()(
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController with I18nSupport {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[PreviousDocumentsData](cacheId, formId).map {
      case Some(data) => Ok(previous_documents(form, data.documents))
      case _          => Ok(previous_documents(form, Seq.empty))
    }
  }

  def savePreviousDocuments(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    import MultipleItemsHelper._

    val boundForm = form.bindFromRequest()

    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))

    val cachedData = customsCacheService
      .fetchAndGetEntry[PreviousDocumentsData](cacheId, formId)
      .map(_.getOrElse(PreviousDocumentsData(Seq.empty)))

    cachedData.flatMap { cache =>
      actionTypeOpt match {
        case Some(Add) =>
          add(boundForm, cache.documents, PreviousDocumentsData.maxAmountOfItems).fold(
            formWithErrors => Future.successful(BadRequest(previous_documents(formWithErrors, cache.documents))),
            updatedCache =>
              customsCacheService
                .cache[PreviousDocumentsData](cacheId, formId, PreviousDocumentsData(updatedCache))
                .map(_ => Redirect(controllers.declaration.routes.PreviousDocumentsController.displayForm()))
          )

        case Some(Remove(ids)) => {
          val updatedCache = remove(ids.headOption, cache.documents)

          customsCacheService
            .cache[PreviousDocumentsData](cacheId, formId, PreviousDocumentsData(updatedCache))
            .map(_ => Redirect(controllers.declaration.routes.PreviousDocumentsController.displayForm()))
        }

        case Some(SaveAndContinue) => {
          saveAndContinue(boundForm, cache.documents, isScreenMandatory, maxAmountOfItems).fold(
            formWithErrors => Future.successful(BadRequest(previous_documents(formWithErrors, cache.documents))),
            updatedCache =>
              if (updatedCache != cache.documents)
                customsCacheService
                  .cache[PreviousDocumentsData](cacheId, formId, PreviousDocumentsData(updatedCache))
                  .map(_ => Redirect(controllers.declaration.routes.ItemsSummaryController.displayForm()))
              else
                Future.successful(Redirect(controllers.declaration.routes.ItemsSummaryController.displayForm()))
          )
        }

        case _ => errorHandler.displayErrorPage()
      }
    }
  }
}
