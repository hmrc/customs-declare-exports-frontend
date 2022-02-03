/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.actions.ItemActionBuilder
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import javax.inject.Inject
import models.requests.JourneyRequest
import models.Mode
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.fiscalInformation.additional_fiscal_references

class AdditionalFiscalReferencesController @Inject()(
  itemAction: ItemActionBuilder,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalFiscalReferencesPage: additional_fiscal_references
) extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = itemAction(itemId) { implicit request =>
    val frm = addAnotherYesNoForm.withSubmissionErrors()
    cachedAdditionalReferencesData(itemId) match {
      case Some(data) if data.references.nonEmpty =>
        Ok(additionalFiscalReferencesPage(mode, itemId, frm, data.references))
      case Some(_) =>
        navigator.continueTo(mode, controllers.declaration.routes.AdditionalFiscalReferencesAddController.displayPage(_, itemId))
      case _ =>
        navigator.continueTo(mode, controllers.declaration.routes.FiscalInformationController.displayPage(_, itemId))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = itemAction(itemId) { implicit request =>
    addAnotherYesNoForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) =>
          BadRequest(
            additionalFiscalReferencesPage(
              mode,
              itemId,
              formWithErrors,
              cachedAdditionalReferencesData(itemId).map(_.references).getOrElse(Seq.empty)
            )
        ),
        validYesNo =>
          validYesNo.answer match {
            case YesNoAnswers.yes =>
              navigator
                .continueTo(mode, controllers.declaration.routes.AdditionalFiscalReferencesAddController.displayPage(_, itemId), mode.isErrorFix)
            case YesNoAnswers.no => navigator.continueTo(mode, routes.CommodityDetailsController.displayPage(_, itemId))
        }
      )
  }

  private def addAnotherYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalFiscalReferences.add.another.empty")

  private def cachedAdditionalReferencesData(itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalFiscalReferencesData)
}
