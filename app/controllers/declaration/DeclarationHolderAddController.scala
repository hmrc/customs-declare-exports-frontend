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
import controllers.util.DeclarationHolderHelper._
import controllers.util._
import forms.declaration.declarationHolder.DeclarationHolder
import forms.declaration.declarationHolder.DeclarationHolder.form
import models.declaration.DeclarationHoldersData
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_add

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationHolderAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarationHolderPage: declaration_holder_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(declarationHolderPage(mode, form.withSubmissionErrors()))
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form.bindFromRequest()
    boundForm.fold(formWithErrors => {
      Future.successful(BadRequest(declarationHolderPage(mode, formWithErrors)))
    }, _ => saveHolder(mode, boundForm, cachedHolders))
  }

  private def saveHolder(mode: Mode, boundForm: Form[DeclarationHolder], holders: Seq[DeclarationHolder])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, holders, DeclarationHoldersData.limitOfHolders, DeclarationHolderFormGroupId, "declaration.declarationHolder")
      .fold(
        formWithErrors => Future.successful(BadRequest(declarationHolderPage(mode, formWithErrors))),
        updatedHolders =>
          updateExportsCache(updatedHolders)
            .map(_ => navigator.continueTo(mode, routes.DeclarationHolderController.displayPage))
      )

  private def updateExportsCache(holders: Seq[DeclarationHolder])(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedParties = model.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders)))
      model.copy(parties = updatedParties)
    })
}
