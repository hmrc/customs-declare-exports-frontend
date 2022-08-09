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

package controllers

import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, JourneyAction, VerifiedEmailAction}
import controllers.declaration.routes.SummaryController
import controllers.declaration.{ModelCacheable, SubmissionErrors}
import controllers.routes.{CopyDeclarationController, DeclarationDetailsController}
import forms.CopyDeclaration.form
import forms.declaration.ConsignmentReferences
import forms.{CopyDeclaration, Ducr, LrnValidator}
import models.DeclarationStatus.DRAFT
import models.Mode.Normal
import models.requests.{ExportsSessionKeys, JourneyRequest}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.helpers.DeclarationDetailsLinks.isDeclarationRejected
import views.html.copy_declaration

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CopyDeclarationController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  journeyType: JourneyAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  override val exportsCacheService: ExportsCacheService,
  lrnValidator: LrnValidator,
  mcc: MessagesControllerComponents,
  copyDeclarationPage: copy_declaration
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def redirectToReceiveJourneyRequest(submissionId: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    customsDeclareExportsConnector.findSubmission(submissionId).map {
      case Some(submission) if !isDeclarationRejected(submission) =>
        Redirect(CopyDeclarationController.displayPage)
          .addingToSession(ExportsSessionKeys.declarationId -> submissionId)

      case _ => Redirect(DeclarationDetailsController.displayPage(submissionId))
    }
  }

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    Future.successful(Ok(copyDeclarationPage(form.withSubmissionErrors)))
  }

  val submitPage: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form.bindFromRequest
      .verifyLrnValidity(lrnValidator)
      .flatMap {
        _.fold(formWithErrors => Future.successful(BadRequest(copyDeclarationPage(formWithErrors))), copyDeclaration)
      }
  }

  private def copyDeclaration(data: CopyDeclaration)(implicit request: JourneyRequest[_]): Future[Result] = {
    val declaration = request.cacheModel.copy(
      parentDeclarationId = None,
      status = DRAFT,
      createdDateTime = Instant.now,
      updatedDateTime = Instant.now,
      consignmentReferences = Some(ConsignmentReferences(Ducr(data.ducr.ducr.toUpperCase), data.lrn)),
      linkDucrToMucr = None,
      mucr = None
    )
    exportsCacheService.create(declaration).map { declaration =>
      Redirect(SummaryController.displayPage(Normal)).addingToSession(ExportsSessionKeys.declarationId -> declaration.id)
    }
  }
}
