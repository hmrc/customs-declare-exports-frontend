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
import forms.NoneOfTheAbove
import forms.declaration.DeclarationAdditionalActors
import forms.declaration.DeclarationAdditionalActors.form
import javax.inject.Inject
import models.declaration.DeclarationAdditionalActorsData
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.additionalActors.additional_actors_add

import scala.concurrent.{ExecutionContext, Future}

class AdditionalActorsAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarationAdditionalActorsPage: additional_actors_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  val validTypes =
    Seq(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL)

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    val frm = form().withSubmissionErrors()
    request.cacheModel.parties.declarationAdditionalActorsData match {
      case Some(_) => Ok(declarationAdditionalActorsPage(mode, frm.fill(DeclarationAdditionalActors(None, Some(NoneOfTheAbove.value)))))
      case _       => Ok(declarationAdditionalActorsPage(mode, frm))
    }
  }

  def saveForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    val cache = request.cacheModel.parties.declarationAdditionalActorsData.getOrElse(DeclarationAdditionalActorsData(Seq()))
    boundForm.fold(
      formWithErrors => {
        Future.successful(BadRequest(declarationAdditionalActorsPage(mode, formWithErrors)))
      },
      actor =>
        if (actor.isDefined) {
          updateCache(DeclarationAdditionalActorsData(cache.actors).addActor(actor))
            .map(_ => navigator.continueTo(mode, routes.AdditionalActorsSummaryController.displayPage))
        } else if (cache.actors.nonEmpty) {
          updateCache(DeclarationAdditionalActorsData(cache.actors))
            .map(_ => navigator.continueTo(mode, routes.AdditionalActorsSummaryController.displayPage))
        } else
          updateCache(DeclarationAdditionalActorsData(cache.actors))
            .map(_ => navigator.continueTo(mode, routes.DeclarationHolderController.displayPage))
    )
  }

  private def updateCache(formData: DeclarationAdditionalActorsData)(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedParties = model.parties.copy(declarationAdditionalActorsData = Some(formData))
      model.copy(parties = updatedParties)
    })
}
