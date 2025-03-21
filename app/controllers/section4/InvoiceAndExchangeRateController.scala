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

package controllers.section4

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section4.routes.TotalPackageQuantityController
import forms.section4.InvoiceAndExchangeRate
import forms.section4.InvoiceAndExchangeRate._
import models.declaration.InvoiceAndPackageTotals
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section4.invoice_and_exchange_rate

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class InvoiceAndExchangeRateController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  invoiceAndExchangeRatePage: invoice_and_exchange_rate,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val validTypes = Seq(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY)

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    request.cacheModel.totalNumberOfItems match {
      case Some(data) => Ok(invoiceAndExchangeRatePage(form.withSubmissionErrors.fill(InvoiceAndExchangeRate(data))))
      case _          => Ok(invoiceAndExchangeRatePage(form.withSubmissionErrors))
    }
  }

  def saveNoOfItems(): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(invoiceAndExchangeRatePage(formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(TotalPackageQuantityController.displayPage))
      )
  }

  private def updateCache(invoiceAndExchangeRate: InvoiceAndExchangeRate)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      declaration.copy(totalNumberOfItems =
        Some(
          InvoiceAndPackageTotals(
            totalAmountInvoiced = invoiceAndExchangeRate.totalAmountInvoiced,
            totalAmountInvoicedCurrency = invoiceAndExchangeRate.totalAmountInvoicedCurrency,
            agreedExchangeRate = Some(invoiceAndExchangeRate.agreedExchangeRate),
            totalPackage = declaration.totalNumberOfItems.flatMap(_.totalPackage),
            exchangeRate =
              if (invoiceAndExchangeRate.agreedExchangeRate.toLowerCase == "no") None
              else invoiceAndExchangeRate.exchangeRate
          )
        )
      )
    }
}
