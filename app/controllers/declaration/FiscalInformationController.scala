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
import forms.declaration.FiscalInformation
import forms.declaration.FiscalInformation.{form, formId}
import javax.inject.Inject
import models.declaration.ProcedureCodesData
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.CustomsCacheService
import services.cache.{ExportItem, ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.fiscal_information

import scala.concurrent.{ExecutionContext, Future}

class FiscalInformationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  legacyCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  fiscalInformationPage: fiscal_information
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    exportsCacheService.getItemByIdAndSession(itemId, journeySessionId).map {
      case Some(data) =>
        data.fiscalInformation.fold(Ok(fiscalInformationPage(itemId, form()))) { fiscalInformation =>
          Ok(fiscalInformationPage(itemId, form().fill(fiscalInformation)))
        }
      case _ => Ok(fiscalInformationPage(itemId, form()))
    }
  }

  def saveFiscalInformation(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[FiscalInformation]) =>
            Future.successful(BadRequest(fiscalInformationPage(itemId, formWithErrors))),
          formData =>
            formData.onwardSupplyRelief match {
              case FiscalInformation.AllowedFiscalInformationAnswers.yes =>
                updateCacheForYes(itemId, journeySessionId, formData) map { _ =>
                  Redirect(routes.AdditionalFiscalReferencesController.displayPage(itemId))
                }
              case FiscalInformation.AllowedFiscalInformationAnswers.no =>
                updateCacheForNo(itemId, journeySessionId, formData) map { _ =>
                  Redirect(routes.ItemTypeController.displayPage(itemId))
                }
          }
        )
  }

  private def updateCacheForYes(itemId: String, sessionId: String, updatedFiscalInformation: FiscalInformation)(
    implicit req: JourneyRequest[_]
  ): Future[Unit] =
    for {
      _ <- legacyCacheService.cache[FiscalInformation](goodsItemCacheId, formId, updatedFiscalInformation)
      _ <- getAndUpdateExportCacheModel(
        sessionId,
        model => {
          val item: Option[ExportItem] =
            model.items
              .find(item => item.id.equals(itemId))
              .map(_.copy(fiscalInformation = Some(updatedFiscalInformation)))
          val itemList = item.fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
          exportsCacheService.update(sessionId, model.copy(items = itemList))
        }
      )
    } yield Unit

  private def updateCacheForNo(itemId: String, sessionId: String, updatedFiscalInformation: FiscalInformation)(
    implicit req: JourneyRequest[_]
  ): Future[Unit] =
    for {
      _ <- legacyCacheService.cache[FiscalInformation](goodsItemCacheId, formId, updatedFiscalInformation)
      _ <- getAndUpdateExportCacheModel(
        sessionId,
        model => {
          val item: Option[ExportItem] =
            model.items
              .find(item => item.id.equals(itemId))
              .map(_.copy(fiscalInformation = Some(updatedFiscalInformation), additionalFiscalReferencesData = None))
          val itemList = item.fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
          exportsCacheService.update(sessionId, model.copy(items = itemList))
        }
      )
    } yield Unit

}
