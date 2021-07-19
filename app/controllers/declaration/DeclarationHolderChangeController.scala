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
import controllers.util.MultipleItemsHelper
import forms.declaration.declarationHolder.DeclarationHolderAdd.form
import forms.declaration.declarationHolder.DeclarationHolderAdd
import models.declaration.DeclarationHoldersData
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_change

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationHolderChangeController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarationHolderChangePage: declaration_holder_change
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, id: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val holder = DeclarationHolderAdd.fromId(id)
    Ok(declarationHolderChangePage(mode, id, form.fill(holder).withSubmissionErrors()))
  }

  def submitForm(mode: Mode, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form.bindFromRequest()
    boundForm.fold(
      formWithErrors => Future.successful(BadRequest(declarationHolderChangePage(mode, id, formWithErrors))),
      holder => changeHolder(mode, DeclarationHolderAdd.fromId(id), holder, boundForm)
    )
  }

  private def changeHolder(mode: Mode, existingHolder: DeclarationHolderAdd, newHolder: DeclarationHolderAdd, boundForm: Form[DeclarationHolderAdd])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {

    val holdersWithoutExisting: Seq[DeclarationHolderAdd] = cachedHolders.filterNot(_ == existingHolder)

    MultipleItemsHelper
      .add(boundForm, holdersWithoutExisting, DeclarationHoldersData.limitOfHolders, DeclarationHolderFormGroupId, "declaration.declarationHolder")
      .fold(
        formWithErrors => Future.successful(BadRequest(declarationHolderChangePage(mode, existingHolder.id, formWithErrors))),
        _ => {
          val updatedHolders = cachedHolders.map(holder => if (holder == existingHolder) newHolder else holder)
          updateExportsCache(updatedHolders)
            .map(_ => navigator.continueTo(mode, routes.DeclarationHolderSummaryController.displayPage))
        }
      )
  }

  private def updateExportsCache(holders: Seq[DeclarationHolderAdd])(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedParties = model.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders)))
      model.copy(parties = updatedParties)
    })
}
