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
import forms.declaration.DeclarationHolder
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.DeclarationHoldersData
import models.declaration.DeclarationHoldersData.limitOfHolders
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.declaration_holder

import scala.concurrent.{ExecutionContext, Future}

class DeclarationHolderController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarationHolderPage: declaration_holder
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  import forms.declaration.DeclarationHolder.form

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.parties.declarationHoldersData match {
      case Some(data) => Ok(declarationHolderPage(mode, form(), data.holders))
      case _          => Ok(declarationHolderPage(mode, form(), Seq()))
    }
  }

  def submitHoldersOfAuthorisation(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    val actionTypeOpt = FormAction.bindFromRequest()

    val cache = request.cacheModel.parties.declarationHoldersData.getOrElse(DeclarationHoldersData(Seq()))

    actionTypeOpt match {
      case Add if !boundForm.hasErrors => addHolder(mode, boundForm.get, cache)
      case SaveAndContinue | SaveAndReturn if !boundForm.hasErrors =>
        saveAndContinue(mode, boundForm.get, cache)
      case Remove(values) => removeHolder(mode, retrieveHolder(values), boundForm, cache)
      case _              => Future.successful(BadRequest(declarationHolderPage(mode, boundForm, cache.holders)))
    }
  }

  private def addHolder(mode: Mode, userInput: DeclarationHolder, cachedData: DeclarationHoldersData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    (userInput, cachedData.holders) match {
      case (_, holders) if holders.length >= limitOfHolders =>
        handleErrorPage(mode, Seq(("", "supplementary.declarationHolders.maximumAmount.error")), userInput, cachedData.holders)

      case (holder, holders) if holders.contains(holder) =>
        handleErrorPage(mode, Seq(("", "supplementary.declarationHolders.duplicated")), userInput, cachedData.holders)

      case (holder, holders) if holder.authorisationTypeCode.isDefined && holder.eori.isDefined =>
        val updatedCache = DeclarationHoldersData(holders :+ holder)
        updateCache(updatedCache)
          .map(_ => Redirect(controllers.declaration.routes.DeclarationHolderController.displayPage(mode)))

      case (DeclarationHolder(authCode, eori), _) =>
        val authCodeError =
          authCode.fold(Seq(("authorisationTypeCode", "supplementary.declarationHolder.authorisationCode.empty")))(_ => Seq[(String, String)]())
        val eoriError = eori.fold(Seq(("eori", "supplementary.eori.empty")))(_ => Seq[(String, String)]())

        handleErrorPage(mode, authCodeError ++ eoriError, userInput, cachedData.holders)
    }

  //scalastyle:off method.length
  private def handleErrorPage(mode: Mode, fieldWithError: Seq[(String, String)], userInput: DeclarationHolder, holders: Seq[DeclarationHolder])(
    implicit request: Request[_]
  ): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form().fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(declarationHolderPage(mode, formWithError, holders)))
  }

  private def updateCache(formData: DeclarationHoldersData)(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedParties = model.parties.copy(declarationHoldersData = Some(formData))
      model.copy(parties = updatedParties)
    })

  private def saveAndContinue(mode: Mode, userInput: DeclarationHolder, cachedData: DeclarationHoldersData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    (userInput, cachedData.holders) match {
      case (DeclarationHolder(None, None), _) =>
        Future.successful(navigator.continueTo(routes.DestinationCountriesController.displayPage(mode)))

      case (holder, Seq()) =>
        holder match {
          case DeclarationHolder(Some(typeCode), Some(eori)) =>
            val updatedCache = DeclarationHoldersData(Seq(DeclarationHolder(Some(typeCode), Some(eori))))
            updateCache(updatedCache)
              .map(_ => navigator.continueTo(controllers.declaration.routes.DestinationCountriesController.displayPage(mode)))

          case DeclarationHolder(maybeTypeCode, maybeEori) =>
            val typeCodeError = maybeTypeCode.fold(Seq(("authorisationTypeCode", "supplementary.declarationHolder.authorisationCode.empty")))(
              _ => Seq[(String, String)]()
            )

            val eoriError = maybeEori.fold(Seq(("eori", "supplementary.eori.empty")))(_ => Seq[(String, String)]())

            handleErrorPage(mode, typeCodeError ++ eoriError, userInput, Seq())
        }

      case (holder, holders) =>
        holder match {
          case _ if holders.length >= limitOfHolders =>
            handleErrorPage(mode, Seq(("", "supplementary.declarationHolders.maximumAmount.error")), userInput, holders)

          case _ if holders.contains(holder) =>
            handleErrorPage(mode, Seq(("", "supplementary.declarationHolders.duplicated")), userInput, holders)

          case _ if holder.authorisationTypeCode.isDefined == holder.eori.isDefined =>
            val updatedHolders = if (holder.authorisationTypeCode.isDefined) holders :+ holder else holders
            val updatedCache = DeclarationHoldersData(updatedHolders)
            updateCache(updatedCache)
              .map(_ => navigator.continueTo(controllers.declaration.routes.DestinationCountriesController.displayPage(mode)))

          case DeclarationHolder(maybeTypeCode, maybeEori) =>
            val typeCodeError = maybeTypeCode.fold(Seq(("authorisationTypeCode", "supplementary.declarationHolder.authorisationCode.empty")))(
              _ => Seq[(String, String)]()
            )

            val eoriError = maybeEori.fold(Seq(("eori", "supplementary.eori.empty")))(_ => Seq[(String, String)]())

            handleErrorPage(mode, typeCodeError ++ eoriError, userInput, holders)
        }
    }
  //scalastyle:on method.length

  private def removeHolder(mode: Mode, holderToRemove: DeclarationHolder, userInput: Form[DeclarationHolder], cachedData: DeclarationHoldersData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] = {
    val updatedCache = cachedData.copy(holders = remove(cachedData.holders, (_: DeclarationHolder) == holderToRemove))
    updateCache(updatedCache)
      .map(_ => Ok(declarationHolderPage(mode, userInput.discardingErrors, updatedCache.holders)))
  }

  private def retrieveHolder(values: Seq[String]): DeclarationHolder =
    DeclarationHolder.buildFromString(values.headOption.getOrElse(""))
}
