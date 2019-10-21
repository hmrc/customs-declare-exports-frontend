/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.Instant

import connectors.exchange.ExportsDeclarationExchange
import controllers.actions.AuthAction
import controllers.declaration.ModelCacheable
import forms.Choice
import forms.Choice.AllowedChoiceValues._
import forms.Choice._
import javax.inject.Inject
import models.requests.ExportsSessionKeys
import models.{DeclarationStatus, ExportsDeclaration, Mode}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.choice_page

import scala.concurrent.{ExecutionContext, Future}

class ChoiceController @Inject()(
  authenticate: AuthAction,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  choicePage: choice_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  private val logger = Logger(this.getClass)

  def displayPage(previousChoice: Option[Choice]): Action[AnyContent] = authenticate.async { implicit request =>
    def pageForPreviousChoice(previousChoice: Option[Choice]) = {
      val form = Choice.form()
      choicePage(previousChoice.fold(form)(form.fill))
    }

    request.declarationId match {
      case Some(id) if previousChoice.isEmpty =>
        exportsCacheService.get(id).map(_.map(_.`type`)).map {
          case Some(data) => Ok(choicePage(Choice.form().fill(Choice(data))))
          case _          => Ok(choicePage(Choice.form()))
        }
      case _ => Future.successful(Ok(pageForPreviousChoice(previousChoice)))
    }

  }

  def submitChoice(): Action[AnyContent] = authenticate.async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Choice]) => Future.successful(BadRequest(choicePage(formWithErrors))),
        choice =>
          choice.value match {
            case SupplementaryDec | StandardDec =>
              request.declarationId match {
                case Some(id) =>
                  updateChoice(id, choice).map { _ =>
                    Redirect(controllers.declaration.routes.DispatchLocationController.displayPage(Mode.Normal))
                  }
                case _ =>
                  create(choice) map { created =>
                    Redirect(controllers.declaration.routes.DispatchLocationController.displayPage(Mode.Normal))
                      .addingToSession(ExportsSessionKeys.declarationId -> created.id)
                  }
              }
            case CancelDec =>
              Future.successful(Redirect(controllers.routes.CancelDeclarationController.displayPage()))
            case ContinueDec =>
              Future.successful(Redirect(controllers.routes.SavedDeclarationsController.displayDeclarations()))
            case Submissions =>
              Future.successful(Redirect(controllers.routes.SubmissionsController.displayListOfSubmissions()))
        }
      )
  }

  private def updateChoice(id: String, choice: Choice)(implicit hc: HeaderCarrier) =
    exportsCacheService.get(id).map(_.map(_.copy(`type` = choice.toDeclarationType.get))).flatMap {
      case Some(declaration) => exportsCacheService.update(declaration)
      case None =>
        logger.error(s"Failed to find declaration for id $id")
        Future.successful(None)
    }

  private def create(choice: Choice)(implicit hc: HeaderCarrier) =
    exportsCacheService
      .create(
        ExportsDeclarationExchange(
          None,
          DeclarationStatus.DRAFT,
          createdDateTime = Instant.now,
          updatedDateTime = Instant.now,
          sourceId = None,
          choice.toDeclarationType.get
        )
      )
}
