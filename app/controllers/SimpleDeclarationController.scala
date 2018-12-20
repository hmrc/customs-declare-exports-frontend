/*
 * Copyright 2018 HM Revenue & Customs
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
import forms.{GoodsPackage, SimpleAddress, SimpleDeclarationForm}
import handlers.ErrorHandler
import javax.inject.Inject
import metrics.ExportsMetrics
import metrics.MetricIdentifiers._
import models.{CustomsDeclarationsResponse, Submission}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{boolean, mapping, nonEmptyText, optional, text}
import play.api.data.validation.Constraints.pattern
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.{CustomsCacheService, NRSService}
import services.Countries.allCountries
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.{Declaration, GoodsShipment, MetaData, Ucr}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue
import views.html.{confirmation_page, simpleDeclaration}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class SimpleDeclarationController @Inject()(
  appConfig: AppConfig,
  authenticate: AuthAction,
  customsDeclarationsConnector: CustomsDeclarationsConnector,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler,
  exportsMetrics: ExportsMetrics,
  nrsService: NRSService
)(implicit val messagesApi: MessagesApi) extends FrontendController with I18nSupport {

  val formId = "SimpleDeclarationForm"

  implicit val formats = Json.format[SimpleDeclarationForm]

  val correctDucrFormat = "^\\d[A-Z]{2}\\d{12}-[0-9A-Z]{1,19}$"

  val form = Form(mapping(
    "countryCode" -> optional(text()),
    "ducr" -> nonEmptyText.verifying(pattern(correctDucrFormat.r, error = "error.ducr")),
    "isConsolidateDucrToWiderShipment" -> boolean,
    "mucr" -> mandatoryIfTrue("isConsolidateDucrToWiderShipment",
      nonEmptyText.verifying(pattern("""^[A-Za-z0-9 \-,.&'\/]{1,65}$""".r, error = "error.ducr"))),
    "isDeclarationForSomeoneElse" -> boolean,
    "isAddressAndEORICorrect" -> boolean,
    "haveRepresentative" -> boolean,
    "isConsignorAddressAndEORICorrect" -> boolean,
    "address" -> SimpleAddress.addressMapping,
    "isFinalDestination" -> boolean,
    "goodsPackage" -> GoodsPackage.packageMapping,
    "doYouKnowCustomsProcedureCode" -> boolean,
    "customsProcedure" -> text,
    "wasPreviousCustomsProcedure" -> boolean,
    "additionalCustomsProcedure" -> text,
    "doYouWantAddAdditionalInformation" -> boolean,
    "addAnotherItem" -> boolean,
    "officeOfExit" -> text,
    "knowConsignmentDispatchCountry" -> boolean
  )(SimpleDeclarationForm.apply)(SimpleDeclarationForm.unapply))

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[SimpleDeclarationForm](appConfig.appName, formId).map {
      case Some(data) => Ok(simpleDeclaration(appConfig, form.fill(data), allCountries))
      case _ => Ok(simpleDeclaration(appConfig, form, allCountries))
    }
  }

  def onSubmit(): Action[AnyContent] = authenticate.async { implicit request =>
    form.bindFromRequest().fold(
      (formWithErrors: Form[SimpleDeclarationForm]) =>
        Future.successful(BadRequest(simpleDeclaration(appConfig, formWithErrors, allCountries))),
      form => {
        customsCacheService.cache[SimpleDeclarationForm](appConfig.appName, formId, form).flatMap { _ =>
          exportsMetrics.startTimer(submissionMetric)
          customsDeclarationsConnector.submitExportDeclaration(createMetadataDeclaration(form)).flatMap {
            case CustomsDeclarationsResponse(ACCEPTED, Some(conversationId)) =>
              val submission = new Submission(request.user.eori, conversationId)
              implicit val signedInUser = request.user
              nrsService.submit(conversationId, form.toString, form.ducr).onComplete {
                case Success(nrsResponse) => Logger.warn("NrsRequest Success and submissionId  => " + nrsResponse)
                case Failure(ex) => Logger.error("Error submitting NRS request with the error => " + ex.getMessage)
              }
              customsDeclareExportsConnector.saveSubmissionResponse(submission).flatMap { _ =>
                exportsMetrics.incrementCounter(submissionMetric)
                Future.successful(Ok(confirmation_page(appConfig, conversationId)))
              }.recover {
                case error: Throwable =>
                  exportsMetrics.incrementCounter(submissionMetric)
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

  private def createMetadataDeclaration(form: SimpleDeclarationForm): MetaData =
    MetaData(declaration = Some(
      Declaration(goodsShipment = Some(GoodsShipment(ucr = Some(Ucr(traderAssignedReferenceId = Some("1234"))))))
    ))
}
