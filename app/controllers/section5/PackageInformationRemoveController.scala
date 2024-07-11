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

package controllers.section5

import controllers.actions.{AuthAction, JourneyAction}
import controllers.section5.routes.PackageInformationSummaryController
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.helpers.PackageInformationHelper.singleCachedPackageInformation
import controllers.helpers.SequenceIdHelper.handleSequencing
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section5.PackageInformation
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section5.packageInformation.package_information_remove

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackageInformationRemoveController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  packageTypeRemove: package_information_remove
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    singleCachedPackageInformation(id, itemId).fold(redirectToPackageListPage(itemId)) { packageInfo =>
      Future.successful(Ok(packageTypeRemove(itemId, packageInfo, removeYesNoForm.withSubmissionErrors)))
    }
  }

  def submitForm(itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    singleCachedPackageInformation(id, itemId).fold(redirectToPackageListPage(itemId)) { packageInfo =>
      removeYesNoForm
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(packageTypeRemove(itemId, packageInfo, formWithErrors))),
          _.answer match {
            case YesNoAnswers.yes => updateExportsCache(itemId, packageInfo).flatMap(_ => nextPage(itemId))
            case YesNoAnswers.no  => nextPage(itemId)
          }
        )
    }
  }

  private def nextPage(itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    Future.successful(navigator.continueTo(PackageInformationSummaryController.displayPage(itemId)))

  private def redirectToPackageListPage(itemId: String): Future[Result] =
    Future.successful(Redirect(PackageInformationSummaryController.displayPage(itemId)))

  private def removeYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.packageInformation.remove.empty")

  private def updateExportsCache(itemId: String, itemToRemove: PackageInformation)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] = {
    val filteredPackageInformation = request.cacheModel
      .itemBy(itemId)
      .flatMap(_.packageInformation)
      .getOrElse(Seq.empty)
      .filterNot(_ == itemToRemove)

    val (updatedPackageInformation, updatedMeta) = handleSequencing(filteredPackageInformation, request.cacheModel.declarationMeta)
    updateDeclarationFromRequest(
      _.updatedItem(itemId, _.copy(packageInformation = Some(updatedPackageInformation.toList))).copy(declarationMeta = updatedMeta)
    )
  }
}
