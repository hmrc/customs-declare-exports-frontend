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
import controllers.declaration.routes.{
  DepartureTransportController,
  ExpressConsignmentController,
  InlandTransportDetailsController,
  TransportContainerController
}
import controllers.helpers.InlandOrBorderHelper
import controllers.helpers.TransportSectionHelper.{additionalDeclTypesAllowedOnInlandOrBorder, isPostalOrFTIModeOfTransport}
import controllers.navigation.Navigator
import controllers.routes.RootController
import forms.declaration.InlandOrBorder
import forms.declaration.InlandOrBorder.{form, Border, Inland}
import models.DeclarationType.SUPPLEMENTARY
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.inland_border

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class InlandOrBorderController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  inlandOrBorderPage: inland_border,
  inlandOrBorderHelper: InlandOrBorderHelper
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  private val actionBuilder = authenticate andThen journeyAction.onAdditionalTypes(additionalDeclTypesAllowedOnInlandOrBorder)

  def displayPage(mode: Mode): Action[AnyContent] = actionBuilder { implicit request =>
    if (inlandOrBorderHelper.skipInlandOrBorder(request.cacheModel)) Results.Redirect(RootController.displayPage)
    else {
      val frm = form.withSubmissionErrors
      request.cacheModel.locations.inlandOrBorder match {
        case Some(location) => Ok(inlandOrBorderPage(mode, frm.fill(location)))
        case _              => Ok(inlandOrBorderPage(mode, frm))
      }
    }
  }

  def submitPage(mode: Mode): Action[AnyContent] = actionBuilder.async { implicit request =>
    if (inlandOrBorderHelper.skipInlandOrBorder(request.cacheModel)) Future.successful(Results.Redirect(RootController.displayPage))
    else
      form.bindFromRequest
        .fold(formWithErrors => Future.successful(BadRequest(inlandOrBorderPage(mode, formWithErrors))), updateExportsCache(mode, _))
  }

  private def nextPage(declaration: ExportsDeclaration, inlandOrBorder: InlandOrBorder): Mode => Call =
    inlandOrBorder match {
      case Border if isPostalOrFTIModeOfTransport(declaration.transportLeavingBorderCode) =>
        if (declaration.isType(SUPPLEMENTARY)) TransportContainerController.displayContainerSummary
        else ExpressConsignmentController.displayPage

      case Border => DepartureTransportController.displayPage
      case Inland => InlandTransportDetailsController.displayPage
    }

  private def updateExportsCache(mode: Mode, inlandOrBorder: InlandOrBorder)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateDeclarationFromRequest { declaration =>
      declaration.copy(locations =
        declaration.locations.copy(
          inlandOrBorder = Some(inlandOrBorder),
          inlandModeOfTransportCode = if (inlandOrBorder == Border) None else declaration.locations.inlandModeOfTransportCode
        )
      )
    } map { _ =>
      navigator.continueTo(mode, nextPage(request.cacheModel, inlandOrBorder))
    }
}
