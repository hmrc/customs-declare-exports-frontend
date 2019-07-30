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
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.declaration.PackageInformation
import forms.declaration.PackageInformation.{formId, _}
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.CustomsCacheService
import services.cache.{ExportItem, ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.package_information

import scala.concurrent.{ExecutionContext, Future}

class PackageInformationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  legacyCacheService: CustomsCacheService,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  packageInformationPage: package_information
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  val packagesMaxElements = 99

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    exportsCacheService
      .getItemByIdAndSession(itemId, journeySessionId)
      .map(_.map(_.packageInformation))
      .map(items => Ok(packageInformationPage(itemId, form(), items.getOrElse(Seq.empty))))
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit authRequest =>
      val actionTypeOpt = authRequest.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded)
      exportsCacheService.getItemByIdAndSession(itemId, journeySessionId).map(_.map(_.packageInformation)).flatMap {
        data =>
          val packagings = data.getOrElse(Seq.empty)

          actionTypeOpt match {
            case Some(Add)             => addItem(itemId, packagings)
            case Some(Remove(ids))     => remove(itemId, packagings, ids.headOption)
            case Some(SaveAndContinue) => continue(itemId, packagings)
            case _                     => errorHandler.displayErrorPage()
          }
      }
  }

  def remove(itemId: String, packages: Seq[PackageInformation], id: Option[String])(
    implicit authRequest: JourneyRequest[AnyContent]
  ): Future[Result] =
    id match {
      case Some(identifier) =>
        val updatedPackages =
          packages.zipWithIndex.filterNot(_._2.toString == identifier).map(_._1)
        updateCacheModels(itemId, updatedPackages, routes.PackageInformationController.displayPage(itemId))
      case _ => errorHandler.displayErrorPage()
    }

  def continue(itemId: String, packages: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {
    val payload = form().bindFromRequest()
    if (!isFormEmpty(payload)) badRequest(itemId, packages, payload, USE_ADD)
    else if (packages.isEmpty) badRequest(itemId, packages, payload, ADD_ONE)
    else
      updateCacheModels(itemId, packages, controllers.declaration.routes.CommodityMeasureController.displayPage(itemId))
  }

  private def isFormEmpty[A](form: Form[A]): Boolean =
    !retrieveData(form).exists { case (_, value) => value.nonEmpty }

  private def retrieveData[A](form: Form[A]): Map[String, String] =
    form.data.filter { case (name, _) => name != "csrfToken" }

  def addItem(itemId: String, packages: Seq[PackageInformation])(
    implicit authenticatedRequest: JourneyRequest[AnyContent]
  ): Future[Result] =
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[PackageInformation]) =>
          Future.successful(BadRequest(packageInformationPage(itemId, formWithErrors, packages))),
        validForm => {
          isAdditionInvalid[PackageInformation](validForm, packages).fold(
            updateCacheModels(itemId, packages :+ validForm, routes.PackageInformationController.displayPage(itemId))
          )(badRequest(itemId, packages, form().fill(validForm), _))
        }
      )

  private def isAdditionInvalid[A](item: A, cachedItems: Seq[A]): Option[String] =
    if (cachedItems.contains(item)) Some(DUPLICATE_MSG_KEY)
    else if (cachedItems.size >= packagesMaxElements) Some(LIMIT_MSG_KEY)
    else None

  private def badRequest(itemId: String, packages: Seq[PackageInformation], form: Form[_], error: String)(
    implicit authenticatedRequest: JourneyRequest[AnyContent]
  ) =
    Future.successful(BadRequest(packageInformationPage(itemId, form.withGlobalError(error), packages)))

  private def updateCacheModels(itemId: String, updatedCache: Seq[PackageInformation], redirect: Call)(
    implicit journeyRequest: JourneyRequest[_]
  ) =
    for {
      _ <- updateExportsCache(itemId, journeySessionId, updatedCache)
      _ <- legacyCacheService.cache[Seq[PackageInformation]](goodsItemCacheId(), formId, updatedCache)
    } yield Redirect(redirect)

  private def updateExportsCache(
    itemId: String,
    sessionId: String,
    updatedCache: Seq[PackageInformation]
  ): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => {
        val item: Option[ExportItem] = model.items
          .find(item => item.id.equals(itemId))
          .map(_.copy(packageInformation = updatedCache.toList))
        val itemList = item.fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
        exportsCacheService.update(sessionId, model.copy(items = itemList))
      }
    )
}
