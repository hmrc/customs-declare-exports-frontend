/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section5.routes._
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section5.AdditionalFiscalReferencesData
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section5.fiscalInformation.additional_fiscal_references

import javax.inject.Inject

class AdditionalFiscalReferencesController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalFiscalReferencesPage: additional_fiscal_references
) extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (request.cacheModel.hasFiscalInformation(itemId)) {
      cachedAdditionalReferencesData(itemId, request) match {
        case Some(data) if data.references.nonEmpty =>
          val form = yesNoForm.withSubmissionErrors
          Ok(additionalFiscalReferencesPage(itemId, form, data.references))

        case Some(_) => navigator.continueTo(AdditionalFiscalReferenceAddController.displayPage(itemId))
        case _       => navigator.continueTo(FiscalInformationController.displayPage(itemId))
      }
    } else navigator.continueTo(CommodityDetailsController.displayPage(itemId))
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    yesNoForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          val data = cachedAdditionalReferencesData(itemId, request).map(_.references).getOrElse(Seq.empty)
          BadRequest(additionalFiscalReferencesPage(itemId, formWithErrors, data))
        },
        _.answer match {
          case YesNoAnswers.yes => navigator.continueTo(AdditionalFiscalReferenceAddController.displayPage(itemId))
          case YesNoAnswers.no  => navigator.continueTo(CommodityDetailsController.displayPage(itemId))
        }
      )
  }

  private def yesNoForm: Form[YesNoAnswer] =
    YesNoAnswer.form(errorKey = "declaration.additionalFiscalReferences.add.another.empty")

  private def cachedAdditionalReferencesData(itemId: String, request: JourneyRequest[_]): Option[AdditionalFiscalReferencesData] =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalFiscalReferencesData)
}
