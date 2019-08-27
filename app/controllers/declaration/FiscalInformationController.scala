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
import controllers.navigation.Navigator
import forms.declaration.FiscalInformation
import forms.declaration.FiscalInformation.form
import javax.inject.Inject
import models.Mode
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.{ExportItem, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.fiscal_information

import scala.concurrent.{ExecutionContext, Future}

class FiscalInformationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  fiscalInformationPage: fiscal_information
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId) match {
      case Some(ExportItem(_, _, _, Some(fiscalInformation), _, _, _, _, _, _)) =>
        Ok(fiscalInformationPage(mode, itemId, form().fill(fiscalInformation)))
      case response => Ok(fiscalInformationPage(mode, itemId, form()))
    }
  }

  def saveFiscalInformation(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[FiscalInformation]) =>
            Future.successful(BadRequest(fiscalInformationPage(mode, itemId, formWithErrors))),
          formData =>
            formData.onwardSupplyRelief match {
              case FiscalInformation.AllowedFiscalInformationAnswers.yes =>
                updateCacheForYes(itemId, formData) map { _ =>
                  navigator.continueTo(routes.AdditionalFiscalReferencesController.displayPage(mode, itemId))
                }
              case FiscalInformation.AllowedFiscalInformationAnswers.no =>
                updateCacheForNo(itemId, formData) map { _ =>
                  navigator.continueTo(routes.ItemTypeController.displayPage(mode, itemId))
                }
          }
        )
  }

  //TODO Use one method instead of updateCacheForYes and updateCacheForNo
  private def updateCacheForYes(itemId: String, updatedFiscalInformation: FiscalInformation)(
    implicit req: JourneyRequest[_]
  ): Future[Unit] =
    updateExportsDeclarationSyncDirect(model => {
      val itemList = model.items
        .find(item => item.id.equals(itemId))
        .map(_.copy(fiscalInformation = Some(updatedFiscalInformation)))
        .fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
      model.copy(items = itemList)
    }).map(_ => ())

  private def updateCacheForNo(itemId: String, updatedFiscalInformation: FiscalInformation)(
    implicit req: JourneyRequest[_]
  ): Future[Unit] =
    updateExportsDeclarationSyncDirect(model => {
      val itemList = model.items
        .find(item => item.id.equals(itemId))
        .map(_.copy(fiscalInformation = Some(updatedFiscalInformation), additionalFiscalReferencesData = None))
        .fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)

      model.copy(items = itemList)
    }).map(_ => ())

}
