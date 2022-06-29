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
import controllers.declaration.PackageInformationAddController.PackageInformationFormGroupId
import controllers.helpers.MultipleItemsHelper
import controllers.helpers.PackageInformationHelper.{allCachedPackageInformation, singleCachedPackageInformation}
import controllers.navigation.Navigator
import forms.declaration.PackageInformation
import forms.declaration.PackageInformation.form
import handlers.ErrorHandler
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.packageInformation.package_information_change

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackageInformationChangeController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  val exportsCacheService: ExportsCacheService,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  packageChangePage: package_information_change
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(mode: Mode, itemId: String, code: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val maybePackageInformation = singleCachedPackageInformation(code, itemId)

    maybePackageInformation.fold(errorHandler.displayErrorPage()) { packageInfo =>
      Future.successful(Ok(packageChangePage(mode, itemId, PackageInformation.form().fill(packageInfo).withSubmissionErrors(), code, Seq.empty)))
    }
  }

  def submitForm(mode: Mode, itemId: String, code: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val maybePackageInfoToRemove = singleCachedPackageInformation(code, itemId)
    val boundForm = form().bindFromRequest()

    maybePackageInfoToRemove.fold(errorHandler.displayErrorPage()) { packageInfoToRemove =>
      saveInformation(mode, itemId, boundForm, allCachedPackageInformation(itemId), packageInfoToRemove)
    }
  }

  private def saveInformation(
    mode: Mode,
    itemId: String,
    boundForm: Form[PackageInformation],
    cachedData: Seq[PackageInformation],
    packageInfoToRemove: PackageInformation
  )(implicit request: JourneyRequest[AnyContent]): Future[Result] = {
    val listWithRemovedPackageInfo = MultipleItemsHelper.remove(cachedData, packageInfoToRemove.equals(_: PackageInformation))
    MultipleItemsHelper
      .add(boundForm, listWithRemovedPackageInfo, PackageInformation.limit, fieldId = PackageInformationFormGroupId, "declaration.packageInformation")
      .fold(
        formWithErrors => Future.successful(BadRequest(packageChangePage(mode, itemId, formWithErrors, packageInfoToRemove.id, cachedData))),
        updatedCache =>
          updateExportsCache(itemId, updatedCache)
            .map(_ => navigator.continueTo(mode, controllers.declaration.routes.PackageInformationSummaryController.displayPage(_, itemId)))
      )
  }

  private def updateExportsCache(itemId: String, updatedPackageInformation: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.updatedItem(itemId, _.copy(packageInformation = Some(updatedPackageInformation.toList))))

}
