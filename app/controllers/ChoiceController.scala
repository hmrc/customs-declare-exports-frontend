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

import java.time.LocalDateTime
import java.util.UUID

import controllers.actions.AuthAction
import controllers.declaration.{ModelCacheable, SessionIdAware}
import forms.Choice
import forms.Choice.AllowedChoiceValues._
import forms.Choice._
import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.{ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.choice_page

import scala.concurrent.{ExecutionContext, Future}

class ChoiceController @Inject()(
  authenticate: AuthAction,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  choicePage: choice_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  val logger = Logger.apply(this.getClass)

  def displayPage(): Action[AnyContent] = authenticate.async { implicit request =>
    exportsCacheService.get(request.session.data("sessionId")).map(_.map(_.choice)).map {
      case Some(data) => Ok(choicePage(Choice.form().fill(Choice(data))))
      case _          => Ok(choicePage(Choice.form()))
    }
  }

  def submitChoice(): Action[AnyContent] = authenticate.async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Choice]) => Future.successful(BadRequest(choicePage(formWithErrors))),
        validChoice => {
          exportsCacheService
            .update(
              authenticatedSessionId,
              ExportsCacheModel(
                authenticatedSessionId,
                UUID.randomUUID().toString,
                createdDateTime = LocalDateTime.now,
                updatedDateTime = LocalDateTime.now,
                validChoice.value
              )
            )
            .map(_ => {
              validChoice.value match {
                case SupplementaryDec | StandardDec =>
                  Redirect(controllers.declaration.routes.DispatchLocationController.displayPage())
                case CancelDec =>
                  Redirect(controllers.routes.CancelDeclarationController.displayForm())
                case Submissions =>
                  Redirect(controllers.routes.SubmissionsController.displayListOfSubmissions())
              }
            })
        }
      )
  }

}
