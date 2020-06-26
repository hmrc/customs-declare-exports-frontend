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

import connectors.CustomsExportsCodelistsConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import controllers.util.MultipleItemsHelper
import forms.declaration.DeclarationHolder
import forms.declaration.DeclarationHolder.form
import javax.inject.Inject
import models.declaration.DeclarationHoldersData
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.HolderOfAuthorisationCode
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_change

import scala.concurrent.{ExecutionContext, Future}

class DeclarationHolderChangeController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  customsExportsCodelistsConnector: CustomsExportsCodelistsConnector,
  mcc: MessagesControllerComponents,
  declarationHolderChangePage: declaration_holder_change
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsExportsCodelistsConnector.authorisationCodes().map { holders =>
      val holder = DeclarationHolder.buildId(id)
      Ok(declarationHolderChangePage(mode, id, form(holders).fill(holder).withSubmissionErrors(), holders))
    }
  }

  def submitForm(mode: Mode, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsExportsCodelistsConnector.authorisationCodes().flatMap { holders =>
      val boundForm = form(holders).bindFromRequest()
      boundForm.fold(
        formWithErrors => Future.successful(BadRequest(declarationHolderChangePage(mode, id, formWithErrors, holders))),
        holder => changeHolder(mode, DeclarationHolder.buildId(id), holder, boundForm, holders)
      )
    }
  }

  private def changeHolder(mode: Mode, existingHolder: DeclarationHolder, newHolder: DeclarationHolder, boundForm: Form[DeclarationHolder], holderCodes: List[HolderOfAuthorisationCode])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {
    val cachedHolders: Seq[DeclarationHolder] = request.cacheModel.parties.declarationHoldersData.map(_.holders).getOrElse(Seq.empty)
    val holdersWithoutExisting: Seq[DeclarationHolder] = cachedHolders.filterNot(_ == existingHolder)

    MultipleItemsHelper
      .add(boundForm, holdersWithoutExisting, DeclarationHoldersData.limitOfHolders)
      .fold(
        formWithErrors => Future.successful(BadRequest(declarationHolderChangePage(mode, existingHolder.id, formWithErrors, holderCodes))),
        _ => {
          val updatedHolders: Seq[DeclarationHolder] = cachedHolders.map(holder => if (holder == existingHolder) newHolder else holder)
          updateExportsCache(updatedHolders)
            .map(_ => navigator.continueTo(mode, controllers.declaration.routes.DeclarationHolderController.displayPage))
        }
      )
  }

  private def updateExportsCache(holders: Seq[DeclarationHolder])(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedParties = model.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders)))
      model.copy(parties = updatedParties)
    })
}
