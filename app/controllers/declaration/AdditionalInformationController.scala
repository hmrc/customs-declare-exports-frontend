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

import controllers.actions.{AuthAction, JourneyAction, VerifiedEmailAction}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers

import javax.inject.Inject
import models.Mode
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalInformation.additional_information

class AdditionalInformationController @Inject()(
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalInformationPage: additional_information
) extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType) { implicit request =>
    val frm = anotherYesNoForm.withSubmissionErrors()
    cachedAdditionalInformationData(itemId) match {
      case Some(data) if data.items.nonEmpty =>
        Ok(additionalInformationPage(mode, itemId, frm, data.items))
      case Some(_) =>
        navigator.continueTo(mode, controllers.declaration.routes.AdditionalInformationAddController.displayPage(_, itemId))
      case _ =>
        navigator.continueTo(mode, controllers.declaration.routes.AdditionalInformationRequiredController.displayPage(_, itemId))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType) { implicit request =>
    anotherYesNoForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) =>
          BadRequest(
            additionalInformationPage(mode, itemId, formWithErrors, cachedAdditionalInformationData(itemId).map(_.items).getOrElse(Seq.empty))
        ),
        validYesNo =>
          validYesNo.answer match {
            case YesNoAnswers.yes =>
              navigator.continueTo(mode, controllers.declaration.routes.AdditionalInformationAddController.displayPage(_, itemId))
            case YesNoAnswers.no => navigator.continueTo(mode, routes.DocumentsProducedController.displayPage(_, itemId))
        }
      )
  }

  private def anotherYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalInformation.add.another.empty")

  private def cachedAdditionalInformationData(itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation)
}
