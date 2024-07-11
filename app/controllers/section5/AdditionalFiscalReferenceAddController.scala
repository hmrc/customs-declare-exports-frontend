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

package controllers.section5

import connectors.CodeListConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.helpers.MultipleItemsHelper
import controllers.navigation.Navigator
import controllers.section5.AdditionalFiscalReferenceAddController.AdditionalFiscalReferencesFormGroupId
import controllers.section5.routes.{AdditionalFiscalReferencesController, CommodityDetailsController}
import forms.section5.AdditionalFiscalReference.{countryId, form}
import forms.section5.AdditionalFiscalReferencesData._
import forms.section5.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.validators.forms.AutoCompleteFieldBinding
import views.html.section5.fiscalInformation.additional_fiscal_reference_add

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalFiscalReferenceAddController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalFiscalReferencePage: additional_fiscal_reference_add
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector, auditService: AuditService)
    extends FrontendController(mcc) with AutoCompleteFieldBinding with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (request.cacheModel.hasFiscalInformation(itemId)) {
      val frm = form.withSubmissionErrors
      Ok(additionalFiscalReferencePage(itemId, frm))
    } else navigator.continueTo(CommodityDetailsController.displayPage(itemId))
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    if (request.cacheModel.hasFiscalInformation(itemId)) {
      val boundForm = form.bindFromRequest(formValuesFromRequest(countryId))
      boundForm.fold(
        formWithErrors => Future.successful(BadRequest(additionalFiscalReferencePage(itemId, formWithErrors))),
        _ => saveAndContinue(itemId, boundForm, cachedData(itemId))
      )
    } else Future.successful(navigator.continueTo(CommodityDetailsController.displayPage(itemId)))
  }

  private def cachedData(itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalFiscalReferencesData).getOrElse(AdditionalFiscalReferencesData.default)

  private def saveAndContinue(itemId: String, form: Form[AdditionalFiscalReference], cachedData: AdditionalFiscalReferencesData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(form, cachedData.references, limit, AdditionalFiscalReferencesFormGroupId, "declaration.additionalFiscalReferences")
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalFiscalReferencePage(itemId, formWithErrors))),
        updatedCache =>
          updateExportsCache(itemId, cachedData.copy(references = updatedCache))
            .map(_ => navigator.continueTo(AdditionalFiscalReferencesController.displayPage(itemId)))
      )

  private def updateExportsCache(itemId: String, updatedAdditionalFiscalReferencesData: AdditionalFiscalReferencesData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      model.updatedItem(itemId, item => item.copy(additionalFiscalReferencesData = Some(updatedAdditionalFiscalReferencesData)))
    }
}

object AdditionalFiscalReferenceAddController {

  val AdditionalFiscalReferencesFormGroupId: String = "additionalFiscalReferences"
}
