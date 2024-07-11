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

package controllers.section4

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.helpers.TransportSectionHelper.isGuernseyOrJerseyDestination
import controllers.navigation.Navigator
import controllers.section4.routes.NatureOfTransactionController
import forms.section4.TotalPackageQuantity
import models.declaration.InvoiceAndPackageTotals
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section4.total_package_quantity

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TotalPackageQuantityController @Inject() (
  authorize: AuthAction,
  journey: JourneyAction,
  mcc: MessagesControllerComponents,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  totalPackageQuantity: total_package_quantity
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val validTypes = Seq(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY)

  val displayPage: Action[AnyContent] = (authorize andThen journey(validTypes)) async { implicit request =>
    if (isGuernseyOrJerseyDestination(request.cacheModel)) updateCache(TotalPackageQuantity(None)) map (_ => nextPage)
    else {
      val totalPackage = request.cacheModel.totalNumberOfItems.flatMap(_.totalPackage)
      val form = TotalPackageQuantity.form(request.declarationType).withSubmissionErrors
      Future.successful(Ok(totalPackageQuantity(totalPackage.fold(form)(value => form.fill(TotalPackageQuantity(Some(value)))))))
    }
  }

  val saveTotalPackageQuantity: Action[AnyContent] = (authorize andThen journey(validTypes)) async { implicit request =>
    if (isGuernseyOrJerseyDestination(request.cacheModel)) updateCache(TotalPackageQuantity(None)) map (_ => nextPage)
    else {
      TotalPackageQuantity
        .form(request.declarationType)
        .bindFromRequest()
        .fold(formWithErrors => Future.successful(BadRequest(totalPackageQuantity(formWithErrors))), updateCache(_) map (_ => nextPage))
    }
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

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Result = navigator.continueTo(NatureOfTransactionController.displayPage)
}
