/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.helpers.MultipleItemsHelper.remove
import controllers.helpers.SupervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified
import controllers.helpers._
import controllers.navigation.{ItemId, Navigator}
import forms.declaration.procedurecodes.AdditionalProcedureCode
import forms.declaration.procedurecodes.AdditionalProcedureCode._
import models.codes.AdditionalProcedureCode.NO_APC_APPLIES_CODE
import models.codes.{ProcedureCode, AdditionalProcedureCode => AdditionalProcedureCodeModel}
import models.declaration.ProcedureCodesData
import models.declaration.ProcedureCodesData.limitOfCodes
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.FormError
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.ProcedureCodeService
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.procedureCodes.additional_procedure_codes

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val (maybeCachedProcedureCode, cachedData) = getCachedData(itemId)
    val availableAdditionalProcedureCodes = getAvailableAdditionalProcedureCodes(maybeCachedProcedureCode)

    (maybeCachedProcedureCode, availableAdditionalProcedureCodes) match {
      case (Some(procedureCode), Some(validAdditionalProcedureCodes)) if !validAdditionalProcedureCodes.isEmpty =>
        val frm = form().withSubmissionErrors()
        resolveBackLink(mode, itemId).map { backLink =>
          Ok(
            additionalProcedureCodesPage(
              mode,
              itemId,
              frm,
              procedureCode,
              validAdditionalProcedureCodes,
              cachedData.additionalProcedureCodes,
              backLink
            )
          )
        }

      case _ =>
        Future.successful(Redirect(routes.ProcedureCodesController.displayPage(mode, itemId)))
    }
  }

  def submitAdditionalProcedureCodes(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    val formAction = FormAction.bindFromRequest()

    val (maybeCachedProcedureCode, cachedData) = getCachedData(itemId)
    val availableAdditionalProcedureCodes = getAvailableAdditionalProcedureCodes(maybeCachedProcedureCode)

    def handleActionTypes(procedureCode: ProcedureCode, validAdditionalProcedureCodes: Seq[AdditionalProcedureCodeModel]): Future[Result] = {
      val errorHandler =
        returnErrorPage(mode, itemId, boundForm.get, procedureCode, cachedData.additionalProcedureCodes, validAdditionalProcedureCodes)(_)

      formAction match {
        case Add | SaveAndContinue | SaveAndReturn if !boundForm.hasErrors =>
          validateForm(formAction, mode, errorHandler, itemId, boundForm.get, cachedData)

        case Remove(values) =>
          removeCodeHandler(mode, itemId, retrieveAdditionalProcedureCode(values), cachedData)

        case _ =>
          resolveBackLink(mode, itemId).map { backLink =>
            BadRequest(
              additionalProcedureCodesPage(
                mode,
                itemId,
                boundForm,
                procedureCode,
                validAdditionalProcedureCodes,
                cachedData.additionalProcedureCodes,
                backLink
              )
            )
          }
      }
    }

    (maybeCachedProcedureCode, availableAdditionalProcedureCodes) match {
      case (_, None) | (None, _) =>
        Future.successful(Redirect(routes.ProcedureCodesController.displayPage(mode, itemId)))

      case (Some(procedureCode), Some(validAdditionalProcedureCodes)) =>
        handleActionTypes(procedureCode, validAdditionalProcedureCodes)
    }
  }

  private def getCachedData(itemId: String)(implicit request: JourneyRequest[AnyContent]): (Option[ProcedureCode], ProcedureCodesData) =
    request.cacheModel.itemBy(itemId) match {
      case Some(exportItem) =>
        (
          exportItem.procedureCodes.flatMap(procedureCodeData => getProcedureCode(procedureCodeData.procedureCode)),
          exportItem.procedureCodes.getOrElse(emptyProcedureCodesData)
        )
      case None => (None, emptyProcedureCodesData)
    }

  private def getAvailableAdditionalProcedureCodes(
    maybeCachedProcedureCode: Option[ProcedureCode]
  )(implicit request: JourneyRequest[AnyContent]): Option[Seq[AdditionalProcedureCodeModel]] =
    maybeCachedProcedureCode
      .map(procedureCode => procedureCodeService.getAdditionalProcedureCodesFor(procedureCode.code, messagesApi.preferred(request).lang.toLocale))

  private def getProcedureCode(maybeCachedProcedureCode: Option[String])(implicit request: JourneyRequest[AnyContent]): Option[ProcedureCode] =
    maybeCachedProcedureCode.flatMap { procedureCode =>
      val isEidr = request.cacheModel.isEntryIntoDeclarantsRecords
      procedureCodeService.getProcedureCodeFor(procedureCode, request.declarationType, isEidr, messagesApi.preferred(request).lang.toLocale)
    }

  private def validateForm(
    action: FormAction,
    mode: Mode,
    errorHandler: Seq[(String, String)] => Future[Result],
    itemId: String,
    userInput: AdditionalProcedureCode,
    cachedData: ProcedureCodesData
  )(implicit request: JourneyRequest[AnyContent]): Future[Result] = {

    val cachedAdditionalProcedureCodes = cachedData.additionalProcedureCodes

    (userInput.additionalProcedureCode, action) match {
      case (Some(_), _) if cachedAdditionalProcedureCodes.length >= limitOfCodes =>
        errorHandler(Seq(("", "declaration.additionalProcedureCodes.error.maximumAmount")))

      case (None, Add) =>
        errorHandler(Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.empty")))

      case (None, _) =>
        if (cachedAdditionalProcedureCodes.nonEmpty)
          nextPage(action, mode, itemId, cachedData.procedureCode, None)
        else
          errorHandler(Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.empty")))

      case (Some(NO_APC_APPLIES_CODE), _) if cachedAdditionalProcedureCodes.length != 0 =>
        errorHandler(Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.tripleZero.notFirstCode")))

      case (Some(_), _) if cachedAdditionalProcedureCodes.contains(NO_APC_APPLIES_CODE) =>
        errorHandler(Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.tripleZero.alreadyPresent")))

      case (Some(code), _) if cachedAdditionalProcedureCodes.contains(code) =>
        errorHandler(Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.duplication")))

      case (Some(code), _) =>
        updateCache(itemId, ProcedureCodesData(None, cachedAdditionalProcedureCodes :+ code))
          .flatMap(nextPage(action, mode, itemId, cachedData.procedureCode, _))
    }
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

  private def nextPage(action: FormAction, mode: Mode, itemId: String, maybeProcedureCode: Option[String], maybeModel: Option[ExportsDeclaration])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    (action, maybeProcedureCode) match {
      case (Add, _) =>
        Future.successful(navigator.continueTo(mode, routes.AdditionalProcedureCodesController.displayPage(_, itemId)))

      case (_, Some(code)) if ProcedureCodesData.osrProcedureCodes.contains(code) =>
        Future.successful(navigator.continueTo(mode, routes.FiscalInformationController.displayPage(_, itemId)))

      case _ =>
        val continueToCommodityDetails = navigator.continueTo(mode, routes.CommodityDetailsController.displayPage(_, itemId))

        val model = maybeModel.fold(request.cacheModel)(identity)
        if (!isConditionForAllProcedureCodesVerified(model)) Future.successful(continueToCommodityDetails)
        else resetSupervisingCustomsOfficeInCache(model).map(_ => continueToCommodityDetails)
    }

  private def resetSupervisingCustomsOfficeInCache(declaration: ExportsDeclaration)(implicit hc: HeaderCarrier): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(declaration.removeSupervisingCustomsOffice)

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

    resolveBackLink(mode, itemId).map { backLink =>
      BadRequest(
        additionalProcedureCodesPage(
          mode,
          itemId,
          formWithError,
          procedureCode,
          validAdditionalProcedureCodes,
          cachedAdditionalProcedureCodes,
          backLink
        )
      )
    }
  }

  private def retrieveAdditionalProcedureCode(values: Seq[String]): String = values.headOption.getOrElse("")

  private def resolveBackLink(mode: Mode, itemId: String)(implicit request: JourneyRequest[_]): Future[Call] =
    navigator.backLink(AdditionalProcedureCode, mode, ItemId(itemId))
}
