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

package controllers.section2

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section2.routes.{AdditionalActorsAddController, AuthorisationProcedureCodeChoiceController}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section2.additionalActors.additional_actors_summary

import javax.inject.Inject

class AdditionalActorsSummaryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalActorsPage: additional_actors_summary
) extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType(nonClearanceJourneys)) { implicit request =>
    request.cacheModel.parties.declarationAdditionalActorsData match {
      case Some(data) if data.actors.nonEmpty => Ok(additionalActorsPage(form.withSubmissionErrors, data.actors))

      case _ => navigator.continueTo(AdditionalActorsAddController.displayPage)
    }
  }

  def submitForm: Action[AnyContent] = (authenticate andThen journeyType(nonClearanceJourneys)) { implicit request =>
    val actors = request.cacheModel.parties.declarationAdditionalActorsData.map(_.actors).getOrElse(Seq.empty)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(additionalActorsPage(formWithErrors, actors)),
        _.answer match {
          case YesNoAnswers.yes => navigator.continueTo(AdditionalActorsAddController.displayPage)
          case YesNoAnswers.no  => navigator.continueTo(AuthorisationProcedureCodeChoiceController.displayPage)
        }
      )
  }

  private def form: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalActors.add.another.empty")
}
