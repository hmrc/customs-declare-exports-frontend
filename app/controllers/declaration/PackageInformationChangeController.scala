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
import controllers.declaration.PackageInformationAddController.PackageInformationFormGroupId
import controllers.helpers.MultipleItemsHelper
import controllers.helpers.PackageInformationHelper.{allCachedPackageInformation, singleCachedPackageInformation}
import controllers.helpers.SequenceIdHelper.handleSequencing
import controllers.navigation.Navigator
import forms.declaration.PackageInformation
import forms.declaration.PackageInformation.form
import handlers.ErrorHandler
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.PackageTypesService
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.packageInformation.package_information_change

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackageInformationChangeController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  val exportsCacheService: ExportsCacheService,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  packageChangePage: package_information_change
)(implicit ec: ExecutionContext, packageTypesService: PackageTypesService, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String, code: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val maybePackageInformation = singleCachedPackageInformation(code, itemId)

    maybePackageInformation.fold(errorHandler.redirectToErrorPage) { packageInfo =>
      Future.successful(Ok(packageChangePage(itemId, PackageInformation.form.fill(packageInfo).withSubmissionErrors, code)))
    }
  }

  def submitForm(itemId: String, code: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val maybePackageInfoToRemove = singleCachedPackageInformation(code, itemId)
    val boundForm = form.bindFromRequest()

    maybePackageInfoToRemove.fold(errorHandler.redirectToErrorPage) { packageInfoToRemove =>
      saveInformation(itemId, boundForm, allCachedPackageInformation(itemId), packageInfoToRemove)
    }
  }

  private def saveInformation(
    itemId: String,
    boundForm: Form[PackageInformation],
    cachedData: Seq[PackageInformation],
    packageInfoToRemove: PackageInformation
  )(implicit request: JourneyRequest[AnyContent]): Future[Result] = {
    val listWithRemovedPackageInfo = MultipleItemsHelper.remove(cachedData, packageInfoToRemove.equals(_: PackageInformation))
    MultipleItemsHelper
      .add(boundForm, listWithRemovedPackageInfo, PackageInformation.limit, fieldId = PackageInformationFormGroupId, "declaration.packageInformation")
      .fold(
        formWithErrors => Future.successful(BadRequest(packageChangePage(itemId, formWithErrors, packageInfoToRemove.id))),
        newPackageInfos =>
          updateExportsCache(itemId, newPackageInfos)
            .map(_ => navigator.continueTo(routes.PackageInformationSummaryController.displayPage(itemId)))
      )
  }

  private def updateExportsCache(itemId: String, packageInfos: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] = {
    val (updatedPackageInfo, updatedMeta) = handleSequencing(packageInfos, request.cacheModel.declarationMeta)
    updateDeclarationFromRequest(
      _.updatedItem(itemId, _.copy(packageInformation = Some(updatedPackageInfo.toList))).copy(declarationMeta = updatedMeta)
    )
  }
}
