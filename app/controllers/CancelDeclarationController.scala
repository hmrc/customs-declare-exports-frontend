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
import connectors.{CustomsDeclarationsConnector, CustomsDeclareExportsConnector}
import controllers.actions.AuthAction
import forms._
import handlers.ErrorHandler
import javax.inject.Inject
import metrics.ExportsMetrics
import metrics.MetricIdentifiers._
import models.{CustomsDeclarationsResponse, Submission}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.{Declaration, MetaData, NamedEntityWithAddress}
import views.html.{cancel_declaration, confirmation_page}

import scala.concurrent.{ExecutionContext, Future}

class CancelDeclarationController @Inject()(
  appConfig: AppConfig,
  authenticate: AuthAction,
  customsDeclarationsConnector: CustomsDeclarationsConnector,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler,
  exportsMetrics: ExportsMetrics
)(implicit val messagesApi: MessagesApi, ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  val formId = "cancelDeclarationForm"
  val CANCEL_FUNCTION_CODE = 13

  implicit val formats = Json.format[CancelDeclarationForm]

  val form = Form(
    mapping(
      "wcoDataModelVersionCode" -> optional(text()),
      "wcoTypeName" -> optional(text()),
      "responsibleAgencyName" -> optional(text()),
      "functionalReferenceID" -> optional(text()),
      "id" -> nonEmptyText,
      "submitter" -> Submitter.formMapping,
      "additionalInformation" -> AdditionalInformation.formMapping,
      "amendment" -> Amendment.formMapping
    )(CancelDeclarationForm.apply)(CancelDeclarationForm.unapply)
  )

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[CancelDeclarationForm](appConfig.appName, formId).map {
      case Some(data) => Ok(cancel_declaration(appConfig, form.fill(data)))
      case _          => Ok(cancel_declaration(appConfig, form))
    }
  }

  def onSubmit(): Action[AnyContent] = authenticate.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[CancelDeclarationForm]) =>
          Future.successful(BadRequest(cancel_declaration(appConfig, formWithErrors))),
        form => {
          customsCacheService.cache[CancelDeclarationForm](appConfig.appName, formId, form).flatMap {
            _ =>
              exportsMetrics.startTimer(cancelMetric)
              customsDeclarationsConnector.submitCancellation(createCancellationMetadata(form)).flatMap {
                case CustomsDeclarationsResponse(ACCEPTED, Some(conversationId)) =>
                  val submission = new Submission(request.user.eori, form.id, conversationId)
                  //TODO ^^ Submission has ducr number, this form doesn't have it, should we use different model for cancellation?
                  customsDeclareExportsConnector
                    .saveSubmissionResponse(submission)
                    .flatMap { _ =>
                      exportsMetrics.incrementCounter(cancelMetric)
                      Future.successful(Ok(confirmation_page(appConfig, conversationId)))
                    }
                    .recover {
                      case error: Throwable =>
                        exportsMetrics.incrementCounter(cancelMetric)
                        Logger.error(s"Error from Customs Declare Exports ${error.toString}")
                        BadRequest(
                          errorHandler.standardErrorTemplate(
                            pageTitle = messagesApi("global.error.title"),
                            heading = messagesApi("global.error.heading"),
                            message = messagesApi("global.error.message")
                          )
                        )
                    }
                case error =>
                  exportsMetrics.incrementCounter(cancelMetric)
                  Logger.error(s"Error from Customs declarations api ${error.toString}")
                  Future.successful(
                    BadRequest(
                      errorHandler.standardErrorTemplate(
                        pageTitle = messagesApi("global.error.title"),
                        heading = messagesApi("global.error.heading"),
                        message = messagesApi("global.error.message")
                      )
                    )
                  )
              }
          }
        }
      )
  }

  private def createCancellationMetadata(form: CancelDeclarationForm): MetaData =
    MetaData(
      wcoDataModelVersionCode = form.wcoDataModelVersionCode,
      wcoTypeName = form.wcoTypeName,
      responsibleAgencyName = form.responsibleAgencyName,
      declaration = Some(
        Declaration(
          functionCode = Some(CANCEL_FUNCTION_CODE),
          functionalReferenceId = form.functionalReferenceID,
          id = Some(CANCEL_FUNCTION_CODE.toString),
          typeCode = Some("INV"),
          submitter = Some(NamedEntityWithAddress(id = Some(form.submitter.id))),
          additionalInformations = Seq(
            uk.gov.hmrc.wco.dec.AdditionalInformation(
              form.additionalInformation.statementCode,
              Some(form.additionalInformation.statementDescription),
              statementTypeCode = Some(form.additionalInformation.statementTypeCode),
              pointers = Seq(uk.gov.hmrc.wco.dec.Pointer(Some(form.additionalInformation.pointer.sequenceNumeric)))
            )
          ),
          amendments = Seq(uk.gov.hmrc.wco.dec.Amendment(Some(form.amendment.changeReasonCode)))
        )
      )
    )
}
