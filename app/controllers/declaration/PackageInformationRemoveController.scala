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
import controllers.helpers.PackageInformationHelper.singleCachedPackageInformation
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.PackageInformation
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.packageInformation.package_information_remove

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackageInformationRemoveController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  packageTypeRemove: package_information_remove
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(packageTypeRemove(mode, itemId, singleCachedPackageInformation(id, itemId), removeYesNoForm.withSubmissionErrors()))
  }

  def submitForm(mode: Mode, itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val packageInformationToRemove = singleCachedPackageInformation(id, itemId)
    removeYesNoForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) =>
          Future.successful(BadRequest(packageTypeRemove(mode, itemId, packageInformationToRemove, formWithErrors))),
        formData => {
          formData.answer match {
            case YesNoAnswers.yes =>
              updateExportsCache(itemId, packageInformationToRemove)
                .map(_ => navigator.continueTo(mode, routes.PackageInformationSummaryController.displayPage(_, itemId)))
            case YesNoAnswers.no =>
              Future.successful(navigator.continueTo(mode, routes.PackageInformationSummaryController.displayPage(_, itemId)))
          }
        }
      )
  }

  private def removeYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.packageInformation.remove.empty")

  private def updateExportsCache(itemId: String, itemToRemove: PackageInformation)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] = {
    val updatedPackageInformation =
      request.cacheModel.itemBy(itemId).flatMap(_.packageInformation).getOrElse(Seq.empty).filterNot(_ == itemToRemove)
    updateDeclarationFromRequest(model => model.updatedItem(itemId, _.copy(packageInformation = Some(updatedPackageInformation.toList))))
  }
}
