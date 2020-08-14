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
import controllers.util._
import forms.declaration.DeclarationHolder
import forms.declaration.DeclarationHolder.form
import javax.inject.Inject
import models.declaration.DeclarationHoldersData
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_add

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
    val holders = cachedHolders
    Ok(declarationHolderPage(mode, form(holders.isEmpty).withSubmissionErrors()))
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val holders = cachedHolders
    val boundForm = form(holders.isEmpty).bindFromRequest()
    boundForm.fold(
      formWithErrors => {
        Future.successful(BadRequest(declarationHolderPage(mode, formWithErrors)))
      },
      holder =>
        if (holder.isComplete)
          saveHolder(mode, boundForm, holders)
        else
          Future.successful(continue(mode, holders))
    )
  }

  private def cachedHolders(implicit request: JourneyRequest[_]) =
    request.cacheModel.parties.declarationHoldersData.map(_.holders).getOrElse(Seq.empty)

  private def saveHolder(mode: Mode, boundForm: Form[DeclarationHolder], cachedData: Seq[DeclarationHolder])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, DeclarationHoldersData.limitOfHolders)
      .fold(
        formWithErrors => Future.successful(BadRequest(declarationHolderPage(mode, formWithErrors))),
        updatedCache =>
          updateExportsCache(updatedCache)
            .map(_ => navigator.continueTo(mode, controllers.declaration.routes.DeclarationHolderController.displayPage))
      )

  private def continue(mode: Mode, cachedData: Seq[DeclarationHolder])(implicit request: JourneyRequest[AnyContent]): Result =
    if (cachedData.isEmpty)
      navigator.continueTo(mode, DeclarationHolderController.nextPage)
    else
      navigator.continueTo(mode, controllers.declaration.routes.DeclarationHolderController.displayPage)

  private def updateExportsCache(holders: Seq[DeclarationHolder])(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedParties = model.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders)))
      model.copy(parties = updatedParties)
    })
}
