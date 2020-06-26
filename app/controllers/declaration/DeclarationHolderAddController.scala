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
import controllers.util._
import forms.declaration.DeclarationHolder
import forms.declaration.DeclarationHolder.form
import javax.inject.Inject
import models.declaration.DeclarationHoldersData
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.HolderOfAuthorisationCode
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_add

import scala.concurrent.{ExecutionContext, Future}

class DeclarationHolderAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  customsExportsCodelistsConnector: CustomsExportsCodelistsConnector,
  mcc: MessagesControllerComponents,
  declarationHolderPage: declaration_holder_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsExportsCodelistsConnector.authorisationCodes().map { holderCodes =>
      val holders = cachedHolders
      Ok(declarationHolderPage(mode, form(holderCodes, holders.isEmpty).withSubmissionErrors(), holderCodes))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsExportsCodelistsConnector.authorisationCodes().flatMap { holderCodes =>
      val holders = cachedHolders
      val boundForm = form(holderCodes, holders.isEmpty).bindFromRequest()
      boundForm.fold(
        formWithErrors => {
          Future.successful(BadRequest(declarationHolderPage(mode, formWithErrors, holderCodes)))
        },
        holder =>
          if (holder.isComplete)
            saveHolder(mode, boundForm, holders, holderCodes)
          else if (holder.isEmpty)
            continue(mode, holders)
          else
            Future.successful(BadRequest(declarationHolderPage(mode, appendMissingFieldErrors(boundForm), holderCodes)))
      )
    }
  }

  private def appendMissingFieldErrors(form: Form[DeclarationHolder]) = {
    val missingEori = if (form.data("eori").isEmpty) Some(FormError("eori", "declaration.declarationHolder.eori.empty")) else None
    val missingCode =
      if (form.data("authorisationTypeCode").isEmpty)
        Some(FormError("authorisationTypeCode", "declaration.declarationHolder.authorisationCode.empty"))
      else None
    form.copy(errors = MultipleItemsHelper.appendAll(form.errors, missingCode, missingEori))
  }

  private def cachedHolders(implicit request: JourneyRequest[_]) =
    request.cacheModel.parties.declarationHoldersData.map(_.holders).getOrElse(Seq.empty)

  private def saveHolder(mode: Mode, boundForm: Form[DeclarationHolder], cachedData: Seq[DeclarationHolder], holderCodes: List[HolderOfAuthorisationCode])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, DeclarationHoldersData.limitOfHolders)
      .fold(
        formWithErrors => Future.successful(BadRequest(declarationHolderPage(mode, formWithErrors, holderCodes))),
        updatedCache =>
          updateExportsCache(updatedCache)
            .map(_ => navigator.continueTo(mode, controllers.declaration.routes.DeclarationHolderController.displayPage))
      )

  private def continue(mode: Mode, cachedData: Seq[DeclarationHolder])(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateExportsCache(cachedData)
      .map(
        _ =>
          if (cachedData.isEmpty)
            navigator.continueTo(mode, DeclarationHolderController.nextPage)
          else
            navigator.continueTo(mode, controllers.declaration.routes.DeclarationHolderController.displayPage)
      )

  private def updateExportsCache(holders: Seq[DeclarationHolder])(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedParties = model.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders)))
      model.copy(parties = updatedParties)
    })
}
