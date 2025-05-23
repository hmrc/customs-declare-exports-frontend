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

package controllers.section2

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section2.routes.AdditionalActorsSummaryController
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section2.AdditionalActor
import models.ExportsDeclaration
import models.declaration.AdditionalActors
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ListItem
import views.html.section2.additionalActors.additional_actors_remove

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalActorsRemoveController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  removePage: additional_actors_remove
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(id: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    findActor(id) match {
      case Some(actor) => Ok(removePage(id, actor, removeYesNoForm.withSubmissionErrors))
      case _           => navigator.continueTo(AdditionalActorsSummaryController.displayPage)
    }
  }

  def submitForm(id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    findActor(id) match {
      case Some(actor) =>
        removeYesNoForm
          .bindFromRequest()
          .fold(
            (formWithErrors: Form[YesNoAnswer]) => Future.successful(BadRequest(removePage(id, actor, formWithErrors))),
            formData =>
              formData.answer match {
                case YesNoAnswers.yes =>
                  updateExportsCache(actor)
                    .map(_ => navigator.continueTo(AdditionalActorsSummaryController.displayPage))
                case YesNoAnswers.no =>
                  Future.successful(navigator.continueTo(AdditionalActorsSummaryController.displayPage))
              }
          )
      case _ => Future.successful(navigator.continueTo(AdditionalActorsSummaryController.displayPage))
    }
  }

  private def removeYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalActors.remove.empty")

  private def findActor(id: String)(implicit request: JourneyRequest[AnyContent]): Option[AdditionalActor] =
    ListItem.findById(id, request.cacheModel.parties.declarationAdditionalActorsData.map(_.actors).getOrElse(Seq.empty))

  private def updateExportsCache(itemToRemove: AdditionalActor)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] = {
    val updatedActors = request.cacheModel.parties.declarationAdditionalActorsData.map(_.actors).getOrElse(Seq.empty).filterNot(_ == itemToRemove)
    val updatedParties = request.cacheModel.parties.copy(declarationAdditionalActorsData = Some(AdditionalActors(updatedActors)))
    updateDeclarationFromRequest(model => model.copy(parties = updatedParties))
  }
}
