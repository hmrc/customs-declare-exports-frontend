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

import scala.concurrent.{ExecutionContext, Future}

import controllers.actions.{AuthAction, JourneyAction}
import controllers.helpers.DeclarationHolderHelper._
import controllers.helpers.MultipleItemsHelper
import controllers.navigation.Navigator
import forms.declaration.declarationHolder.DeclarationHolder
import forms.declaration.declarationHolder.DeclarationHolder.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.DeclarationHoldersData
import models.declaration.DeclarationHoldersData.limitOfHolders
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_change

class DeclarationHolderChangeController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  declarationHolderChangePage: declaration_holder_change
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val maybeExistingHolder = cachedHolders.find(_.id.equals(id))

    maybeExistingHolder.fold(errorHandler.displayErrorPage()) { holder =>
      Future.successful(Ok(declarationHolderChangePage(mode, id, form(request.eori).fill(holder).withSubmissionErrors(), request.eori)))
    }
  }

  def submitForm(mode: Mode, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form(request.eori).bindFromRequest()
    val maybeExistingHolder = cachedHolders.find(_.id.equals(id))

    boundForm.fold(
      formWithErrors => Future.successful(BadRequest(declarationHolderChangePage(mode, id, formWithErrors, request.eori))),
      newHolder => {
        maybeExistingHolder.fold(errorHandler.displayErrorPage()) { existingHolder =>
          changeHolder(mode, existingHolder, newHolder, boundForm)
        }
      }
    )
  }

  private def changeHolder(mode: Mode, existingHolder: DeclarationHolder, newHolder: DeclarationHolder, boundForm: Form[DeclarationHolder])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {

    val sourceHolders = cachedHolders
    val holdersWithoutExisting: Seq[DeclarationHolder] = sourceHolders.filterNot(_ == existingHolder)

    MultipleItemsHelper
      .add(boundForm, holdersWithoutExisting, limitOfHolders, DeclarationHolderFormGroupId, "declaration.declarationHolder")
      .fold(
        formWithErrors => Future.successful(BadRequest(declarationHolderChangePage(mode, existingHolder.id, formWithErrors, request.eori))),
        _ => {
          validateAuthCode(boundForm.value) match {
            case Some(error) =>
              val formWithError = boundForm.copy(errors = Seq(error))
              Future.successful(BadRequest(declarationHolderChangePage(mode, existingHolder.id, formWithError, request.eori)))

            case _ =>
              val updatedHolders = sourceHolders.map(holder => if (holder == existingHolder) newHolder else holder)
              updateExportsCache(updatedHolders)
                .map(_ => navigator.continueTo(mode, routes.DeclarationHolderSummaryController.displayPage))
          }
        }
      )
  }

  private def updateExportsCache(holders: Seq[DeclarationHolder])(implicit r: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val isRequired = model.parties.declarationHoldersData.flatMap(_.isRequired)
      val updatedParties =
        model.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders, isRequired)))
      model.copy(parties = updatedParties)
    })
}
