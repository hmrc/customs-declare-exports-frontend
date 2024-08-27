/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section5

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.helpers.MultipleItemsHelper
import controllers.helpers.PackageInformationHelper.allCachedPackageInformation
import controllers.helpers.SequenceIdHelper.handleSequencing
import controllers.navigation.Navigator
import controllers.section5.PackageInformationAddController.PackageInformationFormGroupId
import controllers.section5.routes.PackageInformationSummaryController
import forms.section5.PackageInformation
import forms.section5.PackageInformation.{form, limit, typeId}
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.PackageTypesService
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.validators.forms.AutoCompleteFieldBinding
import views.html.section5.packageInformation.package_information_add

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackageInformationAddController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  packageInformationPage: package_information_add
)(implicit ec: ExecutionContext, packageTypesService: PackageTypesService, auditService: AuditService)
    extends FrontendController(mcc) with AutoCompleteFieldBinding with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(packageInformationPage(itemId, form.withSubmissionErrors))
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val binding = form.bindFromRequest(formValuesFromRequest(typeId))
    MultipleItemsHelper
      .add(binding, allCachedPackageInformation(itemId), limit, PackageInformationFormGroupId, "declaration.packageInformation")
      .fold(
        formWithErrors => Future.successful(BadRequest(packageInformationPage(itemId, formWithErrors))),
        updateCache(itemId, _).map(_ => navigator.continueTo(PackageInformationSummaryController.displayPage(itemId)))
      )
  }

  private def updateCache(itemId: String, packageInformation: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] = {
    val declarationMeta = request.cacheModel.declarationMeta
    val (updatedPackageInformation, updatedMeta) = handleSequencing(packageInformation, declarationMeta)

    updateDeclarationFromRequest(
      _.updatedItem(itemId, _.copy(packageInformation = Some(updatedPackageInformation.toList)))
        .copy(declarationMeta = updatedMeta)
    )
  }
}

object PackageInformationAddController {
  val PackageInformationFormGroupId: String = "packageInformation"
}
