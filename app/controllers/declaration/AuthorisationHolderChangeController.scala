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

import scala.concurrent.{ExecutionContext, Future}
import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.routes.AuthorisationHolderSummaryController
import controllers.helpers.AuthorisationHolderHelper._
import controllers.helpers.MultipleItemsHelper
import controllers.navigation.Navigator
import forms.declaration.authorisationHolder.AuthorisationHolder
import forms.declaration.authorisationHolder.AuthorisationHolder.authorisationHolderFormGroupId
import handlers.ErrorHandler

import javax.inject.Inject
import models.declaration.AuthorisationHolders
import models.declaration.AuthorisationHolders.limitOfHolders
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.authorisationHolder.authorisation_holder_change

class AuthorisationHolderChangeController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  authorisationHolderChangePage: authorisation_holder_change
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val maybeExistingHolder = authorisationHolders.find(_.id.equals(id))

    maybeExistingHolder.fold(errorHandler.redirectToErrorPage) { holder =>
      Future.successful(Ok(authorisationHolderChangePage(id, form.fill(holder).withSubmissionErrors, request.eori)))
    }
  }

  def submitForm(id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form.bindFromRequest()
    val maybeExistingHolder = authorisationHolders.find(_.id.equals(id))

    boundForm.fold(
      formWithErrors => Future.successful(BadRequest(authorisationHolderChangePage(id, formWithErrors, request.eori))),
      newHolder =>
        maybeExistingHolder.fold(errorHandler.redirectToErrorPage) { existingHolder =>
          changeHolder(existingHolder, newHolder, boundForm)
        }
    )
  }

  private def changeHolder(existingHolder: AuthorisationHolder, newHolder: AuthorisationHolder, boundForm: Form[AuthorisationHolder])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {

    val cachedHolders = authorisationHolders
    val holdersWithoutExisting: Seq[AuthorisationHolder] = cachedHolders.filterNot(_ == existingHolder)

    MultipleItemsHelper
      .add(boundForm, holdersWithoutExisting, limitOfHolders, authorisationHolderFormGroupId, "declaration.authorisationHolder")
      .fold(
        formWithErrors => Future.successful(BadRequest(authorisationHolderChangePage(existingHolder.id, formWithErrors, request.eori))),
        _ => {
          val updatedHolders = cachedHolders.map(holder => if (holder == existingHolder) newHolder else holder)
          updateExportsCache(updatedHolders).map(_ => navigator.continueTo(AuthorisationHolderSummaryController.displayPage))
        }
      )
  }

  private def updateExportsCache(holders: Seq[AuthorisationHolder])(implicit r: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      val isRequired = model.parties.declarationHoldersData.flatMap(_.isRequired)
      val updatedParties = model.parties.copy(declarationHoldersData = Some(AuthorisationHolders(holders, isRequired)))
      model.copy(parties = updatedParties)
    }
}
