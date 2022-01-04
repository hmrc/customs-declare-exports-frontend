/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.declaration.routes._
import controllers.helpers.DeclarationHolderHelper.declarationHolders
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.declarationHolder.DeclarationHolder
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_remove

class DeclarationHolderRemoveController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  holderRemovePage: declaration_holder_remove
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val maybeExistingHolder = declarationHolders.find(_.id.equals(id))

    maybeExistingHolder.fold(errorHandler.displayErrorPage) { holder =>
      Future.successful(Ok(holderRemovePage(mode, holder, removeYesNoForm.withSubmissionErrors)))
    }
  }

  def submitForm(mode: Mode, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val maybeExistingHolder = declarationHolders.find(_.id.equals(id))

    maybeExistingHolder.fold(errorHandler.displayErrorPage) { holderToRemove =>
      removeYesNoForm.bindFromRequest
        .fold(
          formWithErrors => Future.successful(BadRequest(holderRemovePage(mode, holderToRemove, formWithErrors))),
          _.answer match {
            case YesNoAnswers.yes =>
              updateExportsCache(holderToRemove)
                .map(declarationAfterRemoval => navigator.continueTo(mode, nextPageAfterRemoval(declarationAfterRemoval)))

            case YesNoAnswers.no =>
              Future.successful(navigator.continueTo(mode, DeclarationHolderSummaryController.displayPage))
          }
        )
    }
  }

  private val removeYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.declarationHolders.remove.empty")

  private def updateExportsCache(holderToRemove: DeclarationHolder)(implicit r: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedHolders = declarationHolders.filterNot(_ == holderToRemove)
      val updatedHoldersData = model.parties.declarationHoldersData.map(_.copy(holders = updatedHolders))
      model.copy(parties = model.parties.copy(declarationHoldersData = updatedHoldersData))
    })

  private def nextPageAfterRemoval(declarationAfterRemoval: Option[ExportsDeclaration]): Mode => Call = {
    val holdersData = declarationAfterRemoval.flatMap(_.parties.declarationHoldersData)
    if (holdersData.exists(_.holders.isEmpty) && holdersData.exists(_.isRequired.isDefined)) DeclarationHolderRequiredController.displayPage
    else DeclarationHolderSummaryController.displayPage
  }
}
