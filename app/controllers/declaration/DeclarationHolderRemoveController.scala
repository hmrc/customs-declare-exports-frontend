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
import controllers.declaration.routes.{DeclarationHolderAddController, DeclarationHolderRequiredController, DeclarationHolderSummaryController}
import controllers.helpers.DeclarationHolderHelper.{declarationHolders, userCanLandOnIsAuthRequiredPage}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.declarationHolder.DeclarationHolder
import handlers.ErrorHandler
import models.declaration.DeclarationHoldersData
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_remove

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationHolderRemoveController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  holderRemovePage: declaration_holder_remove
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val maybeExistingHolder = declarationHolders.find(_.id.equals(id))

    maybeExistingHolder.fold(errorHandler.displayErrorPage) { holder =>
      Future.successful(Ok(holderRemovePage(holder, removeYesNoForm.withSubmissionErrors)))
    }
  }

  def submitForm(id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val maybeExistingHolder = declarationHolders.find(_.id.equals(id))

    maybeExistingHolder.fold(errorHandler.displayErrorPage) { holderToRemove =>
      removeYesNoForm
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(holderRemovePage(holderToRemove, formWithErrors))),
          _.answer match {
            case YesNoAnswers.yes => updateExportsCache(holderToRemove).map(nextPage(_))

            case YesNoAnswers.no =>
              Future.successful(navigator.continueTo(DeclarationHolderSummaryController.displayPage))
          }
        )
    }
  }

  private def nextPage(declaration: ExportsDeclaration)(implicit r: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(
      if (declaration.declarationHolders.nonEmpty) DeclarationHolderSummaryController.displayPage
      else if (userCanLandOnIsAuthRequiredPage(declaration)) DeclarationHolderRequiredController.displayPage
      else DeclarationHolderAddController.displayPage
    )

  private val removeYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.declarationHolders.remove.empty")

  private def updateExportsCache(holderToRemove: DeclarationHolder)(implicit r: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      val maybeHoldersData = declaration.parties.declarationHoldersData

      val newHoldersData = maybeHoldersData.flatMap { holdersData =>
        val holders = holdersData.holders.filterNot(_ == holderToRemove)
        val isRequired = if (userCanLandOnIsAuthRequiredPage(declaration)) holdersData.isRequired else None
        if (holders.isEmpty && isRequired.isEmpty) None else Some(DeclarationHoldersData(holders, isRequired))
      }

      declaration.copy(parties = declaration.parties.copy(declarationHoldersData = newHoldersData))
    }
}
