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

import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import controllers.util.MultipleItemsHelper.remove
import controllers.util._
import forms.declaration.procedurecodes.AdditionalProcedureCodes
import forms.declaration.procedurecodes.AdditionalProcedureCodes.form
import models.declaration.ProcedureCodesData
import models.declaration.ProcedureCodesData.limitOfCodes
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.FormError
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
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
  additionalProcedureCodesPage: additional_procedure_codes
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form().withSubmissionErrors()
    val cachedAdditionalProcedureCodes = request.cacheModel.itemBy(itemId) match {
      case Some(exportItem) => exportItem.procedureCodes.fold(Seq.empty[String])(_.additionalProcedureCodes)
      case None             => Seq.empty[String]
    }

    Ok(additionalProcedureCodesPage(mode, itemId, frm, cachedAdditionalProcedureCodes))
  }

  def submitAdditionalProcedureCodes(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    val actionTypeOpt = FormAction.bindFromRequest()
    val cachedProcedureCodes = request.cacheModel.itemBy(itemId).flatMap(_.procedureCodes).getOrElse(ProcedureCodesData(None, Seq()))

    actionTypeOpt match {
      case Add if !boundForm.hasErrors                             => addAnotherCodeHandler(mode, itemId, boundForm.get, cachedProcedureCodes)
      case SaveAndContinue | SaveAndReturn if !boundForm.hasErrors => saveAndContinueHandler(mode, itemId, boundForm.get, cachedProcedureCodes)
      case Remove(values)                                          => removeCodeHandler(mode, itemId, retrieveAdditionalProcedureCode(values), cachedProcedureCodes)
      case _                                                       => Future.successful(BadRequest(additionalProcedureCodesPage(mode, itemId, boundForm, cachedProcedureCodes.additionalProcedureCodes)))
    }
  }

  private def addAnotherCodeHandler(mode: Mode, itemId: String, userInput: AdditionalProcedureCodes, cachedData: ProcedureCodesData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    (userInput.additionalProcedureCode, cachedData.additionalProcedureCodes) match {
      case (_, cachedAdditionalProcedureCodes) if cachedAdditionalProcedureCodes.length >= limitOfCodes =>
        returnErrorPage(
          mode,
          itemId,
          Seq(("", "declaration.additionalProcedureCodes.maximumAmount.error")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (None, _) =>
        returnErrorPage(
          mode,
          itemId,
          Seq(("additionalProcedureCode", "declaration.additionalProcedureCodes.empty")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (Some(code), cachedAdditionalProcedureCodes) if cachedAdditionalProcedureCodes.contains(code) =>
        returnErrorPage(
          mode,
          itemId,
          Seq(("additionalProcedureCode", "declaration.additionalProcedureCodes.duplication")),
          userInput,
          cachedData.additionalProcedureCodes
        )

      case (Some(code), cachedAdditionalProcedureCodes) =>
        updateCache(itemId, ProcedureCodesData(None, cachedAdditionalProcedureCodes :+ code))
          .map(_ => navigator.continueTo(mode, routes.AdditionalProcedureCodesController.displayPage(_, itemId)))
    }

  private def saveAndContinueHandler(mode: Mode, itemId: String, userInput: AdditionalProcedureCodes, cachedData: ProcedureCodesData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    (userInput, cachedData.additionalProcedureCodes) match {

      case (newAdditionalProcedureCode, Seq()) =>
        newAdditionalProcedureCode match {

          case AdditionalProcedureCodes(Some(additionalCode)) =>
            updateCache(itemId, ProcedureCodesData(None, Seq(additionalCode))).map(declaration => nextPage(mode, itemId, declaration))

          case AdditionalProcedureCodes(None) =>
            val additionalCodeError = Seq(("additionalProcedureCode", "declaration.additionalProcedureCodes.mandatory.error"))
            returnErrorPage(mode, itemId, additionalCodeError, userInput, cachedData.additionalProcedureCodes)
        }

      case (newAdditionalProcedureCode, cachedAdditionalProcedureCodes) =>
        newAdditionalProcedureCode match {

          case AdditionalProcedureCodes(Some(_)) if cachedAdditionalProcedureCodes.length >= limitOfCodes =>
            returnErrorPage(
              mode,
              itemId,
              Seq(("", "declaration.additionalProcedureCodes.maximumAmount.error")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case AdditionalProcedureCodes(Some(additionalCode)) if cachedAdditionalProcedureCodes.contains(additionalCode) =>
            returnErrorPage(
              mode,
              itemId,
              Seq(("additionalProcedureCode", "declaration.additionalProcedureCodes.duplication")),
              userInput,
              cachedData.additionalProcedureCodes
            )

          case AdditionalProcedureCodes(Some(additionalCode)) =>
            val updatedCache = ProcedureCodesData(None, cachedData.additionalProcedureCodes :+ additionalCode)
            updateCache(itemId, updatedCache).map(declaration => nextPage(mode, itemId, declaration))

          case AdditionalProcedureCodes(None) =>
            val updatedCache = ProcedureCodesData(None, cachedData.additionalProcedureCodes)
            updateCache(itemId, updatedCache).map(declaration => nextPage(mode, itemId, declaration))

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

  private def nextPage(mode: Mode, itemId: String, declaration: Option[ExportsDeclaration])(implicit request: JourneyRequest[AnyContent]) =
    declaration.flatMap(_.itemBy(itemId)).flatMap(_.procedureCodes).flatMap(_.procedureCode) match {
      case Some(code) if ProcedureCodesData.osrProcedureCodes.contains(code) =>
        navigator.continueTo(mode, routes.FiscalInformationController.displayPage(_, itemId))
      case _ => navigator.continueTo(mode, routes.CommodityDetailsController.displayPage(_, itemId))
    }

  private def returnErrorPage(
    mode: Mode,
    itemId: String,
    fieldWithError: Seq[(String, String)],
    userInput: AdditionalProcedureCodes,
    additionalProcedureCodes: Seq[String]
  )(implicit request: JourneyRequest[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form().fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(additionalProcedureCodesPage(mode, itemId, formWithError, additionalProcedureCodes)))
  }

  private def retrieveAdditionalProcedureCode(values: Seq[String]): String = values.headOption.getOrElse("")

}
