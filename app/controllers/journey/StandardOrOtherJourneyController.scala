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
import controllers.journey.routes.OtherJourneyController
import controllers.general.ModelCacheable
import controllers.section1.routes.AdditionalDeclarationTypeController
import forms.journey.JourneySelection._
import models.DeclarationType.STANDARD
import models.declaration.DeclarationStatus.INITIAL
import models.requests.SessionHelper
import models.{DeclarationMeta, ExportsDeclaration}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.journey.standard_or_other_journey

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StandardOrOtherJourneyController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  standardOrOtherJourney: standard_or_other_journey
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    request.declarationId.fold {
      Future.successful(Ok(standardOrOtherJourney(form(standardOrOtherJourneys))))
    } { declarationId =>
      exportsCacheService.get(declarationId).map {
        case Some(declaration) if declaration.isAmendmentDraft => nextPage(Some(declarationId), declaration.`type`.toString)

        case Some(declaration) =>
          val declarationType = if (declaration.isType(STANDARD)) StandardDeclarationType else NonStandardDeclarationType
          Ok(standardOrOtherJourney(form(standardOrOtherJourneys).fill(declarationType)))

        case _ => Ok(standardOrOtherJourney(form(standardOrOtherJourneys)))
      }
    }
  }

  val submitChoice: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    val nextPageOrMaybeDeclaration = request.declarationId match {
      case Some(declarationId) =>
        exportsCacheService.get(declarationId).map {
          case Some(declaration) if declaration.isAmendmentDraft => Left(nextPage(Some(declarationId), declaration.`type`.toString))

          case Some(declaration) => Right(Some(declaration))
          case _                 => Right(None)
        }

      case _ => Future.successful(Right(None))
    }

    nextPageOrMaybeDeclaration.flatMap {
      case Left(result) => Future.successful(result)

      case Right(maybeDeclaration) =>
        form(standardOrOtherJourneys)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(standardOrOtherJourney(formWithErrors))),
            declarationType =>
              maybeDeclaration.fold {
                if (declarationType == StandardDeclarationType) createAndNextPage(request.user.eori)
                else Future.successful(nextPage(None, NonStandardDeclarationType))
              } { declaration =>
                if (declarationType == declaration.`type`.toString)
                  Future.successful(nextPage(Some(declaration.id), StandardDeclarationType))
                else if (declarationType == StandardDeclarationType) updateAndNextPage(declaration, request.user.eori)
                else Future.successful(nextPage(Some(declaration.id), NonStandardDeclarationType))
              }
          )
    }
  }

  private def createAndNextPage(eori: String)(implicit request: RequestHeader): Future[Result] = {
    val declarationMeta = DeclarationMeta(status = INITIAL, createdDateTime = Instant.now, updatedDateTime = Instant.now)
    exportsCacheService
      .create(ExportsDeclaration(id = "", declarationMeta = declarationMeta, eori = eori, `type` = STANDARD), eori)
      .map(declaration => nextPage(Some(declaration.id), StandardDeclarationType))
  }

  private def nextPage(declarationId: Option[String], declarationType: String)(implicit request: RequestHeader): Result = {
    val result =
      if (declarationType == StandardDeclarationType) Redirect(AdditionalDeclarationTypeController.displayPage)
      else Redirect(OtherJourneyController.displayPage)
    declarationId.fold(result)(id => result.addingToSession(SessionHelper.declarationUuid -> id))
  }

  private def updateAndNextPage(declaration: ExportsDeclaration, eori: String)(implicit request: RequestHeader): Future[Result] =
    exportsCacheService
      .update(declaration.updateType(STANDARD), eori)
      .map(declaration => nextPage(Some(declaration.id), StandardDeclarationType))
}
