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
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{cancel_declaration, cancellation_confirmation_page}

import scala.concurrent.{ExecutionContext, Future}

class CancelDeclarationController @Inject()(
  appConfig: AppConfig,
  authenticate: AuthAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  errorHandler: ErrorHandler,
  exportsMetrics: ExportsMetrics,
  mcc: MessagesControllerComponents,
  cancelDeclarationPage: cancel_declaration,
  cancelConfirmationPage: cancellation_confirmation_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private val logger = Logger(this.getClass())

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    Future.successful(Ok(cancelDeclarationPage(appConfig, CancelDeclaration.form)))
  }

  def onSubmit(): Action[AnyContent] = authenticate.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[CancelDeclaration]) =>
          Future.successful(BadRequest(cancelDeclarationPage(appConfig, formWithErrors))),
        form => {
          val context = exportsMetrics.startTimer(cancelMetric)

          val metadata = form.createCancellationMetadata(request.user.eori)

          val mrn = form.declarationId

          customsDeclareExportsConnector.submitCancellation(mrn, metadata).flatMap {
            case status: CancellationStatus =>
              status match {
                case CancellationRequested =>
                  exportsMetrics.incrementCounter(cancelMetric)
                  context.stop()
                  Future.successful(Ok(cancelConfirmationPage(appConfig)))
                case CancellationRequestExists =>
                  logger.error(s"Cancellation for declaration with mrn $mrn exists")
                  Future.successful(
                    BadRequest(
                      errorHandler.standardErrorTemplate(
                        pageTitle = Messages("cancellation.error.title"),
                        heading = Messages("cancellation.exists.error.heading"),
                        message = Messages("cancellation.exists.error.message")
                      )
                    )
                  )
                case MissingDeclaration =>
                  logger.error(s"Declaration with mrn $mrn doesn't exists")
                  Future.successful(
                    BadRequest(
                      errorHandler.standardErrorTemplate(
                        pageTitle = Messages("cancellation.error.title"),
                        heading = Messages("cancellation.error.heading"),
                        message = Messages("cancellation.error.message")
                      )
                    )
                  )
              }
            case _ =>
              logger.error("Internal server error during cancellation")
              errorHandler.displayErrorPage()
          }
        }
      )
  }
}
