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
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.{DeclarationType, Mode}
import models.requests.JourneyRequest
import models.DeclarationType.STANDARD
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalActors.additional_actors_summary

import javax.inject.Inject

class AdditionalActorsSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalActorsPage: additional_actors_summary
) extends FrontendController(mcc) with AdditionalActorsController with I18nSupport with ModelCacheable with SubmissionErrors {

  val validTypes =
    Seq(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL)

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    request.cacheModel.parties.declarationAdditionalActorsData match {
      case Some(data) if data.actors.nonEmpty =>
        Ok(additionalActorsPage(mode, anotherYesNoForm.withSubmissionErrors(), data.actors))
      case _ => navigator.continueTo(mode, routes.AdditionalActorsAddController.displayPage)
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val actors = request.cacheModel.parties.declarationAdditionalActorsData.map(_.actors).getOrElse(Seq.empty)
    anotherYesNoForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) => BadRequest(additionalActorsPage(mode, formWithErrors, actors)),
        validYesNo =>
          validYesNo.answer match {
            case YesNoAnswers.yes => navigator.continueTo(mode, controllers.declaration.routes.AdditionalActorsAddController.displayPage)
            case YesNoAnswers.no  => navigator.continueTo(mode, nextPage)
        }
      )
  }

  private def anotherYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalActors.add.another.empty")
}
