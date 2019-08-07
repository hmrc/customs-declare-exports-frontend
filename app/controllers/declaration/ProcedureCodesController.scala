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

package controllers.declaration

import controllers.actions.{AuthAction, JourneyAction}
import controllers.util._
import forms.declaration.ProcedureCodes
import forms.declaration.ProcedureCodes.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.ProcedureCodesData
import models.declaration.ProcedureCodesData._
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.{ExportItem, ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.procedure_codes

import scala.concurrent.{ExecutionContext, Future}

class ProcedureCodesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  procedureCodesPage: procedure_codes
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    exportsCacheService.getItemByIdAndSession(itemId, journeySessionId).map {
      case Some(exportItem) =>
        exportItem.procedureCodes.fold({ Ok(procedureCodesPage(itemId, form(), Seq())) }) { procedureCodesData =>
          Ok(
            procedureCodesPage(
              itemId,
              form().fill(procedureCodesData.toProcedureCode()),
              procedureCodesData.additionalProcedureCodes
            )
          )

        }
      case None => Ok(procedureCodesPage(itemId, form(), Seq()))
    }
  }

  def submitProcedureCodes(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      val boundForm = form().bindFromRequest()

      val actionTypeOpt = FormAction.bindFromRequest()

      val cachedData = exportsCacheService
        .getItemByIdAndSession(itemId, journeySessionId)
        .map(_.flatMap(_.procedureCodes))
        .map(_.getOrElse(ProcedureCodesData(None, Seq())))

      cachedData.flatMap { cache =>
        actionTypeOpt match {
          case Some(Add) if !boundForm.hasErrors             => addAnotherCodeHandler(itemId, boundForm.get, cache)
          case Some(SaveAndContinue) if !boundForm.hasErrors => saveAndContinueHandler(itemId, boundForm.get, cache)
          case Some(Remove(values))                          => removeCodeHandler(itemId, retrieveProcedureCode(values), boundForm, cache)
          case _                                             => Future.successful(BadRequest(procedureCodesPage(itemId, boundForm, cache.additionalProcedureCodes)))
        }
      }
  }

  private def updateCache(
    itemId: String,
    sessionId: String,
    updatedProcedureCodes: ProcedureCodesData
  ): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => {
        val item: Option[ExportItem] = model.items
          .find(item => item.id.equals(itemId))
          .map(_.copy(procedureCodes = Some(updatedProcedureCodes)))
        val itemList = item.fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
        exportsCacheService.update(sessionId, model.copy(items = itemList))
      }
    )

  private def addAnotherCodeHandler(itemId: String, userInput: ProcedureCodes, cachedData: ProcedureCodesData)(
    implicit request: JourneyRequest[_],
    hc: HeaderCarrier
  ): Future[Result] =
    (userInput.additionalProcedureCode, cachedData.additionalProcedureCodes) match {
      case (_, codes) if codes.length >= limitOfCodes =>
        handleErrorPage(
          itemId,
          Seq(("", "supplementary.procedureCodes.additionalProcedureCode.maximumAmount.error")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (None, _) =>
        handleErrorPage(
          itemId,
          Seq(("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.empty")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (Some(code), seq) if seq.contains(code) =>
        handleErrorPage(
          itemId,
          Seq(("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.duplication")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (Some(code), seq) => {
        updateCache(itemId, journeySessionId, ProcedureCodesData(userInput.procedureCode, seq :+ code))
          .map(_ => Redirect(routes.ProcedureCodesController.displayPage(itemId)))
      }

    }

  private def removeCodeHandler(
    itemId: String,
    code: String,
    userInput: Form[ProcedureCodes],
    cachedData: ProcedureCodesData
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Result] =
    if (cachedData.containsAdditionalCode(code)) {
      val updatedCache =
        cachedData.copy(additionalProcedureCodes = cachedData.additionalProcedureCodes.filterNot(_ == code))
      updateCache(itemId, journeySessionId, updatedCache)
        .map(_ => Ok(procedureCodesPage(itemId, userInput.discardingErrors, updatedCache.additionalProcedureCodes)))
    } else errorHandler.displayErrorPage()

  //scalastyle:off method.length
  private def saveAndContinueHandler(itemId: String, userInput: ProcedureCodes, cachedData: ProcedureCodesData)(
    implicit request: JourneyRequest[_],
    hc: HeaderCarrier
  ): Future[Result] =
    (userInput, cachedData.additionalProcedureCodes) match {
      case (procedureCode, Seq()) =>
        procedureCode match {
          case ProcedureCodes(Some(procedureCode), Some(additionalCode)) =>
            updateCache(itemId, journeySessionId, ProcedureCodesData(Some(procedureCode), Seq(additionalCode)))
              .map(_ => Redirect(routes.FiscalInformationController.displayPage(itemId)))
          case ProcedureCodes(procedureCode, additionalCode) =>
            val procedureCodeError = procedureCode.fold(
              Seq(("procedureCode", "supplementary.procedureCodes.procedureCode.error.empty"))
            )(_ => Seq[(String, String)]())

            val additionalCodeError = additionalCode.fold(
              Seq(("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.mandatory.error"))
            )(_ => Seq[(String, String)]())

            handleErrorPage(
              itemId,
              procedureCodeError ++ additionalCodeError,
              userInput,
              cachedData.additionalProcedureCodes
            )
        }

      case (procedureCode, seq) =>
        procedureCode match {
          case ProcedureCodes(None, _) =>
            handleErrorPage(
              itemId,
              Seq(("procedureCode", "supplementary.procedureCodes.procedureCode.error.empty")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCodes(_, Some(_)) if seq.length >= limitOfCodes =>
            handleErrorPage(
              itemId,
              Seq(("", "supplementary.procedureCodes.additionalProcedureCode.maximumAmount.error")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCodes(_, Some(value)) if seq.contains(value) =>
            handleErrorPage(
              itemId,
              Seq(("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.duplication")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCodes(Some(procedureCode), additionalCode) =>
            val updatedCache = ProcedureCodesData(
              Some(procedureCode),
              cachedData.additionalProcedureCodes ++ additionalCode.fold(Seq[String]())(Seq(_))
            )

            updateCache(itemId, journeySessionId, updatedCache)
              .map(_ => Redirect(routes.FiscalInformationController.displayPage(itemId)))
        }
    }

  //scalastyle:on method.length

  private def retrieveProcedureCode(values: Seq[String]): String = values.headOption.getOrElse("")

  private def handleErrorPage(
    itemId: String,
    fieldWithError: Seq[(String, String)],
    userInput: ProcedureCodes,
    additionalProcedureCodes: Seq[String]
  )(implicit request: Request[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form.fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(procedureCodesPage(itemId, formWithError, additionalProcedureCodes)))
  }
}
