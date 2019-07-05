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
  customsCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  fiscalInformationPage: fiscal_information
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[FiscalInformation](goodsItemCacheId, formId).map {
      case Some(data) => Ok(fiscalInformationPage(itemId, form.fill(data)))
      case _          => Ok(fiscalInformationPage(itemId, form))
    }
  }

  def saveFiscalInformation(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[FiscalInformation]) =>
            Future.successful(BadRequest(fiscalInformationPage(itemId, formWithErrors))),
          validFiscalInformation => updateCacheModelsAndRedirect(itemId, validFiscalInformation)
        )
  }

  private def updateCacheModelsAndRedirect(itemId: String, validFiscalInformation: FiscalInformation)(
    implicit journeyRequest: JourneyRequest[_]
  ) =
    for {
      _ <- customsCacheService.cache[FiscalInformation](goodsItemCacheId, formId, validFiscalInformation)
      _ <- updateExportsCache(itemId, journeySessionId, validFiscalInformation)
    } yield specifyNextPage(itemId, validFiscalInformation)

  private def specifyNextPage(itemId: String, answer: FiscalInformation): Result =
    if (answer.onwardSupplyRelief == FiscalInformation.AllowedFiscalInformationAnswers.yes)
      Redirect(routes.AdditionalFiscalReferencesController.displayPage(itemId))
    else Redirect(routes.ItemTypePageController.displayPage(itemId))

  private def updateExportsCache(
    itemId: String,
    sessionId: String,
    updatedFiscalInformation: FiscalInformation
  ): Future[Either[String, ExportsCacheModel]] =
    getAndUpdateExportCacheModel(sessionId, model => {
      val item: Option[ExportItem] =
        model.items.find(item => item.id.equals(itemId)).map(_.copy(fiscalInformation = Some(updatedFiscalInformation)))
      val itemList = item.fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
      exportsCacheService.update(sessionId, model.copy(items = itemList))
    })

}
