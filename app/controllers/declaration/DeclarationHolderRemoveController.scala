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
import controllers.util.DeclarationHolderHelper
import controllers.util.DeclarationHolderHelper.cachedHolders
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.declarationHolder.DeclarationHolder
import handlers.ErrorHandler
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_remove

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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
    val maybeExistingHolder = cachedHolders.find(_.id.equals(id))

    maybeExistingHolder.fold(errorHandler.displayErrorPage()) { holder =>
      Future.successful(Ok(holderRemovePage(mode, holder, removeYesNoForm.withSubmissionErrors())))
    }
  }

  def submitForm(mode: Mode, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val maybeExistingHolder = cachedHolders.find(_.id.equals(id))

    maybeExistingHolder.fold(errorHandler.displayErrorPage()) { holderToRemove =>
      removeYesNoForm
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[YesNoAnswer]) => Future.successful(BadRequest(holderRemovePage(mode, holderToRemove, formWithErrors))),
          formData => {
            formData.answer match {
              case YesNoAnswers.yes =>
                updateExportsCache(holderToRemove)
                  .map(declarationAfterRemoval => navigator.continueTo(mode, nextPageAfterRemoval(declarationAfterRemoval)))
              case YesNoAnswers.no =>
                Future.successful(navigator.continueTo(mode, routes.DeclarationHolderSummaryController.displayPage))
            }
          }
        )
    }
  }

  private val removeYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.declarationHolders.remove.empty")

  private def updateExportsCache(
    holderToRemove: DeclarationHolder
  )(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedHolders =
        model.parties.declarationHoldersData.map(_.copy(holders = DeclarationHolderHelper.cachedHolders.filterNot(_ == holderToRemove)))
      val updatedParties = model.parties.copy(declarationHoldersData = updatedHolders)
      model.copy(parties = updatedParties)
    })

  private def nextPageAfterRemoval(declarationAfterRemoval: Option[ExportsDeclaration]): Mode => Call = {
    val holdersData = declarationAfterRemoval.flatMap(_.parties.declarationHoldersData)
    if (holdersData.exists(_.holders.isEmpty) && holdersData.exists(_.isRequired.isDefined))
      routes.DeclarationHolderRequiredController.displayPage
    else
      routes.DeclarationHolderSummaryController.displayPage
  }

}
