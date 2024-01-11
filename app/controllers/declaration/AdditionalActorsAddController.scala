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

package controllers.declaration

import controllers.actions.{AuthAction, JourneyAction}
import controllers.helpers.MultipleItemsHelper
import controllers.navigation.Navigator
import forms.NoneOfTheAbove
import forms.declaration.AdditionalActor
import forms.declaration.AdditionalActor.{additionalActorsFormGroupId, form}
import models.DeclarationType._
import models.declaration.AdditionalActors
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalActors.additional_actors_add

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalActorsAddController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarationAdditionalActorsPage: additional_actors_add
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType(nonClearanceJourneys)) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.parties.declarationAdditionalActorsData match {
      case Some(_) => Ok(declarationAdditionalActorsPage(frm.fill(AdditionalActor(None, Some(NoneOfTheAbove.value)))))
      case _       => Ok(declarationAdditionalActorsPage(frm))
    }
  }

  def saveForm: Action[AnyContent] = (authenticate andThen journeyType(nonClearanceJourneys)).async { implicit request =>
    val boundForm = form.bindFromRequest()
    boundForm.fold(
      formWithErrors => Future.successful(BadRequest(declarationAdditionalActorsPage(formWithErrors))),
      actor => {
        val cachedActors = request.cacheModel.parties.declarationAdditionalActorsData.map(_.actors).getOrElse(Seq.empty)
        if (actor.isDefined) addAdditionalActor(boundForm, cachedActors)
        else if (cachedActors.nonEmpty)
          updateCache(AdditionalActors(cachedActors))
            .map(_ => navigator.continueTo(routes.AdditionalActorsSummaryController.displayPage))
        else
          updateCache(AdditionalActors(cachedActors))
            .map(_ => navigator.continueTo(routes.AuthorisationProcedureCodeChoiceController.displayPage))
      }
    )
  }

  private def addAdditionalActor(boundForm: Form[AdditionalActor], cachedActors: Seq[AdditionalActor])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedActors, AdditionalActors.maxNumberOfActors, additionalActorsFormGroupId, "declaration.additionalActors")
      .fold(
        formWithErrors => Future.successful(BadRequest(declarationAdditionalActorsPage(formWithErrors))),
        updatedActors =>
          updateCache(AdditionalActors(updatedActors))
            .map(_ => navigator.continueTo(routes.AdditionalActorsSummaryController.displayPage))
      )

  private def updateCache(formData: AdditionalActors)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      val updatedParties = model.parties.copy(declarationAdditionalActorsData = Some(formData))
      model.copy(parties = updatedParties)
    }
}
