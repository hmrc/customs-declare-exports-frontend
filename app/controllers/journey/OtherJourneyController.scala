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

package controllers.journey

import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.general.ModelCacheable
import controllers.section1.routes.AdditionalDeclarationTypeController
import forms.journey.JourneySelection._
import models.DeclarationType.{CLEARANCE, DeclarationType, SIMPLIFIED, STANDARD}
import models.declaration.DeclarationStatus.INITIAL
import models.requests.SessionHelper
import models.{DeclarationMeta, DeclarationType, ExportsDeclaration}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.journey.other_journey

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherJourneyController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  otherJourney: other_journey
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    request.declarationId.fold {
      Future.successful(Ok(otherJourney(form(nonStandardJourneys))))
    } { declarationId =>
      exportsCacheService.get(declarationId).map {
        case Some(declaration) if declaration.isAmendmentDraft => nextPage(declarationId)

        case Some(declaration) if !declaration.isType(STANDARD) =>
          Ok(otherJourney(form(nonStandardJourneys).fill(declaration.`type`.toString)))

        case _ => Ok(otherJourney(form(nonStandardJourneys)))
      }
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
        form(nonStandardJourneys)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(otherJourney(formWithErrors))),
            declarationType =>
              maybeDeclaration
                .map(_.updateType(DeclarationType.withName(declarationType)))
                .map(clearAuthorisationProcedureCodeChoiceIfRequired)
                .map(exportsCacheService.update(_, request.user.eori))
                .getOrElse(create(DeclarationType.withName(declarationType), request.user.eori))
                .map(declaration => nextPage(declaration.id))
          )
    }
  }

  private def clearAuthorisationProcedureCodeChoiceIfRequired(declaration: ExportsDeclaration): ExportsDeclaration =
    declaration.`type` match {
      case CLEARANCE | SIMPLIFIED => declaration.removeAuthorisationProcedureCodeChoice
      case _                      => declaration
    }

  private def create(declarationType: DeclarationType, eori: String)(implicit hc: HeaderCarrier): Future[ExportsDeclaration] = {
    val declarationMeta = DeclarationMeta(status = INITIAL, createdDateTime = Instant.now, updatedDateTime = Instant.now)
    exportsCacheService.create(ExportsDeclaration(id = "", declarationMeta = declarationMeta, eori = eori, `type` = declarationType), eori)
  }

  private def nextPage(declarationId: String)(implicit request: RequestHeader): Result =
    Redirect(AdditionalDeclarationTypeController.displayPage)
      .addingToSession(SessionHelper.declarationUuid -> declarationId)
}
