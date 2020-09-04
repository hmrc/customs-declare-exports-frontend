/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.util.MultipleItemsHelper.remove
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData, AdditionalInformation}
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ListItem
import views.html.declaration.fiscalInformation.additional_fiscal_references_remove

import scala.concurrent.{ExecutionContext, Future}

class AdditionalFiscalReferencesRemoveController @Inject()(
                                                       authenticate: AuthAction,
                                                       journeyType: JourneyAction,
                                                       override val exportsCacheService: ExportsCacheService,
                                                       navigator: Navigator,
                                                       mcc: MessagesControllerComponents,
                                                       removePage: additional_fiscal_references_remove
                                                     )(implicit ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    findAdditionalFiscalReference(itemId, id) match {
      case Some(reference) => Ok(removePage(mode, itemId, id, reference, removeYesNoForm.withSubmissionErrors()))
      case _                 => returnToSummary(mode, itemId)
    }
  }

  def submitForm(mode: Mode, itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    findAdditionalFiscalReference(itemId, id) match {
      case Some(reference) =>
        removeYesNoForm
          .bindFromRequest()
          .fold(
            (formWithErrors: Form[YesNoAnswer]) => Future.successful(BadRequest(removePage(mode, itemId, id, reference, formWithErrors))),
            formData => {
              formData.answer match {
                case YesNoAnswers.yes =>
                  removeAdditionalFiscalReference(itemId, reference)
                    .map(declaration => afterRemove(mode, itemId, declaration))
                case YesNoAnswers.no =>
                  Future.successful(returnToSummary(mode, itemId))
              }
            }
          )
      case _ => Future.successful(returnToSummary(mode, itemId))
    }

  }

  private def removeYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalFiscalReferences.remove.empty")

  private def afterRemove(mode: Mode, itemId: String, declaration: Option[ExportsDeclaration])(implicit request: JourneyRequest[AnyContent]) =
    declaration.flatMap(_.itemBy(itemId)).flatMap(_.additionalFiscalReferencesData).map(_.references) match {
      case Some(references) if references.nonEmpty => returnToSummary(mode, itemId)
      case _                             => navigator.continueTo(mode, routes.FiscalInformationController.displayPage(_, itemId))
    }

  private def returnToSummary(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    navigator.continueTo(mode, routes.AdditionalFiscalReferencesController.displayPage(_, itemId))

  private def findAdditionalFiscalReference(itemId: String, id: String)(implicit request: JourneyRequest[AnyContent]): Option[AdditionalFiscalReference] =
    ListItem.findById(id, request.cacheModel.itemBy(itemId).flatMap(_.additionalFiscalReferencesData).map(_.references).getOrElse(Seq.empty))

  private def removeAdditionalFiscalReference(itemId: String, itemToRemove: AdditionalFiscalReference)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] = {
    val cachedInformation = request.cacheModel.itemBy(itemId).flatMap(_.additionalFiscalReferencesData).getOrElse(AdditionalFiscalReferencesData(Seq.empty))
    val updatedInformation = cachedInformation.copy(references = remove(cachedInformation.references, itemToRemove.equals(_: AdditionalFiscalReference)))
    updateExportsDeclarationSyncDirect(model => {
      model.updatedItem(itemId, item => item.copy(additionalFiscalReferencesData = Some(updatedInformation)))
    })
  }

}
