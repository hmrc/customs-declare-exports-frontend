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

import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.declaration.routes.AdditionalDeclarationTypeController
import forms.declaration.DeclarationChoice
import forms.declaration.DeclarationChoice._
import models.DeclarationType.{CLEARANCE, DeclarationType, SIMPLIFIED}
import models.declaration.DeclarationStatus
import models.requests.SessionHelper
import models.{DeclarationMeta, ExportsDeclaration}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declaration_choice

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationChoiceController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  choicePage: declaration_choice
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging with ModelCacheable with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    request.declarationId match {
      case Some(declarationId) =>
        exportsCacheService.get(declarationId).map {
          case Some(declaration) if declaration.isAmendmentDraft => nextPage(declarationId)
          case Some(declaration)                                 => Ok(choicePage(form.fill(DeclarationChoice(declaration.`type`))))
          case _                                                 => Ok(choicePage(form))
        }

      case _ => Future.successful(Ok(choicePage(form)))
    }
  }

  val submitChoice: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    val nextPageOrMaybeDeclaration = request.declarationId match {
      case Some(declarationId) =>
        exportsCacheService.get(declarationId).map {
          case Some(declaration) if declaration.isAmendmentDraft => Left(nextPage(declarationId))
          case Some(declaration)                                 => Right(Some(declaration))
          case _                                                 => Right(None)
        }

      case _ => Future.successful(Right(None))
    }

    nextPageOrMaybeDeclaration.flatMap {
      case Left(result) => Future.successful(result)
      case Right(maybeDeclaration) =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(choicePage(formWithErrors))),
            declarationType =>
              maybeDeclaration
                .map(_.updateType(declarationType.value))
                .map(clearAuthorisationProcedureCodeChoiceIfRequired)
                .map(exportsCacheService.update(_))
                .getOrElse(create(declarationType.value))
                .map(declaration => nextPage(declaration.id))
          )
    }
  }

  private def clearAuthorisationProcedureCodeChoiceIfRequired(declaration: ExportsDeclaration): ExportsDeclaration =
    declaration.`type` match {
      case CLEARANCE | SIMPLIFIED => declaration.removeAuthorisationProcedureCodeChoice
      case _                      => declaration
    }

  private def create(declarationType: DeclarationType)(implicit hc: HeaderCarrier): Future[ExportsDeclaration] =
    exportsCacheService.create(
      ExportsDeclaration(
        id = "",
        declarationMeta = DeclarationMeta(status = DeclarationStatus.INITIAL, createdDateTime = Instant.now, updatedDateTime = Instant.now),
        `type` = declarationType
      )
    )

  private def nextPage(declarationId: String)(implicit request: RequestHeader): Result =
    Redirect(AdditionalDeclarationTypeController.displayPage)
      .addingToSession(SessionHelper.declarationUuid -> declarationId)
}
