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
import controllers.declaration.routes.{InvoiceAndExchangeRateController, TotalPackageQuantityController}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers.{no, yes}
import forms.common.YesNoAnswer.form
import models.declaration.InvoiceAndPackageTotals
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.invoice_and_exchange_rate_choice

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class InvoiceAndExchangeRateChoiceController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  invoiceAndExchangeRateChoicePage: invoice_and_exchange_rate_choice,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  private val validTypes = Seq(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY)

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    val frm = form(errorKey = "declaration.invoice.amount.choice.answer.empty").withSubmissionErrors

    val declaration = request.cacheModel

    if (declaration.isInvoiceAmountGreaterThan100000) Ok(invoiceAndExchangeRateChoicePage(frm.fill(YesNoAnswer(no))))
    else if (declaration.totalNumberOfItems.isDefined) Ok(invoiceAndExchangeRateChoicePage(frm.fill(YesNoAnswer(yes))))
    else Ok(invoiceAndExchangeRateChoicePage(frm))
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    form(errorKey = "declaration.invoice.amount.choice.answer.empty")
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(invoiceAndExchangeRateChoicePage(formWithErrors))),
        yesNoAnswer =>
          if (yesNoAnswer.answer == no) Future.successful(navigator.continueTo(InvoiceAndExchangeRateController.displayPage))
          else resetCachedInvoiceData.map(_ => navigator.continueTo(TotalPackageQuantityController.displayPage))
      )
  }

  private def resetCachedInvoiceData(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      declaration.copy(totalNumberOfItems =
        Some(
          InvoiceAndPackageTotals(
            totalAmountInvoiced = None,
            totalAmountInvoicedCurrency = None,
            agreedExchangeRate = None,
            exchangeRate = None,
            totalPackage = declaration.totalNumberOfItems.flatMap(_.totalPackage)
          )
        )
      )
    }
}
