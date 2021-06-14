/*
 * Copyright 2021 HM Revenue & Customs
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

import scala.concurrent.{ExecutionContext, Future}

import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import controllers.util.MultipleItemsHelper.remove
import controllers.util._
import forms.declaration.procedurecodes.AdditionalProcedureCode
import javax.inject.Inject
import forms.declaration.procedurecodes.AdditionalProcedureCode._
import models.{ExportsDeclaration, Mode}
import models.codes.{ProcedureCode, AdditionalProcedureCode => AdditionalProcedureCodeModel}
import models.codes.AdditionalProcedureCode.NO_APC_APPLIES_CODE
import models.declaration.ProcedureCodesData
import models.declaration.ProcedureCodesData.limitOfCodes
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.FormError
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.ProcedureCodeService
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import views.html.declaration.procedureCodes.additional_procedure_codes

class AdditionalProcedureCodesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  procedureCodeService: ProcedureCodeService,
  additionalProcedureCodesPage: additional_procedure_codes
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  private val emptyProcedureCodesData = ProcedureCodesData(None, Seq())

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form().withSubmissionErrors()

    val (maybeCachedProcedureCode, cachedData) = getCachedData(itemId)
    val availableAdditionalProcedureCodes = getAvailableAdditionalProcedureCodes(maybeCachedProcedureCode)

    (maybeCachedProcedureCode, availableAdditionalProcedureCodes) match {
      case (Some(procedureCode), Some(validAdditionalProcedureCodes)) if !validAdditionalProcedureCodes.isEmpty =>
        Ok(additionalProcedureCodesPage(mode, itemId, frm, procedureCode, validAdditionalProcedureCodes, cachedData.additionalProcedureCodes))
      case _ =>
        Redirect(controllers.declaration.routes.ProcedureCodesController.displayPage(mode, itemId))
    }
  }

  def submitAdditionalProcedureCodes(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    val formAction = FormAction.bindFromRequest()

    val (maybeCachedProcedureCode, cachedData) = getCachedData(itemId)
    val availableAdditionalProcedureCodes = getAvailableAdditionalProcedureCodes(maybeCachedProcedureCode)

    def handleActionTypes(procedureCode: ProcedureCode, validAdditionalProcedureCodes: Seq[AdditionalProcedureCodeModel]) =
      formAction match {
        case Add if !boundForm.hasErrors =>
          addAnotherCodeHandler(mode, itemId, boundForm.get, procedureCode, cachedData, validAdditionalProcedureCodes)
        case SaveAndContinue | SaveAndReturn if !boundForm.hasErrors =>
          saveAndContinueHandler(mode, itemId, boundForm.get, procedureCode, cachedData, validAdditionalProcedureCodes)
        case Remove(values) =>
          removeCodeHandler(mode, itemId, retrieveAdditionalProcedureCode(values), cachedData)
        case _ =>
          Future.successful(
            BadRequest(
              additionalProcedureCodesPage(mode, itemId, boundForm, procedureCode, validAdditionalProcedureCodes, cachedData.additionalProcedureCodes)
            )
          )
      }

    (maybeCachedProcedureCode, availableAdditionalProcedureCodes) match {
      case (_, None) | (None, _) =>
        Future.successful(Redirect(controllers.declaration.routes.ProcedureCodesController.displayPage(mode, itemId)))
      case (Some(procedureCode), Some(validAdditionalProcedureCodes)) =>
        handleActionTypes(procedureCode, validAdditionalProcedureCodes)
    }
  }

  private def getCachedData(itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.itemBy(itemId) match {
      case Some(exportItem) =>
        (
          exportItem.procedureCodes.flatMap(procedureCodeData => getProcedureCode(procedureCodeData.procedureCode)),
          exportItem.procedureCodes.getOrElse(emptyProcedureCodesData)
        )
      case None => (None, emptyProcedureCodesData)
    }

  private def getAvailableAdditionalProcedureCodes(maybeCachedProcedureCode: Option[ProcedureCode])(implicit request: JourneyRequest[AnyContent]) =
    maybeCachedProcedureCode
      .map(procedureCode => procedureCodeService.getAdditionalProcedureCodesFor(procedureCode.code, messagesApi.preferred(request).lang.toLocale))

  private def getProcedureCode(maybeCachedProcedureCode: Option[String])(implicit request: JourneyRequest[AnyContent]) =
    maybeCachedProcedureCode
      .flatMap(
        procedureCode =>
          procedureCodeService
            .getProcedureCodeFor(procedureCode, request.declarationType, request.cacheModel.isEidr, messagesApi.preferred(request).lang.toLocale)
      )

  private def addAnotherCodeHandler(
    mode: Mode,
    itemId: String,
    userInput: AdditionalProcedureCode,
    procedureCode: ProcedureCode,
    cachedData: ProcedureCodesData,
    validAdditionalProcedureCodes: Seq[AdditionalProcedureCodeModel]
  )(implicit request: JourneyRequest[AnyContent], hc: HeaderCarrier): Future[Result] =
    (userInput.additionalProcedureCode, cachedData.additionalProcedureCodes) match {
      case (_, cachedAdditionalProcedureCodes) if cachedAdditionalProcedureCodes.length >= limitOfCodes =>
        returnErrorPage(mode, itemId, userInput, procedureCode, cachedData.additionalProcedureCodes, validAdditionalProcedureCodes)(
          Seq(("", "declaration.additionalProcedureCodes.error.maximumAmount"))
        )

      case (None, _) =>
        returnErrorPage(mode, itemId, userInput, procedureCode, cachedData.additionalProcedureCodes, validAdditionalProcedureCodes)(
          Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.empty"))
        )

      case (Some(NO_APC_APPLIES_CODE), cachedAdditionalProcedureCodes) if cachedAdditionalProcedureCodes.length != 0 =>
        returnErrorPage(mode, itemId, userInput, procedureCode, cachedData.additionalProcedureCodes, validAdditionalProcedureCodes)(
          Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.tripleZero.notFirstCode"))
        )

      case (Some(_), cachedAdditionalProcedureCodes) if cachedAdditionalProcedureCodes.contains(NO_APC_APPLIES_CODE) =>
        returnErrorPage(mode, itemId, userInput, procedureCode, cachedData.additionalProcedureCodes, validAdditionalProcedureCodes)(
          Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.tripleZero.alreadyPresent"))
        )

      case (Some(code), cachedAdditionalProcedureCodes) if cachedAdditionalProcedureCodes.contains(code) =>
        returnErrorPage(mode, itemId, userInput, procedureCode, cachedData.additionalProcedureCodes, validAdditionalProcedureCodes)(
          Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.duplication"))
        )

      case (Some(code), cachedAdditionalProcedureCodes) =>
        updateCache(itemId, ProcedureCodesData(None, cachedAdditionalProcedureCodes :+ code))
          .map(_ => navigator.continueTo(mode, routes.AdditionalProcedureCodesController.displayPage(_, itemId)))
    }

  private def saveAndContinueHandler(
    mode: Mode,
    itemId: String,
    userInput: AdditionalProcedureCode,
    procedureCode: ProcedureCode,
    cachedData: ProcedureCodesData,
    validAdditionalProcedureCodes: Seq[AdditionalProcedureCodeModel]
  )(implicit request: JourneyRequest[AnyContent]): Future[Result] = {

    def validateFirstCode(newAdditionalProcedureCode: AdditionalProcedureCode) = newAdditionalProcedureCode match {
      case AdditionalProcedureCode(Some(additionalCode)) =>
        updateCache(itemId, ProcedureCodesData(None, Seq(additionalCode))).map(declaration => nextPage(mode, itemId, declaration))

      case AdditionalProcedureCode(None) =>
        returnErrorPage(mode, itemId, userInput, procedureCode, cachedData.additionalProcedureCodes, validAdditionalProcedureCodes)(
          Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.empty"))
        )
    }

    def validateSubsequentCode(newAdditionalProcedureCode: AdditionalProcedureCode) = newAdditionalProcedureCode match {
      case AdditionalProcedureCode(Some(_)) if cachedData.additionalProcedureCodes.length >= limitOfCodes =>
        returnErrorPage(mode, itemId, userInput, procedureCode, cachedData.additionalProcedureCodes, validAdditionalProcedureCodes)(
          Seq(("", "declaration.additionalProcedureCodes.error.maximumAmount"))
        )

      case AdditionalProcedureCode(Some(additionalCode)) if cachedData.additionalProcedureCodes.contains(additionalCode) =>
        returnErrorPage(mode, itemId, userInput, procedureCode, cachedData.additionalProcedureCodes, validAdditionalProcedureCodes)(
          Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.duplication"))
        )

      case AdditionalProcedureCode(Some(additionalCode)) =>
        val updatedCache = ProcedureCodesData(None, cachedData.additionalProcedureCodes :+ additionalCode)
        updateCache(itemId, updatedCache).map(declaration => nextPage(mode, itemId, declaration))

      case AdditionalProcedureCode(None) =>
        val updatedCache = ProcedureCodesData(None, cachedData.additionalProcedureCodes)
        updateCache(itemId, updatedCache).map(declaration => nextPage(mode, itemId, declaration))
    }

    if (cachedData.additionalProcedureCodes.isEmpty) validateFirstCode(userInput)
    else validateSubsequentCode(userInput)
  }

  private def removeCodeHandler(mode: Mode, itemId: String, code: String, cachedData: ProcedureCodesData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] = {
    val updatedCache = cachedData.copy(additionalProcedureCodes = remove(cachedData.additionalProcedureCodes, (_: String) == code))
    updateCache(itemId, updatedCache).map(_ => navigator.continueTo(mode, routes.AdditionalProcedureCodesController.displayPage(_, itemId)))
  }

  private def updateCache(itemId: String, updatedProcedureCodes: ProcedureCodesData)(
    implicit r: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] = {

    val updatedModel = (model: ExportsDeclaration) =>
      model.updatedItem(
        itemId,
        item => {
          val newProcedureCodes = item.procedureCodes.fold(ProcedureCodesData(None, updatedProcedureCodes.additionalProcedureCodes))(
            _.copy(additionalProcedureCodes = updatedProcedureCodes.additionalProcedureCodes)
          )
          item.copy(procedureCodes = Some(newProcedureCodes))
        }
    )
    updateExportsDeclarationSyncDirect(updatedModel(_))
  }

  private def nextPage(mode: Mode, itemId: String, declaration: Option[ExportsDeclaration])(implicit request: JourneyRequest[AnyContent]) =
    declaration.flatMap(_.itemBy(itemId)).flatMap(_.procedureCodes).flatMap(_.procedureCode) match {
      case Some(code) if ProcedureCodesData.osrProcedureCodes.contains(code) =>
        navigator.continueTo(mode, routes.FiscalInformationController.displayPage(_, itemId))
      case _ => navigator.continueTo(mode, routes.CommodityDetailsController.displayPage(_, itemId))
    }

  private def returnErrorPage(
    mode: Mode,
    itemId: String,
    userInput: AdditionalProcedureCode,
    procedureCode: ProcedureCode,
    cachedAdditionalProcedureCodes: Seq[String],
    validAdditionalProcedureCodes: Seq[AdditionalProcedureCodeModel]
  )(fieldWithError: Seq[(String, String)])(implicit request: JourneyRequest[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)
    val formWithError = form().fill(userInput).copy(errors = updatedErrors)

    Future.successful(
      BadRequest(
        additionalProcedureCodesPage(mode, itemId, formWithError, procedureCode, validAdditionalProcedureCodes, cachedAdditionalProcedureCodes)
      )
    )
  }

  private def retrieveAdditionalProcedureCode(values: Seq[String]): String = values.headOption.getOrElse("")

}
