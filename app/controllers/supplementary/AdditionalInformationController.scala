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
import controllers.util.CacheIdGenerator.supplementaryCacheId
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.supplementary.AdditionalInformation
import forms.supplementary.AdditionalInformation.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.supplementary.AdditionalInformationData
import models.declaration.supplementary.AdditionalInformationData.{formId, maxNumberOfItems}
import models.requests.AuthenticatedRequest
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.CustomsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.additional_information

import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[AdditionalInformationData](supplementaryCacheId, formId).map {
      case Some(data) => Ok(additional_information(appConfig, form, data.items))
      case _          => Ok(additional_information(appConfig, form, Seq()))
    }
  }

  def saveAdditionalInfo(): Action[AnyContent] = authenticate.async { implicit request =>
    val boundForm = form.bindFromRequest()

    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))

    val cachedData = customsCacheService
      .fetchAndGetEntry[AdditionalInformationData](supplementaryCacheId, formId)
      .map(_.getOrElse(AdditionalInformationData(Seq())))

    cachedData.flatMap { cache =>
      boundForm
        .fold(
          (formWithErrors: Form[AdditionalInformation]) =>
            Future.successful(BadRequest(additional_information(appConfig, formWithErrors, cache.items))),
          validForm =>
            actionTypeOpt match {
              case Some(Add)             => addItem(validForm, cache)
              case Some(SaveAndContinue) => saveAndContinue(validForm, cache)
              case Some(Remove(values))  => removeItem(retrieveItem(values), cache)
              case _                     => errorHandler.displayErrorPage()
          }
        )
    }
  }

  private def saveAndContinue(
    userInput: AdditionalInformation,
    cachedData: AdditionalInformationData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cachedData.items) match {
      case (item, Seq()) => handleSaveAndContinueEmptyCache(item)
      case (item, items) => handleSaveAndContinueCache(item, items)
    }

  private def handleSaveAndContinueCache(item: AdditionalInformation, items: Seq[AdditionalInformation])(
    implicit request: AuthenticatedRequest[_]
  ) =
    item match {
      case _ if items.length >= maxNumberOfItems =>
        handleErrorPage(Seq(("", "supplementary.additionalInformation.maximumAmount.error")), item, items)

      case _ if items.contains(item) =>
        handleErrorPage(Seq(("", "supplementary.additionalInformation.duplicated")), item, items)

      case _ if item.code.isDefined == item.description.isDefined =>
        val updatedItems = if (item.code.isDefined) items :+ item else items
        val updatedCache = AdditionalInformationData(updatedItems)

        customsCacheService.cache[AdditionalInformationData](supplementaryCacheId, formId, updatedCache).map { _ =>
          Redirect(controllers.supplementary.routes.DocumentsProducedController.displayForm())
        }

      case AdditionalInformation(maybeCode, maybeDescription) =>
        val codeError =
          maybeCode.fold(Seq(("code", "supplementary.additionalInformation.code.empty")))(_ => Seq[(String, String)]())
        val descriptionError = maybeDescription.fold(
          Seq(("description", "supplementary.additionalInformation.description.empty"))
        )(_ => Seq[(String, String)]())

        handleErrorPage(codeError ++ descriptionError, item, items)
    }

  private def handleSaveAndContinueEmptyCache(item: AdditionalInformation)(implicit request: AuthenticatedRequest[_]) =
    item match {
      case AdditionalInformation(Some(code), Some(description)) =>
        val updateCache = AdditionalInformationData(Seq(AdditionalInformation(Some(code), Some(description))))

        customsCacheService.cache[AdditionalInformationData](supplementaryCacheId(), formId, updateCache).map { _ =>
          Redirect(controllers.supplementary.routes.DocumentsProducedController.displayForm())
        }

      case AdditionalInformation(maybeCode, maybeDescription) =>
        val codeError =
          maybeCode.fold(Seq(("code", "supplementary.additionalInformation.code.empty")))(_ => Seq[(String, String)]())

        val descriptionError = maybeDescription.fold(
          Seq(("description", "supplementary.additionalInformation.description.empty"))
        )(_ => Seq[(String, String)]())

        handleErrorPage(codeError ++ descriptionError, item, Seq())
    }
  private def addItem(
    userInput: AdditionalInformation,
    cachedData: AdditionalInformationData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cachedData.items) match {
      case (_, items) if items.length >= maxNumberOfItems =>
        handleErrorPage(
          Seq(("", "supplementary.additionalInformation.maximumAmount.error")),
          userInput,
          cachedData.items
        )

      case (item, items) if items.contains(item) =>
        handleErrorPage(Seq(("", "supplementary.additionalInformation.duplicated")), userInput, cachedData.items)

      case (item, items) if item.code.isDefined && item.description.isDefined =>
        val updatedCache = AdditionalInformationData(items :+ item)

        customsCacheService.cache[AdditionalInformationData](supplementaryCacheId, formId, updatedCache).map { _ =>
          Redirect(controllers.supplementary.routes.AdditionalInformationController.displayForm())
        }

      case (AdditionalInformation(code, description), _) =>
        val codeError =
          code.fold(Seq(("code", "supplementary.additionalInformation.code.empty")))(_ => Seq[(String, String)]())
        val descriptionError = description.fold(
          Seq(("description", "supplementary.additionalInformation.description.empty"))
        )(_ => Seq[(String, String)]())

        handleErrorPage(codeError ++ descriptionError, userInput, cachedData.items)
    }

  private def removeItem(
    itemToRemove: AdditionalInformation,
    cachedData: AdditionalInformationData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    if (cachedData.containsItem(itemToRemove)) {
      val updatedCache = cachedData.copy(items = cachedData.items.filterNot(_ == itemToRemove))

      customsCacheService.cache[AdditionalInformationData](supplementaryCacheId, formId, updatedCache).map { _ =>
        Redirect(controllers.supplementary.routes.AdditionalInformationController.displayForm())
      }
    } else errorHandler.displayErrorPage()

  private def handleErrorPage(
    fieldWithError: Seq[(String, String)],
    userInput: AdditionalInformation,
    items: Seq[AdditionalInformation]
  )(implicit request: Request[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form.fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(additional_information(appConfig, formWithError, items)))
  }

  private def retrieveItem(values: Seq[String]): AdditionalInformation =
    AdditionalInformation.buildFromString(values.headOption.getOrElse(""))
}
