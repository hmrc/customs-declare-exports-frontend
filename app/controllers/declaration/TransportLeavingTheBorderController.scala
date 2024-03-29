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
import controllers.declaration.routes.WarehouseIdentificationController
import controllers.helpers.TransportSectionHelper.{clearCacheOnSkippingTransportPages, isPostalOrFTIModeOfTransport}
import controllers.helpers.{InlandOrBorderHelper, SupervisingCustomsOfficeHelper}
import controllers.navigation.Navigator
import forms.declaration.ModeOfTransportCode.RoRo
import forms.declaration.TransportLeavingTheBorder
import forms.declaration.TransportLeavingTheBorder.form
import models.DeclarationType.CLEARANCE
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.transport_leaving_the_border

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransportLeavingTheBorderController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  transportAtBorder: transport_leaving_the_border,
  inlandOrBorderHelper: InlandOrBorderHelper,
  supervisingCustomsOfficeHelper: SupervisingCustomsOfficeHelper
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.transport.borderModeOfTransportCode match {
      case Some(data) => Ok(transportAtBorder(form.withSubmissionErrors.fill(data)))
      case _          => Ok(transportAtBorder(form.withSubmissionErrors))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form.withSubmissionErrors
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(transportAtBorder(formWithErrors))),
        updateCache(_).map(declaration => navigator.continueTo(nextPage(declaration)))
      )
  }

  private def nextPage(declaration: ExportsDeclaration): Call =
    if (declaration.isType(CLEARANCE) || declaration.requiresWarehouseId) WarehouseIdentificationController.displayPage
    else supervisingCustomsOfficeHelper.landOnOrSkipToNextPage(declaration)

  private def updateCache(code: TransportLeavingTheBorder)(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      val transportCrossingTheBorderNationality =
        if (isPostalOrFTIModeOfTransport(code.code)) None else declaration.transport.transportCrossingTheBorderNationality

      clearCacheOnSkippingTransportPages(
        declaration.copy(
          transport = declaration.transport
            .copy(borderModeOfTransportCode = Some(code), transportCrossingTheBorderNationality = transportCrossingTheBorderNationality),
          locations = declaration.locations.copy(inlandOrBorder =
            if (code.code == Some(RoRo)) None
            else inlandOrBorderHelper.resetInlandOrBorderIfRequired(declaration)
          )
        )
      )
    }
}
