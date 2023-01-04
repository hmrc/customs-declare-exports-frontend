/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.helpers._
import controllers.navigation.Navigator
import forms.declaration.procedurecodes.AdditionalProcedureCode
import forms.declaration.procedurecodes.AdditionalProcedureCode._
import models.codes.AdditionalProcedureCode.NO_APC_APPLIES_CODE
import models.codes.{AdditionalProcedureCode => AdditionalProcedureCodeModel, ProcedureCode}
import models.declaration.ProcedureCodesData
import models.declaration.ProcedureCodesData.limitOfCodes
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.data.FormError
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.ProcedureCodeService
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.procedureCodes.additional_procedure_codes

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalProcedureCodesController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  procedureCodeService: ProcedureCodeService,
  additionalProcedureCodesPage: additional_procedure_codes,
  supervisingCustomsOfficeHelper: SupervisingCustomsOfficeHelper
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  private val emptyProcedureCodesData = ProcedureCodesData(None, Seq())

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val (maybeCachedProcedureCode, cachedData) = getCachedData(itemId)
    val availableAdditionalProcedureCodes = getAvailableAdditionalProcedureCodes(maybeCachedProcedureCode)

    (maybeCachedProcedureCode, availableAdditionalProcedureCodes) match {
      case (Some(procedureCode), Some(validAdditionalProcedureCodes)) if validAdditionalProcedureCodes.nonEmpty =>
        val frm = form.withSubmissionErrors
        Ok(additionalProcedureCodesPage(itemId, frm, procedureCode, validAdditionalProcedureCodes, cachedData.additionalProcedureCodes))
      case _ =>
        Redirect(routes.ProcedureCodesController.displayPage(itemId))
    }
  }

  def submitAdditionalProcedureCodes(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form.bindFromRequest()
    val formAction = FormAction.bindFromRequest()

    val (maybeCachedProcedureCode, cachedData) = getCachedData(itemId)
    val availableAdditionalProcedureCodes = getAvailableAdditionalProcedureCodes(maybeCachedProcedureCode)

    def handleActionTypes(procedureCode: ProcedureCode, validAdditionalProcedureCodes: Seq[AdditionalProcedureCodeModel]): Future[Result] = {
      val errorHandler =
        returnErrorPage(itemId, boundForm.get, procedureCode, cachedData.additionalProcedureCodes, validAdditionalProcedureCodes)(_)
      val actionsRequiringValidation = Seq(Add, SaveAndContinue, SaveAndReturnToSummary, SaveAndReturnToErrors)

      formAction match {
        case Remove(values) =>
          removeCodeHandler(itemId, retrieveAdditionalProcedureCode(values), cachedData)

        case _ if !boundForm.hasErrors && actionsRequiringValidation.contains(formAction) =>
          validateForm(formAction, errorHandler, itemId, boundForm.get, cachedData)

        case _ =>
          Future.successful(
            BadRequest(
              additionalProcedureCodesPage(itemId, boundForm, procedureCode, validAdditionalProcedureCodes, cachedData.additionalProcedureCodes)
            )
          )
      }
    }

    (maybeCachedProcedureCode, availableAdditionalProcedureCodes) match {
      case (_, None) | (None, _) =>
        Future.successful(Redirect(routes.ProcedureCodesController.displayPage(itemId)))

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
          nextPage(action, itemId, cachedData.procedureCode, request.cacheModel)
        else
          errorHandler(Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.empty")))

      case (Some(NO_APC_APPLIES_CODE), _) if cachedAdditionalProcedureCodes.nonEmpty =>
        errorHandler(Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.tripleZero.notFirstCode")))

      case (Some(_), _) if cachedAdditionalProcedureCodes.contains(NO_APC_APPLIES_CODE) =>
        errorHandler(Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.tripleZero.alreadyPresent")))

      case (Some(code), _) if cachedAdditionalProcedureCodes.contains(code) =>
        errorHandler(Seq((additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.duplication")))

      case (Some(code), _) =>
        updateCache(itemId, ProcedureCodesData(None, cachedAdditionalProcedureCodes :+ code))
          .flatMap(nextPage(action, itemId, cachedData.procedureCode, _))
    }
  }

  private def removeCodeHandler(itemId: String, code: String, cachedData: ProcedureCodesData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {
    val updatedCache = cachedData.copy(additionalProcedureCodes = remove(cachedData.additionalProcedureCodes, (_: String) == code))
    updateCache(itemId, updatedCache).map(_ => navigator.continueTo(routes.AdditionalProcedureCodesController.displayPage(itemId)))
  }

  private def updateCache(itemId: String, updatedProcedureCodes: ProcedureCodesData)(
    implicit r: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] = {

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
    updateDeclarationFromRequest(updatedModel(_))
  }

  private def nextPage(action: FormAction, itemId: String, maybeProcedureCode: Option[String], declaration: ExportsDeclaration)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    (action, maybeProcedureCode) match {
      case (Add, _) =>
        Future.successful(navigator.continueTo(routes.AdditionalProcedureCodesController.displayPage(itemId)))

      case (_, Some(code)) if ProcedureCodesData.osrProcedureCodes.contains(code) =>
        Future.successful(navigator.continueTo(routes.FiscalInformationController.displayPage(itemId)))

      case _ =>
        val continueToCommodityDetails = navigator.continueTo(routes.CommodityDetailsController.displayPage(itemId))

        if (!supervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified(declaration)) Future.successful(continueToCommodityDetails)
        else resetSupervisingCustomsOfficeInCache(declaration).map(_ => continueToCommodityDetails)
    }

  private def resetSupervisingCustomsOfficeInCache(declaration: ExportsDeclaration)(implicit hc: HeaderCarrier): Future[ExportsDeclaration] =
    updateDeclaration(declaration.removeSupervisingCustomsOffice)

  private def returnErrorPage(
    itemId: String,
    userInput: AdditionalProcedureCode,
    procedureCode: ProcedureCode,
    cachedAdditionalProcedureCodes: Seq[String],
    validAdditionalProcedureCodes: Seq[AdditionalProcedureCodeModel]
  )(fieldWithError: Seq[(String, String)])(implicit request: JourneyRequest[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)
    val formWithError = form.fill(userInput).copy(errors = updatedErrors)

    Future.successful(
      BadRequest(additionalProcedureCodesPage(itemId, formWithError, procedureCode, validAdditionalProcedureCodes, cachedAdditionalProcedureCodes))
    )
  }

  private def retrieveAdditionalProcedureCode(values: Seq[String]): String = values.headOption.getOrElse("")

}
