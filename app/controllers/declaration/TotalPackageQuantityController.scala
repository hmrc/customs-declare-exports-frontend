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
import forms.declaration.TotalPackageQuantity
import javax.inject.Inject
import models.DeclarationType.DeclarationType
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.total_package_quantity

import scala.concurrent.{ExecutionContext, Future}

class TotalPackageQuantityController @Inject()(
  authorize: AuthAction,
  journey: JourneyAction,
  mcc: MessagesControllerComponents,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  totalPackageQuantity: total_package_quantity
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode): Action[Unit] = (authorize andThen journey)(parse.empty) { implicit request =>
    Ok(totalPackageQuantity(mode, TotalPackageQuantity.form()))
  }

  def saveTotalPackageQuantity(mode: Mode): Action[AnyContent] = (authorize andThen journey).async { implicit request =>
    TotalPackageQuantity
      .form()
      .bindFromRequest()
      .fold(
        error => Future.successful(BadRequest(totalPackageQuantity(mode, error))),
        form => updateCache(form).map(_ => navigator.continueTo(mode, nextPage(request.declarationType)))
      )
  }

  private def nextPage(declarationType: DeclarationType): Mode => Call =
    declarationType match {
      case DeclarationType.SUPPLEMENTARY | DeclarationType.STANDARD | DeclarationType.CLEARANCE =>
        controllers.declaration.routes.NatureOfTransactionController.displayPage
      case DeclarationType.SIMPLIFIED | DeclarationType.OCCASIONAL =>
        controllers.declaration.routes.PreviousDocumentsController.displayPage
    }

  private def updateCache(totalPackage: TotalPackageQuantity)(implicit req: JourneyRequest[AnyContent]) =
    updateExportsDeclarationSyncDirect(_.copy(totalPackageQuantity = Some(totalPackage)))

}
