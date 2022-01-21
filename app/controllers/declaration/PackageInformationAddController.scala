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
import controllers.navigation.{ItemId, Navigator}
import controllers.helpers.MultipleItemsHelper
import forms.declaration.PackageInformation
import forms.declaration.PackageInformation.form
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.package_information_add

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackageInformationAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  packageInformationPage: package_information_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    resolveBackLink(mode, itemId).map { backLink =>
      Ok(packageInformationPage(mode, itemId, form().withSubmissionErrors(), cachedPackageInformation(itemId), backLink))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    saveInformation(mode, itemId, boundForm, cachedPackageInformation(itemId))
  }

  private def cachedPackageInformation(itemId: String)(implicit request: JourneyRequest[_]): Seq[PackageInformation] =
    request.cacheModel.itemBy(itemId).flatMap(_.packageInformation).getOrElse(List.empty)

  private def saveInformation(mode: Mode, itemId: String, boundForm: Form[PackageInformation], cachedData: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, PackageInformation.limit, fieldId = PackageInformationFormGroupId, "declaration.packageInformation")
      .fold(
        formWithErrors =>
          resolveBackLink(mode, itemId).map { backLink =>
            BadRequest(packageInformationPage(mode, itemId, formWithErrors, cachedData, backLink))
        },
        updatedCache =>
          updateExportsCache(itemId, updatedCache)
            .map(_ => navigator.continueTo(mode, controllers.declaration.routes.PackageInformationSummaryController.displayPage(_, itemId)))
      )

  private def updateExportsCache(itemId: String, updatedCache: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => model.updatedItem(itemId, _.copy(packageInformation = Some(updatedCache.toList))))

  private def resolveBackLink(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[Call] =
    navigator.backLink(PackageInformation, mode, ItemId(itemId))
}

object PackageInformationAddController {
  val PackageInformationFormGroupId: String = "packageInformation"
}
