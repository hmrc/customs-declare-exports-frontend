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
import controllers.util.CacheIdGenerator.goodsItemCacheId
import controllers.util._
import forms.supplementary.ProcedureCodes
import forms.supplementary.ProcedureCodes.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.supplementary.ProcedureCodesData
import models.declaration.supplementary.ProcedureCodesData._
import models.requests.AuthenticatedRequest
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

  def displayPage(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[ProcedureCodesData](goodsItemCacheId, formId).map {
      case Some(data) =>
        Ok(procedure_codes(appConfig, form.fill(data.toProcedureCode()), data.additionalProcedureCodes))
      case _ => Ok(procedure_codes(appConfig, form, Seq()))
    }
  }

  def submitProcedureCodes(): Action[AnyContent] = authenticate.async { implicit request =>
    val boundForm = form.bindFromRequest()

    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))

    val cachedData = customsCacheService
      .fetchAndGetEntry[ProcedureCodesData](goodsItemCacheId, formId)
      .map(_.getOrElse(ProcedureCodesData(None, Seq())))

    cachedData.flatMap { cache =>
      boundForm
        .fold(
          (formWithErrors: Form[ProcedureCodes]) =>
            Future.successful(BadRequest(procedure_codes(appConfig, formWithErrors, cache.additionalProcedureCodes))),
          validForm => {
            actionTypeOpt match {
              case Some(Add)             => addAnotherCodeHandler(validForm, cache)
              case Some(SaveAndContinue) => saveAndContinueHandler(validForm, cache)
              case Some(Remove(values))  => removeCodeHandler(retrieveProcedureCode(values), cache)
              case _                     => errorHandler.displayErrorPage()
            }
          }
        )
    }
  }

  private def addAnotherCodeHandler(
    userInput: ProcedureCodes,
    cachedData: ProcedureCodesData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput.additionalProcedureCode, cachedData.additionalProcedureCodes) match {
      case (_, codes) if codes.length >= limitOfCodes =>
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

        customsCacheService.cache[ProcedureCodesData](goodsItemCacheId, formId, updatedCache).map { _ =>
          Redirect(controllers.supplementary.routes.ProcedureCodesPageController.displayPage())
        }
    }

  private def removeCodeHandler(
    code: String,
    cachedData: ProcedureCodesData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    if (cachedData.containsAdditionalCode(code)) {
      val updatedCache =
        cachedData.copy(additionalProcedureCodes = cachedData.additionalProcedureCodes.filterNot(_ == code))

      customsCacheService.cache[ProcedureCodesData](goodsItemCacheId, formId, updatedCache).map { _ =>
        Redirect(controllers.supplementary.routes.ProcedureCodesPageController.displayPage())
      }
    } else errorHandler.displayErrorPage()

  //scalastyle:off method.length
  private def saveAndContinueHandler(
    userInput: ProcedureCodes,
    cachedData: ProcedureCodesData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cachedData.additionalProcedureCodes) match {
      case (procedureCode, Seq()) =>
        procedureCode match {
          case ProcedureCodes(Some(procedureCode), Some(additionalCode)) =>
            val procedureCodes = ProcedureCodesData(Some(procedureCode), Seq(additionalCode))

            customsCacheService.cache[ProcedureCodesData](goodsItemCacheId, formId, procedureCodes).map { _ =>
              Redirect(controllers.supplementary.routes.ItemTypePageController.displayPage())
            }

          case ProcedureCodes(procedureCode, additionalCode) =>
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
          case ProcedureCodes(None, _) if cachedData.procedureCode.isEmpty =>
            handleErrorPage(
              Seq(("procedureCode", "supplementary.procedureCodes.procedureCode.error.empty")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCodes(_, Some(_)) if seq.length >= limitOfCodes =>
            handleErrorPage(
              Seq(("", "supplementary.procedureCodes.additionalProcedureCode.maximumAmount.error")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCodes(_, Some(value)) if seq.contains(value) =>
            handleErrorPage(
              Seq(("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.duplication")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCodes(Some(procedureCode), additionalCode) =>
            val updatedCache = ProcedureCodesData(
              Some(procedureCode),
              cachedData.additionalProcedureCodes ++ additionalCode.fold(Seq[String]())(Seq(_))
            )

            customsCacheService.cache[ProcedureCodesData](goodsItemCacheId, formId, updatedCache).map { _ =>
              Redirect(controllers.supplementary.routes.ItemTypePageController.displayPage())
            }
        }
    }
  //scalastyle:on method.length

  private def retrieveProcedureCode(values: Seq[String]): String = values.headOption.getOrElse("")

  private def handleErrorPage(
    fieldWithError: Seq[(String, String)],
    userInput: ProcedureCodes,
    additionalProcedureCodes: Seq[String]
  )(implicit request: Request[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form.fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(procedure_codes(appConfig, formWithError, additionalProcedureCodes)))
  }
}
