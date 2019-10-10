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
import controllers.navigation.Navigator
import controllers.util.MultipleItemsHelper.remove
import controllers.util._
import forms.declaration.ProcedureCodes
import forms.declaration.ProcedureCodes.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.{ExportsDeclaration, Mode}
import models.declaration.ProcedureCodesData
import models.declaration.ProcedureCodesData._
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.{ExportItem, ExportsCacheService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.procedure_codes

import scala.concurrent.{ExecutionContext, Future}

class ProcedureCodesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  procedureCodesPage: procedure_codes
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId) match {
      case Some(exportItem) =>
        exportItem.procedureCodes.fold(Ok(procedureCodesPage(mode, itemId, form(), Seq()))) { procedureCodesData =>
          Ok(procedureCodesPage(mode, itemId, form().fill(procedureCodesData.toProcedureCode()), procedureCodesData.additionalProcedureCodes))

        }
      case None => Ok(procedureCodesPage(mode, itemId, form(), Seq()))
    }
  }

  def submitProcedureCodes(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    val actionTypeOpt = FormAction.bindFromRequest()

    val cache = request.cacheModel.itemBy(itemId).flatMap(_.procedureCodes).getOrElse(ProcedureCodesData(None, Seq()))
    actionTypeOpt match {
      case Add if !boundForm.hasErrors => addAnotherCodeHandler(mode, itemId, boundForm.get, cache)
      case SaveAndContinue | SaveAndReturn if !boundForm.hasErrors =>
        saveAndContinueHandler(mode, itemId, boundForm.get, cache)
      case Remove(values) => removeCodeHandler(mode, itemId, retrieveProcedureCode(values), boundForm, cache)
      case _ =>
        Future.successful(BadRequest(procedureCodesPage(mode, itemId, boundForm, cache.additionalProcedureCodes)))
    }
  }

  private def addAnotherCodeHandler(mode: Mode, itemId: String, userInput: ProcedureCodes, cachedData: ProcedureCodesData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    (userInput.additionalProcedureCode, cachedData.additionalProcedureCodes) match {
      case (_, codes) if codes.length >= limitOfCodes =>
        handleErrorPage(
          mode,
          itemId,
          Seq(("", "supplementary.procedureCodes.additionalProcedureCode.maximumAmount.error")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (None, _) =>
        handleErrorPage(
          mode,
          itemId,
          Seq(("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.empty")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (Some(code), seq) if seq.contains(code) =>
        handleErrorPage(
          mode,
          itemId,
          Seq(("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.duplication")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (Some(code), seq) =>
        updateCache(itemId, ProcedureCodesData(userInput.procedureCode, seq :+ code))
          .map(_ => navigator.continueTo(routes.ProcedureCodesController.displayPage(mode, itemId)))

    }

  private def removeCodeHandler(mode: Mode, itemId: String, code: String, userInput: Form[ProcedureCodes], cachedData: ProcedureCodesData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] = {
    val updatedCache =
      cachedData.copy(additionalProcedureCodes = remove(cachedData.additionalProcedureCodes, (_: String) == code))
    updateCache(itemId, updatedCache)
      .map(_ => Ok(procedureCodesPage(mode, itemId, userInput.discardingErrors, updatedCache.additionalProcedureCodes)))
  }

  private def updateCache(itemId: String, updatedProcedureCodes: ProcedureCodesData)(
    implicit r: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val item: Option[ExportItem] = model.items
        .find(item => item.id.equals(itemId))
        .map(_.copy(procedureCodes = Some(updatedProcedureCodes)))
      val itemList = item.fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
      model.copy(items = itemList)
    })

  //scalastyle:off method.length
  private def saveAndContinueHandler(mode: Mode, itemId: String, userInput: ProcedureCodes, cachedData: ProcedureCodesData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    (userInput, cachedData.additionalProcedureCodes) match {
      case (procedureCode, Seq()) =>
        procedureCode match {
          case ProcedureCodes(Some(procedureCode), Some(additionalCode)) =>
            updateCache(itemId, ProcedureCodesData(Some(procedureCode), Seq(additionalCode)))
              .map(_ => navigator.continueTo(routes.FiscalInformationController.displayPage(mode, itemId)))
          case ProcedureCodes(procedureCode, additionalCode) =>
            val procedureCodeError =
              procedureCode.fold(Seq(("procedureCode", "supplementary.procedureCodes.procedureCode.error.empty")))(_ => Seq[(String, String)]())

            val additionalCodeError = additionalCode.fold(
              Seq(("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.mandatory.error"))
            )(_ => Seq[(String, String)]())

            handleErrorPage(mode, itemId, procedureCodeError ++ additionalCodeError, userInput, cachedData.additionalProcedureCodes)
        }

      case (procedureCode, seq) =>
        procedureCode match {
          case ProcedureCodes(None, _) =>
            handleErrorPage(
              mode,
              itemId,
              Seq(("procedureCode", "supplementary.procedureCodes.procedureCode.error.empty")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCodes(_, Some(_)) if seq.length >= limitOfCodes =>
            handleErrorPage(
              mode,
              itemId,
              Seq(("", "supplementary.procedureCodes.additionalProcedureCode.maximumAmount.error")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCodes(_, Some(value)) if seq.contains(value) =>
            handleErrorPage(
              mode,
              itemId,
              Seq(("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.duplication")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCodes(Some(procedureCode), additionalCode) =>
            val updatedCache =
              ProcedureCodesData(Some(procedureCode), cachedData.additionalProcedureCodes ++ additionalCode.fold(Seq[String]())(Seq(_)))

            updateCache(itemId, updatedCache)
              .map(_ => navigator.continueTo(routes.FiscalInformationController.displayPage(mode, itemId)))
        }
    }

  //scalastyle:on method.length

  private def handleErrorPage(
    mode: Mode,
    itemId: String,
    fieldWithError: Seq[(String, String)],
    userInput: ProcedureCodes,
    additionalProcedureCodes: Seq[String]
  )(implicit request: Request[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form().fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(procedureCodesPage(mode, itemId, formWithError, additionalProcedureCodes)))
  }

  private def retrieveProcedureCode(values: Seq[String]): String = values.headOption.getOrElse("")
}
