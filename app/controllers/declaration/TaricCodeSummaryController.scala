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

import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.Mode
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.taric_codes

import javax.inject.Inject

class TaricCodeSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  taricCodesPage: taric_codes
) extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.taricCodes) match {
      case Some(taricCodes) if taricCodes.nonEmpty => Ok(taricCodesPage(mode, itemId, addYesNoForm.withSubmissionErrors(), taricCodes))
      case _                                       => navigator.continueTo(mode, routes.TaricCodeAddController.displayPage(_, itemId))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val taricCodes = request.cacheModel.itemBy(itemId).flatMap(_.taricCodes).getOrElse(List.empty)
    addYesNoForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) => BadRequest(taricCodesPage(mode, itemId, formWithErrors, taricCodes)),
        validYesNo =>
          validYesNo.answer match {
            case YesNoAnswers.yes => navigator.continueTo(mode, controllers.declaration.routes.TaricCodeAddController.displayPage(_, itemId))
            case YesNoAnswers.no  => navigator.continueTo(mode, controllers.declaration.routes.NactCodeSummaryController.displayPage(_, itemId))
        }
      )
  }

  private def addYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.taricAdditionalCodes.add.answer.empty")
}
