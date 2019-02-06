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

package controllers.supplementary

import config.AppConfig
import controllers.actions.AuthAction
import forms.supplementary.ProcedureCode.form
import forms.supplementary.ProcedureCodesData.formId
import forms.supplementary.{ProcedureCode, ProcedureCodesData}
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.CustomsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.procedure_codes

import scala.concurrent.{ExecutionContext, Future}

class ProcedureCodesPageController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  private val cacheId = appConfig.appName

  def displayPage(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[ProcedureCodesData](cacheId, formId).map {
      case Some(data) =>
        Ok(procedure_codes(appConfig, form.fill(data.toProcedureCode()), data.additionalProcedureCodes))
      case _ => Ok(procedure_codes(appConfig, form, Seq()))
    }
  }

  def submitProcedureCodes(): Action[AnyContent] = authenticate.async { implicit request =>
    val boundForm = form.bindFromRequest()

    val actionType =
      request.body.asFormUrlEncoded.flatMap(_.get("action")).flatMap(_.headOption).getOrElse("Wrong action")

    val cachedData =
      customsCacheService
        .fetchAndGetEntry[ProcedureCodesData](cacheId, formId)
        .map(_.getOrElse(ProcedureCodesData(None, Seq())))

    cachedData.flatMap { cache =>
      boundForm
        .fold(
          (formWithErrors: Form[ProcedureCode]) =>
            Future.successful(BadRequest(procedure_codes(appConfig, formWithErrors, cache.additionalProcedureCodes))),
          validForm => {
            actionType match {
              case "Add"                             => addAnotherCodeHandler(validForm, cache)
              case "Save and continue"               => saveAndContinueHandler(validForm, cache)
              case value if value.contains("Remove") => removeCodeHandler(retrieveProcedureCode(value), cache)
              case _                                 => displayErrorPage()
            }
          }
        )
    }
  }

  private def addAnotherCodeHandler(
    userInput: ProcedureCode,
    cachedData: ProcedureCodesData
  )(implicit request: Request[_], hc: HeaderCarrier): Future[Result] =
    (userInput.additionalProcedureCode, cachedData.additionalProcedureCodes) match {
      case (_, codes) if codes.length >= 99 =>
        handleErrorPage(
          Seq(("", "supplementary.procedureCodes.additionalProcedureCode.maximumAmount.error")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (None, _) =>
        handleErrorPage(
          Seq(("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.empty")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (Some(code), seq) if seq.contains(code) =>
        handleErrorPage(
          Seq(("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.duplication")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (Some(code), seq) =>
        val updatedCache = ProcedureCodesData(userInput.procedureCode, seq :+ code)

        customsCacheService.cache[ProcedureCodesData](cacheId, formId, updatedCache).map { _ =>
          Redirect(controllers.supplementary.routes.ProcedureCodesPageController.displayPage())
        }
    }

  private def removeCodeHandler(
    code: String,
    cachedData: ProcedureCodesData
  )(implicit request: Request[_], hc: HeaderCarrier): Future[Result] =
    if (cachedData.containsAdditionalCode(code)) {
      val updatedCache =
        cachedData.copy(additionalProcedureCodes = cachedData.additionalProcedureCodes.filterNot(_ == code))

      customsCacheService.cache[ProcedureCodesData](cacheId, formId, updatedCache).map { _ =>
        Redirect(controllers.supplementary.routes.ProcedureCodesPageController.displayPage())
      }
    } else displayErrorPage()

  //scalastyle:off method.length
  private def saveAndContinueHandler(
    userInput: ProcedureCode,
    cachedData: ProcedureCodesData
  )(implicit request: Request[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cachedData.additionalProcedureCodes) match {
      case (procedureCode, Seq()) =>
        procedureCode match {
          case ProcedureCode(Some(procedureCode), Some(additionalCode)) =>
            val procedureCodes = ProcedureCodesData(Some(procedureCode), Seq(additionalCode))

            customsCacheService.cache[ProcedureCodesData](cacheId, formId, procedureCodes).map { _ =>
              Redirect(controllers.supplementary.routes.SupervisingCustomsOfficeController.displayForm())
            }

          case ProcedureCode(procedureCode, additionalCode) =>
            val procedureCodeError = procedureCode.fold(
              Seq(("procedureCode", "supplementary.procedureCodes.procedureCode.error.empty"))
            )(_ => Seq[(String, String)]())

            val additionalCodeError = additionalCode.fold(
              Seq(("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.mandatory.error"))
            )(_ => Seq[(String, String)]())

            handleErrorPage(procedureCodeError ++ additionalCodeError, userInput, cachedData.additionalProcedureCodes)
        }

      case (procedureCode, seq) =>
        procedureCode match {
          case ProcedureCode(None, _) if !cachedData.procedureCode.isDefined =>
            handleErrorPage(
              Seq(("procedureCode", "supplementary.procedureCodes.procedureCode.error.empty")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCode(_, Some(_)) if seq.length >= 99 =>
            handleErrorPage(
              Seq(("", "supplementary.procedureCodes.additionalProcedureCode.maximumAmount.error")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCode(_, Some(value)) if seq.contains(value) =>
            handleErrorPage(
              Seq(("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.duplication")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCode(Some(procedureCode), additionalCode) =>
            val updatedCache = ProcedureCodesData(
              Some(procedureCode),
              cachedData.additionalProcedureCodes ++ additionalCode.fold(Seq[String]())(Seq(_))
            )

            customsCacheService.cache[ProcedureCodesData](cacheId, formId, updatedCache).map { _ =>
              Redirect(controllers.supplementary.routes.SupervisingCustomsOfficeController.displayForm())
            }
        }
    }
  //scalastyle:on methodLength

  private def retrieveProcedureCode(action: String): String = action.dropWhile(_ != ':').drop(1)

  private def displayErrorPage()(implicit request: Request[_]): Future[Result] =
    Future.successful(
      BadRequest(
        errorHandler.standardErrorTemplate(
          pageTitle = messagesApi("global.error.title"),
          heading = messagesApi("global.error.heading"),
          message = messagesApi("global.error.message")
        )
      )
    )

  private def handleErrorPage(
    fieldWithError: Seq[(String, String)],
    userInput: ProcedureCode,
    additionalProcedureCodes: Seq[String]
  )(implicit request: Request[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form.fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(procedure_codes(appConfig, formWithError, additionalProcedureCodes)))
  }
}
