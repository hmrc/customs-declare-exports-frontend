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
import controllers.declaration.routes.NatureOfTransactionController
import controllers.navigation.Navigator
import forms.declaration.TotalPackageQuantity
import models.declaration.InvoiceAndPackageTotals
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.total_package_quantity

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TotalPackageQuantityController @Inject() (
  authorize: AuthAction,
  journey: JourneyAction,
  mcc: MessagesControllerComponents,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  totalPackageQuantity: total_package_quantity
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val validTypes = Seq(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY)

  def displayPage: Action[AnyContent] = (authorize andThen journey(validTypes)) { implicit request =>
    val totalPackage = request.cacheModel.totalNumberOfItems.flatMap(_.totalPackage)
    val form = TotalPackageQuantity.form(request.declarationType).withSubmissionErrors
    Ok(totalPackageQuantity(totalPackage.fold(form)(value => form.fill(TotalPackageQuantity(Some(value))))))
  }

  def saveTotalPackageQuantity(): Action[AnyContent] = (authorize andThen journey(validTypes)).async { implicit request =>
    TotalPackageQuantity
      .form(request.declarationType)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(totalPackageQuantity(formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(NatureOfTransactionController.displayPage))
      )
  }

  private def updateCache(totalPackage: TotalPackageQuantity)(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] =
    if (totalPackage.totalPackage.isEmpty && request.cacheModel.totalNumberOfItems.isEmpty) Future.successful(request.cacheModel)
    else
      updateDeclarationFromRequest { declaration =>
        val invoiceAndPackageTotals = declaration.totalNumberOfItems.fold {
          InvoiceAndPackageTotals(None, None, None, None, totalPackage = totalPackage.totalPackage)
        } {
          _.copy(totalPackage = totalPackage.totalPackage)
        }
        declaration.copy(totalNumberOfItems = Some(invoiceAndPackageTotals))
      }
}
