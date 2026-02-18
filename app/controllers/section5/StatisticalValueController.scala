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

package controllers.section5

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.section5.routes.PackageInformationSummaryController
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.general.routes.RootController
import forms.section5.StatisticalValue
import forms.section5.StatisticalValue.{form, formOptional}
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section5.statistical_value

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StatisticalValueController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  itemTypePage: statistical_value
)(implicit ec: ExecutionContext, auditService: AuditService, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val validJourneys = nonClearanceJourneys

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validJourneys)) { implicit request =>
    if (redirectToRoot(itemId)) Redirect(RootController.displayPage)
    else
      request.cacheModel.itemBy(itemId).flatMap(_.statisticalValue) match {
        case Some(itemType) => Ok(itemTypePage(itemId, form.withSubmissionErrors.fill(itemType)))
        case _              => Ok(itemTypePage(itemId, form.withSubmissionErrors))
      }
  }

  def submitItemType(itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validJourneys)).async { implicit request =>
    if (redirectToRoot(itemId)) Future.successful(Redirect(RootController.displayPage))
    else {
      if (!is3NS(itemId) && appConfig.isOptionalFieldsEnabled)
        form
          .bindFromRequest()
          .fold(
            (formWithErrors: Form[StatisticalValue]) => Future.successful(BadRequest(itemTypePage(itemId, formWithErrors))),
            statisticalValue => {
              val statisticalValueOption = Some(statisticalValue)
              updateExportsCache(itemId, statisticalValueOption)
                .map(_ => navigator.continueTo(PackageInformationSummaryController.displayPage(itemId)))
            }
          )
      else
        formOptional
          .bindFromRequest()
          .fold(
            (formWithErrors: Form[StatisticalValue]) => Future.successful(BadRequest(itemTypePage(itemId, formWithErrors))),
            statisticalValue => {
              val statisticalValueOption = if (statisticalValue.statisticalValue.trim.isEmpty) None else Some(statisticalValue)
              updateExportsCache(itemId, statisticalValueOption)
                .map(_ => navigator.continueTo(PackageInformationSummaryController.displayPage(itemId)))
            }
          )

    }
  }

  private def redirectToRoot(itemId: String)(implicit request: JourneyRequest[_]): Boolean =
    occasionalAndSimplified.contains(request.declarationType) && !request.cacheModel.isLowValueDeclaration(itemId)

  private def is3NS(itemId: String)(implicit request: JourneyRequest[_]): Boolean = {
    val item = request.cacheModel.itemBy(itemId)

    val is3NS: Boolean =
      item.exists(value => value.procedureCodes.exists(codes => codes.additionalProcedureCodes.contains("3NS")))
    is3NS
  }

  private def updateExportsCache(itemId: String, updatedItem: Option[StatisticalValue])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updatedItem(itemId, item => item.copy(statisticalValue = updatedItem)))
}
