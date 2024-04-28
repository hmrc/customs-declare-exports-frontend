/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.declaration.routes.{AdditionalFiscalReferenceAddController, AdditionalFiscalReferencesController, CommodityDetailsController}
import controllers.navigation.Navigator
import forms.declaration.FiscalInformation
import forms.declaration.FiscalInformation.{form, AllowedFiscalInformationAnswers}
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.fiscalInformation.fiscal_information

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FiscalInformationController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  fiscalInformationPage: fiscal_information
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (request.cacheModel.hasOsrProcedureCode(itemId)) {
      val frm = form.withSubmissionErrors
      request.cacheModel.itemBy(itemId).flatMap(_.fiscalInformation) match {
        case Some(fiscalInformation) => Ok(fiscalInformationPage(itemId, frm.fill(fiscalInformation)))
        case _                       => Ok(fiscalInformationPage(itemId, frm))
      }
    } else navigator.continueTo(CommodityDetailsController.displayPage(itemId))
  }

  def saveFiscalInformation(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    if (request.cacheModel.hasOsrProcedureCode(itemId))
      form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[FiscalInformation]) => Future.successful(BadRequest(fiscalInformationPage(itemId, formWithErrors))),
          formData => updateCache(itemId, formData).map(_ => redirectToNextPage(itemId, formData))
        )
    else Future.successful(navigator.continueTo(CommodityDetailsController.displayPage(itemId)))
  }

  private def updateCache(itemId: String, updatedFiscalInformation: FiscalInformation)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] = {

    val fiscalInformation = Some(updatedFiscalInformation)

    def updatedModel(model: ExportsDeclaration): ExportsDeclaration =
      updatedFiscalInformation.onwardSupplyRelief match {
        case AllowedFiscalInformationAnswers.yes =>
          model.updatedItem(itemId, item => item.copy(fiscalInformation = fiscalInformation))

        case AllowedFiscalInformationAnswers.no =>
          model.updatedItem(itemId, item => item.copy(fiscalInformation = fiscalInformation, additionalFiscalReferencesData = None))
      }

    updateDeclarationFromRequest(updatedModel)
  }

  private def redirectToNextPage(itemId: String, fiscalInformation: FiscalInformation)(implicit request: JourneyRequest[AnyContent]): Result =
    fiscalInformation.onwardSupplyRelief match {
      case AllowedFiscalInformationAnswers.yes =>
        val nextPage =
          if (request.cacheModel.hasFiscalReferences(itemId)) AdditionalFiscalReferencesController.displayPage(itemId)
          else AdditionalFiscalReferenceAddController.displayPage(itemId)

        navigator.continueTo(nextPage)

      case AllowedFiscalInformationAnswers.no =>
        navigator.continueTo(CommodityDetailsController.displayPage(itemId))
    }
}
