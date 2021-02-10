/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.actions.{AuthAction, JourneyAction, VerifiedEmailAction}
import controllers.declaration.PackageInformationAddController.PackageInformationFormGroupId
import controllers.navigation.Navigator
import controllers.util.MultipleItemsHelper
import forms.declaration.PackageInformation
import forms.declaration.PackageInformation.form

import javax.inject.Inject
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.package_information_add

import scala.concurrent.{ExecutionContext, Future}

class PackageInformationAddController @Inject()(
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  packageInformationPage: package_information_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType) { implicit request =>
    val items = request.cacheModel.itemBy(itemId).flatMap(_.packageInformation).getOrElse(List.empty)
    Ok(packageInformationPage(mode, itemId, form().withSubmissionErrors(), items))
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType).async {
    implicit authRequest =>
      val boundForm = form().bindFromRequest()
      saveInformation(mode, itemId, boundForm, authRequest.cacheModel.itemBy(itemId).flatMap(_.packageInformation).getOrElse(Seq.empty))
  }

  private def saveInformation(mode: Mode, itemId: String, boundForm: Form[PackageInformation], cachedData: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, PackageInformation.limit, fieldId = PackageInformationFormGroupId, "declaration.packageInformation")
      .fold(
        formWithErrors => Future.successful(BadRequest(packageInformationPage(mode, itemId, formWithErrors, cachedData))),
        updatedCache =>
          updateExportsCache(itemId, updatedCache)
            .map(_ => navigator.continueTo(mode, controllers.declaration.routes.PackageInformationSummaryController.displayPage(_, itemId)))
      )

  private def updateExportsCache(itemId: String, updatedCache: Seq[PackageInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => model.updatedItem(itemId, _.copy(packageInformation = Some(updatedCache.toList))))
}

object PackageInformationAddController {
  val PackageInformationFormGroupId: String = "packageInformation"
}
