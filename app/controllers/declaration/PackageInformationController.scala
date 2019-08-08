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
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.declaration.PackageInformation
import forms.declaration.PackageInformation._
import handlers.ErrorHandler
import javax.inject.Inject
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.{ExportItem, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.package_information

import scala.concurrent.{ExecutionContext, Future}

class PackageInformationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  packageInformationPage: package_information
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  // TODO Future[Option[List[PackageInformation]]]...
  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    exportsCacheService
      .getItemByIdAndSession(itemId, journeySessionId)
      .map(_.map(_.packageInformation))
      .map(items => Ok(packageInformationPage(itemId, form(), items.getOrElse(Seq.empty))))
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit authRequest =>
      val actionTypeOpt = FormAction.bindFromRequest()
      val boundForm = form().bindFromRequest()
      exportsCacheService.getItemByIdAndSession(itemId, journeySessionId).map(_.map(_.packageInformation)).flatMap {
        data =>
          val packagings = data.getOrElse(Seq.empty)

          actionTypeOpt match {
            case Some(Add)             => addItem(itemId, packagings)
            case Some(Remove(ids))     => remove(itemId, packagings, boundForm, ids)
            case Some(SaveAndContinue) => continue(itemId, packagings)
            case _                     => errorHandler.displayErrorPage()
          }
      }
  }

  def remove(itemId: String, packages: Seq[PackageInformation], form: Form[PackageInformation], id: Seq[String])(
    implicit authRequest: JourneyRequest[AnyContent]
  ): Future[Result] = {
    val updatedPackages =
      packages.zipWithIndex.filterNot {
        case (_, index) => id.headOption.contains(index.toString)
      }.map(_._1)
    updateExportsCache(itemId, journeySessionId, updatedPackages)
      .map(_ => Ok(packageInformationPage(itemId, form.discardingErrors, updatedPackages)))
  }

  def continue(itemId: String, packages: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {
    val payload = form().bindFromRequest()
    if (!isFormEmpty(payload)) badRequest(itemId, packages, payload, USE_ADD)
    else if (packages.isEmpty) badRequest(itemId, packages, payload, ADD_ONE)
    else
      updateExportsCache(itemId, journeySessionId, packages)
        .map(_ => Redirect(controllers.declaration.routes.CommodityMeasureController.displayPage(itemId)))
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
            updateExportsCache(itemId, journeySessionId, packages :+ validForm)
              .map(_ => Redirect(routes.PackageInformationController.displayPage(itemId)))
          )(badRequest(itemId, packages, form().fill(validForm), _))
        }
      )

  private def isAdditionInvalid[A](item: A, cachedItems: Seq[A]): Option[String] =
    if (cachedItems.contains(item)) Some(DUPLICATE_MSG_KEY)
    else if (cachedItems.size >= PackageInformation.limit) Some(LIMIT_MSG_KEY)
    else None

  private def badRequest(itemId: String, packages: Seq[PackageInformation], form: Form[_], error: String)(
    implicit authenticatedRequest: JourneyRequest[AnyContent]
  ) =
    Future.successful(BadRequest(packageInformationPage(itemId, form.withGlobalError(error), packages)))

  private def updateExportsCache(
    itemId: String,
    sessionId: String,
    updatedCache: Seq[PackageInformation]
  ): Future[Option[ExportsDeclaration]] =
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
