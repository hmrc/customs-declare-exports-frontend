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

import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.declaration.routes.AdditionalDeclarationTypeController
import forms.declaration.DeclarationChoice
import forms.declaration.DeclarationChoice._
import models.DeclarationType.{CLEARANCE, DeclarationType, SIMPLIFIED}
import models.requests.ExportsSessionKeys
import models.{DeclarationStatus, ExportsDeclaration, Mode}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declaration_choice

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationChoiceController @Inject()(
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  choicePage: declaration_choice
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging with ModelCacheable with WithDefaultFormBinding {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    request.declarationId match {
      case Some(id) =>
        exportsCacheService.get(id).map {
          case Some(declaration) => Ok(choicePage(mode, form.fill(DeclarationChoice(declaration.`type`))))
          case _                 => Ok(choicePage(mode, form))
        }

      case _ => Future.successful(Ok(choicePage(mode, form)))
    }
  }

  def submitChoice(mode: Mode): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    form.bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(choicePage(mode, formWithErrors))),
        declarationType =>
          request.declarationId match {
            case Some(id) => updateDeclarationType(id, declarationType.value).map(_ => nextPage(mode, id))
            case _        => create(declarationType.value).map(created => nextPage(mode, created.id))
        }
      )
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
        status = DeclarationStatus.INITIAL,
        createdDateTime = Instant.now,
        updatedDateTime = Instant.now,
        sourceId = None,
        `type` = declarationType
      )
    )

  private def nextPage(mode: Mode, declarationId: String)(implicit request: RequestHeader): Result =
    Redirect(AdditionalDeclarationTypeController.displayPage(mode))
      .addingToSession(ExportsSessionKeys.declarationId -> declarationId)

  private def updateDeclarationType(id: String, `type`: DeclarationType)(implicit hc: HeaderCarrier): Future[Option[ExportsDeclaration]] = {
    val updatedDeclaration = exportsCacheService.get(id).map { maybeDeclaration =>
      maybeDeclaration
        .map(_.updateType(`type`))
        .map(clearAuthorisationProcedureCodeChoiceIfRequired)
    }

    updatedDeclaration.flatMap {
      case Some(declaration) => exportsCacheService.update(declaration).map(Some(_))
      case None =>
        logger.error(s"Failed to find declaration for id $id")
        Future.successful(None)
    }
  }
}
