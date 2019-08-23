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

  val logger = Logger.apply(this.getClass)

  def displayPage(): Action[AnyContent] = authenticate.async { implicit request =>
    request.declarationId match {
      case Some(id) =>
        exportsCacheService.get(id).map(_.map(_.choice)).map {
          case Some(data) => Ok(choicePage(Choice.form().fill(Choice(data))))
          case _          => Ok(choicePage(Choice.form()))
        }
      case None => Future.successful(Ok(choicePage(Choice.form())))
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
              exportsCacheService
                .create(
                  ExportsDeclaration(
                    None,
                    DeclarationStatus.DRAFT,
                    createdDateTime = Instant.now,
                    updatedDateTime = Instant.now,
                    choice.value
                  )
                ) map { created =>
                Redirect(controllers.declaration.routes.DispatchLocationController.displayPage(Mode.Normal))
                  .addingToSession(ExportsSessionKeys.declarationId -> created.id.get)
              }
            case CancelDec =>
              Future.successful(Redirect(controllers.routes.CancelDeclarationController.displayForm()))
            case ContinueDec =>
              Future.successful(Redirect(controllers.routes.SavedDeclarationsController.displayDeclarations()))
            case Submissions =>
              Future.successful(Redirect(controllers.routes.SubmissionsController.displayListOfSubmissions()))
        }
      )
  }

}
