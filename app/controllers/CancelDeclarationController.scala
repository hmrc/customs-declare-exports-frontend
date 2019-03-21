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

import config.AppConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.AuthAction
import forms.CancelDeclaration
import forms.CancelDeclaration._
import handlers.ErrorHandler
import javax.inject.Inject
import metrics.ExportsMetrics
import metrics.MetricIdentifiers._
import models.requests._
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{cancel_declaration, cancellation_confirmation_page}

import scala.concurrent.{ExecutionContext, Future}

class CancelDeclarationController @Inject()(
  appConfig: AppConfig,
  authenticate: AuthAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  errorHandler: ErrorHandler,
  exportsMetrics: ExportsMetrics
)(implicit val messagesApi: MessagesApi, ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    Future.successful(Ok(cancel_declaration(appConfig, CancelDeclaration.form)))
  }

  def onSubmit(): Action[AnyContent] = authenticate.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[CancelDeclaration]) =>
          Future.successful(BadRequest(cancel_declaration(appConfig, formWithErrors))),
        form => {
          val metadata = form.createCancellationMetadata(request.user.eori)
          exportsMetrics.startTimer(cancelMetric)

          val mrn = form.declarationId

          customsDeclareExportsConnector.submitCancellation(mrn, metadata).flatMap {
            case status: CancellationStatus =>
              status match {
                case CancellationRequested =>
                  Future.successful(Ok(cancellation_confirmation_page(appConfig)))
                case CancellationRequestExists =>
                  Future.successful(
                    BadRequest(
                      errorHandler.standardErrorTemplate(
                        pageTitle = messagesApi("cancellation.error.title"),
                        heading = messagesApi("cancellation.exists.error.heading"),
                        message = messagesApi("cancellation.exists.error.message")
                      )
                    )
                  )
                case MissingDeclaration =>
                  Future.successful(
                    BadRequest(
                      errorHandler.standardErrorTemplate(
                        pageTitle = messagesApi("cancellation.error.title"),
                        heading = messagesApi("cancellation.error.heading"),
                        message = messagesApi("cancellation.error.message")
                      )
                    )
                  )
              }
            case _ => errorHandler.displayErrorPage()
          }
        }
      )
  }
}
