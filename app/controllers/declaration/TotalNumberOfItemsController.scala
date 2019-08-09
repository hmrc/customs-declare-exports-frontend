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
import forms.declaration.TotalNumberOfItems
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.total_number_of_items

import scala.concurrent.{ExecutionContext, Future}

class TotalNumberOfItemsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  mcc: MessagesControllerComponents,
  totalNumberOfItemsPage: total_number_of_items,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {
  import forms.declaration.TotalNumberOfItems._

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.totalNumberOfItems match {
      case Some(data) => Ok(totalNumberOfItemsPage(form.fill(data)))
      case _          => Ok(totalNumberOfItemsPage(form))
    }
  }

  def saveNoOfItems(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[TotalNumberOfItems]) =>
          Future.successful(BadRequest(totalNumberOfItemsPage(formWithErrors))),
        formData =>
          updateCache(journeySessionId, formData).map { _ =>
            Redirect(routes.NatureOfTransactionController.displayForm())
        }
      )
  }

  private def updateCache(sessionId: String, formData: TotalNumberOfItems)(implicit req: JourneyRequest[_]) =
    getAndUpdateExportsDeclaration(
      sessionId,
      model =>
        exportsCacheService
          .update(sessionId, model.copy(totalNumberOfItems = Some(formData)))
    )
}
