/*
 * Copyright 2020 HM Revenue & Customs
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
import javax.inject.Inject
import models.declaration.ProcedureCodesData
import models.declaration.ProcedureCodesData._
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.procedure_codes

import scala.concurrent.{ExecutionContext, Future}

class ProcedureCodesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  procedureCodesPage: procedure_codes
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form().withSubmissionErrors()
    request.cacheModel.itemBy(itemId) match {
      case Some(exportItem) =>
        exportItem.procedureCodes.fold(Ok(procedureCodesPage(mode, itemId, frm, Seq()))) { procedureCodesData =>
          Ok(procedureCodesPage(mode, itemId, frm.fill(procedureCodesData.toProcedureCode()), procedureCodesData.additionalProcedureCodes))

        }
      case None => Ok(procedureCodesPage(mode, itemId, frm, Seq()))
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
          Seq(("", "declaration.procedureCodes.additionalProcedureCode.maximumAmount.error")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (None, _) =>
        handleErrorPage(
          mode,
          itemId,
          Seq(("additionalProcedureCode", "declaration.procedureCodes.additionalProcedureCode.empty")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (Some(code), seq) if seq.contains(code) =>
        handleErrorPage(
          mode,
          itemId,
          Seq(("additionalProcedureCode", "declaration.procedureCodes.additionalProcedureCode.duplication")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (Some(code), seq) =>
        updateCache(itemId, ProcedureCodesData(userInput.procedureCode, seq :+ code))
          .map(_ => navigator.continueTo(mode, routes.ProcedureCodesController.displayPage(_, itemId)))

    }

  private def removeCodeHandler(mode: Mode, itemId: String, code: String, userInput: Form[ProcedureCodes], cachedData: ProcedureCodesData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] = {
    val updatedCache =
      cachedData.copy(additionalProcedureCodes = remove(cachedData.additionalProcedureCodes, (_: String) == code))
    updateCache(itemId, updatedCache)
      .map(_ => navigator.continueTo(mode, routes.ProcedureCodesController.displayPage(_, itemId)))
  }

  private def updateCache(itemId: String, updatedProcedureCodes: ProcedureCodesData)(
    implicit r: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] = {

    def clearDataForProcedureCode(code: String, itemId: String, model: ExportsDeclaration) = {

      def removeFiscalInformationForCode(sourceModel: ExportsDeclaration) =
        if (!ProcedureCodesData.osrProcedureCodes.contains(code))
          sourceModel.updatedItem(itemId, item => item.copy(fiscalInformation = None, additionalFiscalReferencesData = None))
        else sourceModel

      def removePackageInformationForCode(sourceModel: ExportsDeclaration) =
        if (r.isType(DeclarationType.CLEARANCE) && ProcedureCodesData.eicrProcedureCodes.contains(code))
          sourceModel.updatedItem(itemId, item => item.copy(packageInformation = None))
        else sourceModel

      def removeWarehouseIdentificationForCode(sourceModel: ExportsDeclaration) =
        if (r.isType(DeclarationType.CLEARANCE) || sourceModel.requiresWarehouseId)
          sourceModel
        else
          sourceModel.copy(locations = sourceModel.locations.copy(warehouseIdentification = None))

      model
        .transform(removeFiscalInformationForCode)
        .transform(removePackageInformationForCode)
        .transform(removeWarehouseIdentificationForCode)
    }

    def updatedModel(model: ExportsDeclaration): ExportsDeclaration = {
      val updatedModel = model.updatedItem(itemId, item => item.copy(procedureCodes = Some(updatedProcedureCodes)))
      updatedProcedureCodes.procedureCode match {
        case Some(code) =>
          clearDataForProcedureCode(code, itemId, updatedModel)
        case _ =>
          updatedModel
      }
    }

    updateExportsDeclarationSyncDirect(updatedModel(_))
  }

  //scalastyle:off method.length
  private def saveAndContinueHandler(mode: Mode, itemId: String, userInput: ProcedureCodes, cachedData: ProcedureCodesData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] = {

    def nextPage =
      (declaration: Option[ExportsDeclaration]) =>
        declaration.flatMap(_.itemBy(itemId)).flatMap(_.procedureCodes).flatMap(_.procedureCode) match {
          case Some(code) if ProcedureCodesData.osrProcedureCodes.contains(code) =>
            navigator.continueTo(mode, routes.FiscalInformationController.displayPage(_, itemId))
          case _ => navigator.continueTo(mode, routes.CommodityDetailsController.displayPage(_, itemId))
      }

    (userInput, cachedData.additionalProcedureCodes) match {
      case (procedureCode, Seq()) =>
        procedureCode match {
          case ProcedureCodes(Some(procedureCode), Some(additionalCode)) =>
            updateCache(itemId, ProcedureCodesData(Some(procedureCode), Seq(additionalCode)))
              .map(nextPage)
          case ProcedureCodes(procedureCode, additionalCode) =>
            val procedureCodeError =
              procedureCode.fold(Seq(("procedureCode", "declaration.procedureCodes.procedureCode.error.empty")))(_ => Seq[(String, String)]())

            val additionalCodeError = additionalCode.fold(
              Seq(("additionalProcedureCode", "declaration.procedureCodes.additionalProcedureCode.mandatory.error"))
            )(_ => Seq[(String, String)]())

            handleErrorPage(mode, itemId, procedureCodeError ++ additionalCodeError, userInput, cachedData.additionalProcedureCodes)
        }

      case (procedureCode, seq) =>
        procedureCode match {
          case ProcedureCodes(None, _) =>
            handleErrorPage(
              mode,
              itemId,
              Seq(("procedureCode", "declaration.procedureCodes.procedureCode.error.empty")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCodes(_, Some(_)) if seq.length >= limitOfCodes =>
            handleErrorPage(
              mode,
              itemId,
              Seq(("", "declaration.procedureCodes.additionalProcedureCode.maximumAmount.error")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCodes(_, Some(value)) if seq.contains(value) =>
            handleErrorPage(
              mode,
              itemId,
              Seq(("additionalProcedureCode", "declaration.procedureCodes.additionalProcedureCode.duplication")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case ProcedureCodes(Some(procedureCode), additionalCode) =>
            val updatedCache =
              ProcedureCodesData(Some(procedureCode), cachedData.additionalProcedureCodes ++ additionalCode.fold(Seq[String]())(Seq(_)))

            updateCache(itemId, updatedCache)
              .map(nextPage)
        }
    }
  }

  //scalastyle:on method.length

  private def handleErrorPage(
    mode: Mode,
    itemId: String,
    fieldWithError: Seq[(String, String)],
    userInput: ProcedureCodes,
    additionalProcedureCodes: Seq[String]
  )(implicit request: JourneyRequest[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form().fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(procedureCodesPage(mode, itemId, formWithError, additionalProcedureCodes)))
  }

  private def retrieveProcedureCode(values: Seq[String]): String = values.headOption.getOrElse("")
}
