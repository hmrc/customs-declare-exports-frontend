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
import controllers.helpers.PackageInformationHelper.allCachedPackageInformation
import controllers.navigation.Navigator
import forms.declaration.PackageInformation
import forms.declaration.PackageInformation.form
import models.requests.JourneyRequest
import models.{DeclarationMeta, ExportsDeclaration}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.PackageTypesService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.packageInformation.package_information_add

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackageInformationAddController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  packageInformationPage: package_information_add
)(implicit ec: ExecutionContext, packageTypesService: PackageTypesService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(packageInformationPage(itemId, form.withSubmissionErrors))
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form.bindFromRequest()
    saveInformation(itemId, boundForm, allCachedPackageInformation(itemId))
  }

  private def saveInformation(itemId: String, boundForm: Form[PackageInformation], cachedData: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, PackageInformation.limit, fieldId = PackageInformationFormGroupId, "declaration.packageInformation")
      .fold(
        formWithErrors => Future.successful(BadRequest(packageInformationPage(itemId, formWithErrors))),
        updatedCache =>
          updateCache(itemId, updatedCache)
            .map(_ => navigator.continueTo(routes.PackageInformationSummaryController.displayPage(itemId)))
      )

  private def updateCache(itemId: String, packageInformations: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] = {
    val declarationMeta = request.cacheModel.declarationMeta

    val (updatedMeta, updatedPackageInformations): (DeclarationMeta, List[PackageInformation]) =
      packageInformations.foldLeft((declarationMeta, List.empty[PackageInformation])) {
        (tuple: (DeclarationMeta, List[PackageInformation]), packageInformation: PackageInformation) =>
          val meta = tuple._1
          val packageInformations = tuple._2
          packageInformation match {
            case newPackageInformation @ PackageInformation(DeclarationMeta.sequenceIdPlaceholder, _, _, _, _) =>
              val (packInfo, updatedMeta) = PackageInformation.copyWithIncrementedSeqId(newPackageInformation, meta)
              (updatedMeta, packageInformations :+ packInfo)
            case existingPackageInformation @ _ => (meta, packageInformations :+ existingPackageInformation)
          }
      }

    updateDeclarationFromRequest(
      _.updatedItem(itemId, _.copy(packageInformation = Some(updatedPackageInformations)))
        .copy(declarationMeta = updatedMeta)
    )
  }
}

object PackageInformationAddController {
  val PackageInformationFormGroupId: String = "packageInformation"
}
