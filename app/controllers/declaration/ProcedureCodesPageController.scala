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

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.goodsItemCacheId
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
import services.CustomsCacheService
import services.cache.{ExportItem, ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.procedure_codes

import scala.concurrent.{ExecutionContext, Future}

class ProcedureCodesPageController @Inject()(
  appConfig: AppConfig,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  procedureCodesPage: procedure_codes
)(implicit ec: ExecutionContext)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    exportsCacheService.getItemByIdAndSession(itemId, journeySessionId).map {
      case Some(exportItem) =>
        exportItem.procedureCodes.fold({ Ok(procedureCodesPage(appConfig, itemId, form(), Seq())) }) {
          procedureCodesData =>
            Ok(
              procedureCodesPage(
                appConfig,
                itemId,
                form().fill(procedureCodesData.toProcedureCode()),
                procedureCodesData.additionalProcedureCodes
              )
            )

        }
      case None => Ok(procedureCodesPage(appConfig, itemId, form(), Seq()))
    }
  }

  def submitProcedureCodes(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      val boundForm = form().bindFromRequest()

      val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded)

      val cachedData = customsCacheService
        .fetchAndGetEntry[ProcedureCodesData](goodsItemCacheId, formId)
        .map(_.getOrElse(ProcedureCodesData(None, Seq())))

      cachedData.flatMap { cache =>
        boundForm
          .fold(
            (formWithErrors: Form[ProcedureCodes]) =>
              Future.successful(
                BadRequest(procedureCodesPage(appConfig, itemId, formWithErrors, cache.additionalProcedureCodes))
            ),
            validForm => {
              actionTypeOpt match {
                case Some(Add)             => addAnotherCodeHandler(itemId, validForm, cache)
                case Some(SaveAndContinue) => saveAndContinueHandler(itemId, validForm, cache)
                case Some(Remove(values))  => removeCodeHandler(itemId, retrieveProcedureCode(values), cache)
                case _                     => errorHandler.displayErrorPage()
              }
            }
          )
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

      case (Some(code), seq) =>
        for {
          _ <- updateCache(itemId, journeySessionId, ProcedureCodesData(userInput.procedureCode, seq :+ code))
          _ <- customsCacheService.cache[ProcedureCodesData](
            goodsItemCacheId,
            formId,
            ProcedureCodesData(userInput.procedureCode, seq :+ code)
          )
        } yield Redirect(routes.ProcedureCodesPageController.displayPage(itemId))

    }

  private def removeCodeHandler(itemId: String, code: String, cachedData: ProcedureCodesData)(
    implicit request: JourneyRequest[_],
    hc: HeaderCarrier
  ): Future[Result] =
    if (cachedData.containsAdditionalCode(code)) {
      val updatedCache =
        cachedData.copy(additionalProcedureCodes = cachedData.additionalProcedureCodes.filterNot(_ == code))
      for {
        _ <- updateCache(itemId, journeySessionId, updatedCache)
        _ <- customsCacheService.cache[ProcedureCodesData](goodsItemCacheId, formId, updatedCache)
      } yield Redirect(routes.ProcedureCodesPageController.displayPage(itemId))
    } else errorHandler.displayErrorPage()

  //scalastyle:off method.length
  private def saveAndContinueHandler(itemId: String, userInput: ProcedureCodes, cachedData: ProcedureCodesData)(
    implicit request: JourneyRequest[_],
    hc: HeaderCarrier
  ): Future[Result] =
    (userInput, cachedData.additionalProcedureCodes) match {
      case (procedureCode, Seq()) =>
        procedureCode match {
          case ProcedureCodes(Some(procedureCode), Some(additionalCode)) => {
            for {
              _ <- updateCache(itemId, journeySessionId, ProcedureCodesData(Some(procedureCode), Seq(additionalCode)))
              _ <- customsCacheService.cache[ProcedureCodesData](
                goodsItemCacheId,
                formId,
                ProcedureCodesData(Some(procedureCode), Seq(additionalCode))
              )
            } yield Redirect(routes.FiscalInformationController.displayPage(itemId))

          }
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
          case ProcedureCodes(None, _) if cachedData.procedureCode.isEmpty =>
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

          case ProcedureCodes(Some(procedureCode), additionalCode) => {
            val updatedCache = ProcedureCodesData(
              Some(procedureCode),
              cachedData.additionalProcedureCodes ++ additionalCode.fold(Seq[String]())(Seq(_))
            )

            for {
              _ <- updateCache(itemId, journeySessionId, updatedCache)
              _ <- customsCacheService.cache[ProcedureCodesData](goodsItemCacheId, formId, updatedCache)
            } yield Redirect(routes.FiscalInformationController.displayPage(itemId))
          }
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

    Future.successful(BadRequest(procedureCodesPage(appConfig, itemId, formWithError, additionalProcedureCodes)))
  }
}
