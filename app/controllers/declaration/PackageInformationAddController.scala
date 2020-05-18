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
import controllers.util.MultipleItemsHelper
import forms.declaration.PackageInformation
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.package_information_add

import scala.concurrent.{ExecutionContext, Future}

class PackageInformationAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  packageInformationPage: package_information_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val items = request.cacheModel.itemBy(itemId).flatMap(_.packageInformation).getOrElse(List.empty)
    Ok(packageInformationPage(mode, itemId, form(), items))
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit authRequest =>
    val boundForm = form().bindFromRequest()
    authRequest.cacheModel.itemBy(itemId).flatMap(_.packageInformation) match {
      case Some(items) if items.nonEmpty => saveNext(mode, itemId, boundForm, items)
      case _                             => saveFirst(mode, itemId, boundForm)
    }
  }

  private def saveFirst(mode: Mode, itemId: String, boundForm: Form[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .saveAndContinue(boundForm, Seq.empty, isMandatory(request), PackageInformation.limit)
      .fold(
        formWithErrors => Future.successful(BadRequest(packageInformationPage(mode, itemId, formWithErrors, Seq.empty))),
        updatedCache =>
          updateExportsCache(itemId, updatedCache)
            .map(_ => navigator.continueTo(mode, nextPage(itemId, boundForm.value)))
      )

  private def saveNext(mode: Mode, itemId: String, boundForm: Form[PackageInformation], cachedData: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, PackageInformation.limit)
      .fold(
        formWithErrors => Future.successful(BadRequest(packageInformationPage(mode, itemId, formWithErrors, cachedData))),
        updatedCache =>
          updateExportsCache(itemId, updatedCache)
            .map(_ => navigator.continueTo(mode, nextPage(itemId, boundForm.value)))
      )

  private def form()(implicit request: JourneyRequest[_]): Form[PackageInformation] = PackageInformation.form(request.declarationType)

  private def isMandatory(journeyRequest: JourneyRequest[_]): Boolean = journeyRequest.declarationType != DeclarationType.CLEARANCE

  private def updateExportsCache(itemId: String, updatedCache: Seq[PackageInformation])(
    implicit r: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => model.updatedItem(itemId, _.copy(packageInformation = Some(updatedCache.toList))))

  private def nextPage(itemId: String, submittedPackageInformation: Option[PackageInformation])(implicit request: JourneyRequest[_]): Mode => Call =
    if (submittedPackageInformation.exists(_.nonEmpty))
      controllers.declaration.routes.PackageInformationSummaryController.displayPage(_, itemId)
    else
      PackageInformationSummaryController.nextPage(itemId)
}
