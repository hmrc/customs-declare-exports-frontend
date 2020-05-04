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
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import javax.inject.Inject
import models.{Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.additional_information_required
import scala.concurrent.{ExecutionContext}

class AdditionalInformationRequiredController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalInfoReq: additional_information_required
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(additionalInfoReq(mode, itemId, YesNoAnswer.form()))
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    YesNoAnswer
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) => BadRequest(additionalInfoReq(mode, itemId, formWithErrors)),
        validYesNo => navigator.continueTo(mode, nextPage(yesNoAnswer = validYesNo, itemId))
      )

  }

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String): Mode => Call =
    mode =>
      yesNoAnswer.answer match {
        case YesNoAnswers.yes =>
          controllers.declaration.routes.AdditionalInformationController.displayPage(mode, itemId)
        case YesNoAnswers.no =>
          controllers.declaration.routes.DocumentsProducedController.displayPage(mode, itemId)
    }

}
