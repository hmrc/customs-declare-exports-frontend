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
import controllers.declaration.routes.WarehouseIdentificationController
import controllers.helpers.SupervisingCustomsOfficeHelper
import controllers.navigation.Navigator
import forms.declaration.TransportLeavingTheBorder
import models.DeclarationType._
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.transport_leaving_the_border

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransportLeavingTheBorderController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  transportAtBorder: transport_leaving_the_border
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  private val validTypes = Seq(STANDARD, SUPPLEMENTARY, CLEARANCE)

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    val form = TransportLeavingTheBorder.form(request.declarationType).withSubmissionErrors
    request.cacheModel.transport.borderModeOfTransportCode match {
      case Some(data) => Ok(transportAtBorder(form.fill(data), mode))
      case _          => Ok(transportAtBorder(form, mode))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    TransportLeavingTheBorder
      .form(request.declarationType)
      .bindFromRequest
      .fold(errors => Future.successful(BadRequest(transportAtBorder(errors, mode))), updateCache(_).map(_ => navigator.continueTo(mode, nextPage)))
  }

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Mode => Call =
    if (request.isType(CLEARANCE) || request.cacheModel.requiresWarehouseId) WarehouseIdentificationController.displayPage
    else SupervisingCustomsOfficeHelper.landOnOrSkipToNextPage

  private def updateCache(code: TransportLeavingTheBorder)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updateTransportLeavingBorder(code))
}
