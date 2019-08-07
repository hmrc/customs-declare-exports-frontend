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
import forms.declaration.NatureOfTransaction
import forms.declaration.NatureOfTransaction._
import javax.inject.Inject
import models.ExportsCacheModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.nature_of_transaction

import scala.concurrent.{ExecutionContext, Future}

class NatureOfTransactionController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  mcc: MessagesControllerComponents,
  natureOfTransactionPage: nature_of_transaction,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    exportsCacheService.get(journeySessionId).map(_.flatMap(_.natureOfTransaction)).map {
      case Some(data) => Ok(natureOfTransactionPage(form.fill(data)))
      case _          => Ok(natureOfTransactionPage(form))
    }
  }

  def saveTransactionType(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form.bindFromRequest
      .fold(
        (formWithErrors: Form[NatureOfTransaction]) =>
          Future.successful(BadRequest(natureOfTransactionPage(adjustErrors(formWithErrors)))),
        form =>
          updateCache(journeySessionId, form).map(
            _ => Redirect(controllers.declaration.routes.PreviousDocumentsController.displayForm())
        )
      )
  }

  private def updateCache(sessionId: String, formData: NatureOfTransaction): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => exportsCacheService.update(sessionId, model.copy(natureOfTransaction = Some(formData)))
    )
}
