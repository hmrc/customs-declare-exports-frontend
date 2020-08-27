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
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.DeclarationHolder
import javax.inject.Inject
import models.declaration.DeclarationHoldersData
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_remove

import scala.concurrent.{ExecutionContext, Future}

class DeclarationHolderRemoveController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  holderRemovePage: declaration_holder_remove
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, id: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val holder = DeclarationHolder.fromId(id)
    Ok(holderRemovePage(mode, holder, removeYesNoForm.withSubmissionErrors()))
  }

  def submitForm(mode: Mode, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val holderToRemove = DeclarationHolder.fromId(id)
    removeYesNoForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) => Future.successful(BadRequest(holderRemovePage(mode, holderToRemove, formWithErrors))),
        formData => {
          formData.answer match {
            case YesNoAnswers.yes =>
              updateExportsCache(holderToRemove)
                .map(_ => navigator.continueTo(mode, routes.DeclarationHolderController.displayPage))
            case YesNoAnswers.no =>
              Future.successful(navigator.continueTo(mode, routes.DeclarationHolderController.displayPage))
          }
        }
      )
  }

  private def removeYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.declarationHolders.remove.empty")

  private def updateExportsCache(
    itemToRemove: DeclarationHolder
  )(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] = {
    val updatedHolders = request.cacheModel.parties.declarationHoldersData.map(_.holders).getOrElse(Seq.empty).filterNot(_ == itemToRemove)
    val updatedParties = request.cacheModel.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(updatedHolders)))
    updateExportsDeclarationSyncDirect(model => model.copy(parties = updatedParties))
  }
}
