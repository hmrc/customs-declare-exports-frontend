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
import forms.declaration.PackageInformation
import forms.declaration.PackageInformation._
import handlers.ErrorHandler
import javax.inject.Inject
import models.{ExportsDeclaration, Mode}
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
  navigator: Navigator,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  packageInformationPage: package_information
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  // TODO Future[Option[List[PackageInformation]]]...
  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val items = request.cacheModel.itemBy(itemId).map(_.packageInformation).getOrElse(Nil)
    Ok(packageInformationPage(mode, itemId, form(), items))
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit authRequest =>
      val actionTypeOpt = FormAction.bindFromRequest()
      val boundForm = form().bindFromRequest()
      val packagings = authRequest.cacheModel.itemBy(itemId).map(_.packageInformation).getOrElse(Seq.empty)
      actionTypeOpt match {
        case Some(Add)                                   => addItem(mode, itemId, boundForm, packagings)
        case Some(Remove(values))                        => removeItem(mode, itemId, values, boundForm, packagings)
        case Some(SaveAndContinue) | Some(SaveAndReturn) => saveAndContinue(mode, itemId, boundForm, packagings)
        case _                                           => errorHandler.displayErrorPage()
      }
  }

  private def removeItem(
                          mode: Mode,
    itemId: String,
    values: Seq[String],
    boundForm: Form[PackageInformation],
    items: Seq[PackageInformation]
  )(implicit request: JourneyRequest[_]): Future[Result] = {
    val itemToRemove = PackageInformation.fromJsonString(values.head)
    val updatedCache = remove(items, itemToRemove.contains(_: PackageInformation))
    updateExportsCache(itemId, updatedCache)
      .map(_ => Ok(packageInformationPage(mode, itemId, boundForm.discardingErrors, updatedCache)))
  }

  private def saveAndContinue(mode: Mode, itemId: String, boundForm: Form[PackageInformation], cachedData: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .saveAndContinue(boundForm, cachedData, true, PackageInformation.limit)
      .fold(
        formWithErrors => Future.successful(BadRequest(packageInformationPage(mode, itemId, formWithErrors, cachedData))),
        updatedCache =>
          if (updatedCache != cachedData)
            updateExportsCache(itemId, updatedCache)
              .map(
                _ => navigator.continueTo(controllers.declaration.routes.CommodityMeasureController.displayPage(mode, itemId))
              )
          else
            Future.successful(Redirect(controllers.declaration.routes.CommodityMeasureController.displayPage(mode, itemId)))
      )

  private def addItem(mode: Mode, itemId: String, boundForm: Form[PackageInformation], cachedData: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, PackageInformation.limit)
      .fold(
        formWithErrors => Future.successful(BadRequest(packageInformationPage(mode, itemId, formWithErrors, cachedData))),
        updatedCache =>
          updateExportsCache(itemId, updatedCache)
            .map(
              _ => Redirect(controllers.declaration.routes.PackageInformationController.displayPage(mode, itemId))
          )
      )

  private def updateExportsCache(itemId: String, updatedCache: Seq[PackageInformation])(
    implicit r: JourneyRequest[_]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val item: Option[ExportItem] = model.items
        .find(item => item.id.equals(itemId))
        .map(_.copy(packageInformation = updatedCache.toList))
      val itemList = item.fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
      model.copy(items = itemList)
    })
}
