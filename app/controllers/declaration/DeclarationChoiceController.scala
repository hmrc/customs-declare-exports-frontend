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

import java.time.Instant
import connectors.exchange.ExportsDeclarationExchange
import controllers.actions.{AuthAction, VerifiedEmailAction}
import forms.declaration.DeclarationChoice
import forms.declaration.DeclarationChoice._

import javax.inject.Inject
import models.DeclarationType.DeclarationType
import models.requests.ExportsSessionKeys
import models.{DeclarationStatus, Mode}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declaration_choice

import scala.concurrent.{ExecutionContext, Future}

class DeclarationChoiceController @Inject()(
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  choicePage: declaration_choice
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  private val logger = Logger(this.getClass)

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    request.declarationId match {
      case Some(id) =>
        exportsCacheService.get(id).map(_.map(_.`type`)).map {
          case Some(data) => Ok(choicePage(mode, form().fill(DeclarationChoice(data))))
          case _          => Ok(choicePage(mode, form()))
        }
      case _ => Future.successful(Ok(choicePage(mode, form())))
    }
  }

  def submitChoice(mode: Mode): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[DeclarationChoice]) => Future.successful(BadRequest(choicePage(mode, formWithErrors))),
        choice => {
          val declarationType = choice.value

          request.declarationId match {
            case Some(id) =>
              updateDeclarationType(id, declarationType).map { _ =>
                Redirect(controllers.declaration.routes.DispatchLocationController.displayPage(mode))
              }
            case _ =>
              create(declarationType) map { created =>
                Redirect(controllers.declaration.routes.DispatchLocationController.displayPage(mode))
                  .addingToSession(ExportsSessionKeys.declarationId -> created.id)
              }
          }
        }
      )
  }

  private def updateDeclarationType(id: String, `type`: DeclarationType)(implicit hc: HeaderCarrier) =
    exportsCacheService.get(id).map(_.map(_.updateType(`type`))).flatMap {
      case Some(declaration) => exportsCacheService.update(declaration)
      case None =>
        logger.error(s"Failed to find declaration for id $id")
        Future.successful(None)
    }

  private def create(`type`: DeclarationType)(implicit hc: HeaderCarrier) =
    exportsCacheService
      .create(
        ExportsDeclarationExchange(
          None,
          DeclarationStatus.INITIAL,
          createdDateTime = Instant.now,
          updatedDateTime = Instant.now,
          sourceId = None,
          `type`
        )
      )
}
