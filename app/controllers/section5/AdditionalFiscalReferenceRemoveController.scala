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

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.{ModelCacheable, SubmissionErrors}
import controllers.helpers.MultipleItemsHelper.remove
import controllers.navigation.Navigator
import controllers.section5.routes.{AdditionalFiscalReferencesController, FiscalInformationController}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
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
import utils.ListItem
import views.html.section5.fiscalInformation.additional_fiscal_reference_remove

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalFiscalReferenceRemoveController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  removePage: additional_fiscal_reference_remove
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    findAdditionalFiscalReference(itemId, id) match {
      case Some(reference) => Ok(removePage(itemId, id, reference, removalYesNoForm.withSubmissionErrors))
      case _               => returnToSummary(itemId)
    }
  }

  def submitForm(itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    findAdditionalFiscalReference(itemId, id) match {
      case Some(reference) =>
        removalYesNoForm
          .bindFromRequest()
          .fold(
            (formWithErrors: Form[YesNoAnswer]) => Future.successful(BadRequest(removePage(itemId, id, reference, formWithErrors))),
            formData =>
              formData.answer match {
                case YesNoAnswers.yes =>
                  removeAdditionalFiscalReference(itemId, reference).map(declaration => redirectAfterRemove(itemId, declaration))

                case YesNoAnswers.no => Future.successful(returnToSummary(itemId))
              }
          )
      case _ => Future.successful(returnToSummary(itemId))
    }
  }

  private def removalYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalFiscalReferences.remove.empty")

  private def redirectAfterRemove(itemId: String, declaration: ExportsDeclaration)(implicit request: JourneyRequest[AnyContent]): Result =
    declaration.itemBy(itemId).flatMap(_.additionalFiscalReferencesData).map(_.references) match {
      case Some(references) if references.nonEmpty => returnToSummary(itemId)
      case _                                       => navigator.continueTo(FiscalInformationController.displayPage(itemId))
    }

  private def returnToSummary(itemId: String)(implicit request: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(AdditionalFiscalReferencesController.displayPage(itemId))

  private def findAdditionalFiscalReference(itemId: String, id: String)(
    implicit request: JourneyRequest[AnyContent]
  ): Option[AdditionalFiscalReference] =
    ListItem.findById(id, request.cacheModel.itemBy(itemId).flatMap(_.additionalFiscalReferencesData).map(_.references).getOrElse(Seq.empty))

  private def removeAdditionalFiscalReference(itemId: String, itemToRemove: AdditionalFiscalReference)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] = {
    val cachedInformation =
      request.cacheModel.itemBy(itemId).flatMap(_.additionalFiscalReferencesData).getOrElse(AdditionalFiscalReferencesData(Seq.empty))
    val updatedInformation =
      cachedInformation.copy(references = remove(cachedInformation.references, itemToRemove.equals(_: AdditionalFiscalReference)))
    updateDeclarationFromRequest(model => model.updatedItem(itemId, item => item.copy(additionalFiscalReferencesData = Some(updatedInformation))))
  }
}
