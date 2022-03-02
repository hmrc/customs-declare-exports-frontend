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
import controllers.declaration.routes.TotalPackageQuantityController
import controllers.navigation.Navigator
import forms.declaration.TotalNumberOfItems
import forms.declaration.TotalNumberOfItems._
import models.declaration.Totals
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.total_number_of_items

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TotalNumberOfItemsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  totalNumberOfItemsPage: total_number_of_items,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  private val validTypes = Seq(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY)

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    request.cacheModel.totalNumberOfItems match {
      case Some(data) => Ok(totalNumberOfItemsPage(mode, form.withSubmissionErrors.fill(TotalNumberOfItems(data))))
      case _          => Ok(totalNumberOfItemsPage(mode, form.withSubmissionErrors))
    }
  }

  def saveNoOfItems(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    form.bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(totalNumberOfItemsPage(mode, formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(mode, TotalPackageQuantityController.displayPage))
      )
  }

  private def updateCache(totalNumberOfItems: TotalNumberOfItems)(implicit req: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      declaration.copy(
        totalNumberOfItems = Some(
          Totals(
            totalAmountInvoiced = totalNumberOfItems.totalAmountInvoiced,
            totalAmountInvoicedCurrency = totalNumberOfItems.totalAmountInvoicedCurrency,
            exchangeRate = totalNumberOfItems.exchangeRate,
            totalPackage = declaration.totalNumberOfItems.flatMap(_.totalPackage)
          )
        )
      )
    }
}
