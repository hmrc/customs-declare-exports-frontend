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

package controllers.section5

import controllers.actions.{AuthAction, JourneyAction}
import controllers.section5.routes.AdditionalInformationRequiredController
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import forms.section5.commodityMeasure.SupplementaryUnits
import forms.section5.commodityMeasure.SupplementaryUnits.form
import models.DeclarationType.{STANDARD, SUPPLEMENTARY}
import models.declaration.{CommodityMeasure, ExportItem}
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section5.commodityMeasure.supplementary_units_yes_no

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SupplementaryUnitsController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  supplementaryUnitsYesNoPage: supplementary_units_yes_no
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val validTypes = Seq(STANDARD, SUPPLEMENTARY)

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    val formWithDataIfAny =
      request.cacheModel.commodityMeasure(itemId) match {
        case Some(commodityMeasure) if hasSupplementaryUnits(commodityMeasure) =>
          form.withSubmissionErrors.fill(SupplementaryUnits(commodityMeasure))

        case _ => form.withSubmissionErrors
      }

    Future.successful(Ok(supplementaryUnitsYesNoPage(itemId, formWithDataIfAny)))
  }

  def submitPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(supplementaryUnitsYesNoPage(itemId, formWithErrors))),
        updateExportsCacheAndContinueToNextPage(itemId, _)
      )
  }

  private def hasSupplementaryUnits(commodityMeasure: CommodityMeasure): Boolean =
    commodityMeasure.supplementaryUnits.isDefined || commodityMeasure.supplementaryUnitsNotRequired.isDefined

  private def updateExportsCacheAndContinueToNextPage(itemId: String, newUnits: SupplementaryUnits)(
    implicit r: JourneyRequest[AnyContent]
  ): Future[Result] =
    updateDeclarationFromRequest {
      _.updatedItem(itemId, item => item.copy(commodityMeasure = updateCommodityMeasure(item, newUnits)))
    }.map { _ =>
      navigator.continueTo(AdditionalInformationRequiredController.displayPage(itemId))
    }

  private def updateCommodityMeasure(item: ExportItem, newUnits: SupplementaryUnits): Option[CommodityMeasure] = {
    val notRequired = newUnits.supplementaryUnits.fold(Some(true))(_ => Some(false))
    item.commodityMeasure match {
      case Some(commodityMeasure) =>
        Some(CommodityMeasure(newUnits.supplementaryUnits, notRequired, commodityMeasure.grossMass, commodityMeasure.netMass))

      case _ => Some(CommodityMeasure(newUnits.supplementaryUnits, notRequired, None, None))
    }
  }
}
