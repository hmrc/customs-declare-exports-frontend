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
import forms.declaration.FiscalInformation
import forms.declaration.FiscalInformation.formId
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.governmentagencygoodsitem.Formats._
import models.declaration.governmentagencygoodsitem.GovernmentAgencyGoodsItem
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import services.ExportsItemsCacheIds.itemsId
import uk.gov.hmrc.http.HeaderCarrier
import services.cache.{ExportItem, ExportsCacheService, ExportsCacheModel}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.items_summary

import scala.concurrent.{ExecutionContext, Future}

class ItemsSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  cacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    cacheService
      .fetchAndGetEntry[Seq[GovernmentAgencyGoodsItem]](cacheId, itemsId)
      .zip(hasFiscalInformation())
      .map {
        case (items, hasFiscalInformation) =>
          Ok(items_summary(items.getOrElse(Seq.empty), hasFiscalInformation))
      }
  }

  def hasFiscalInformation()(implicit journeyRequest: JourneyRequest[_], hc: HeaderCarrier): Future[Boolean] =
    cacheService.fetchAndGetEntry[FiscalInformation](cacheId, formId).map {
      case Some(data) => data.onwardSupplyRelief == FiscalInformation.AllowedFiscalInformationAnswers.yes
      case None       => false
    }

  def addItem(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    sessionId.map(updateCache(_, ExportItem()))
    Redirect(controllers.declaration.routes.ProcedureCodesPageController.displayPage())
  }

  private def updateCache(sessionId: String, exportItem: ExportItem): Future[Either[String, ExportsCacheModel]] =
    exportsCacheService.get(sessionId).flatMap {
      case Right(model) => exportsCacheService.update(sessionId, model.copy(items = List(exportItem)))
    }

  private def sessionId(implicit request: JourneyRequest[AnyContent]) =
    request.authenticatedRequest.session.data.get("sessionId")
}
