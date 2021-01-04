/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.declaration.FiscalInformation._
import javax.inject.Inject
import models.declaration.ProcedureCodesData
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.fiscalInformation.fiscal_information

import scala.concurrent.{ExecutionContext, Future}

class FiscalInformationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  fiscalInformationPage: fiscal_information
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String, fastForward: Boolean): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    def cacheContainsFiscalReferenceData = request.cacheModel.itemBy(itemId).exists(_.additionalFiscalReferencesData.exists(_.references.nonEmpty))
    def cacheItemIneligibleForOSR =
      request.cacheModel
        .itemBy(itemId)
        .flatMap(_.procedureCodes)
        .flatMap(_.procedureCode)
        .exists(code => !ProcedureCodesData.osrProcedureCodes.contains(code))

    def displayFiscalInformationPage() = {
      val frm = form().withSubmissionErrors()
      request.cacheModel.itemBy(itemId).flatMap(_.fiscalInformation) match {
        case Some(fiscalInformation) => Ok(fiscalInformationPage(mode, itemId, frm.fill(fiscalInformation)))
        case _                       => Ok(fiscalInformationPage(mode, itemId, frm))
      }
    }

    if (mode == Mode.Change) {
      displayFiscalInformationPage()
    } else if (fastForward && cacheContainsFiscalReferenceData) {
      navigator.continueTo(mode, routes.AdditionalFiscalReferencesController.displayPage(_, itemId))
    } else if (fastForward && cacheItemIneligibleForOSR) {
      navigator.continueTo(mode, routes.ProcedureCodesController.displayPage(_, itemId))
    } else {
      if (cacheContainsFiscalReferenceData) {
        navigator.continueTo(mode, controllers.declaration.routes.AdditionalFiscalReferencesController.displayPage(_, itemId))
      } else {
        displayFiscalInformationPage()
      }
    }
  }

  def saveFiscalInformation(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[FiscalInformation]) => Future.successful(BadRequest(fiscalInformationPage(mode, itemId, formWithErrors))),
        formData => updateCache(itemId, formData).map(_ => redirectToNextPage(mode, itemId, formData))
      )
  }

  private def updateCache(itemId: String, updatedFiscalInformation: FiscalInformation)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] = {
    def updatedModel(model: ExportsDeclaration): ExportsDeclaration =
      updatedFiscalInformation.onwardSupplyRelief match {
        case AllowedFiscalInformationAnswers.yes =>
          model.updatedItem(itemId, item => item.copy(fiscalInformation = Some(updatedFiscalInformation)))
        case AllowedFiscalInformationAnswers.no =>
          model.updatedItem(itemId, item => item.copy(fiscalInformation = Some(updatedFiscalInformation), additionalFiscalReferencesData = None))
      }

    updateExportsDeclarationSyncDirect(updatedModel(_))
  }

  private def redirectToNextPage(mode: Mode, itemId: String, fiscalInformation: FiscalInformation)(
    implicit request: JourneyRequest[AnyContent]
  ): Result =
    fiscalInformation.onwardSupplyRelief match {
      case FiscalInformation.AllowedFiscalInformationAnswers.yes =>
        navigator.continueTo(mode, routes.AdditionalFiscalReferencesAddController.displayPage(_, itemId))
      case FiscalInformation.AllowedFiscalInformationAnswers.no =>
        navigator.continueTo(mode, routes.CommodityDetailsController.displayPage(_, itemId))
    }
}
