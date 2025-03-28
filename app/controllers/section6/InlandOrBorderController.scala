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

package controllers.section6

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.helpers.InlandOrBorderHelper
import controllers.helpers.TransportSectionHelper._
import controllers.navigation.Navigator
import controllers.general.routes.RootController
import controllers.section6.routes._
import forms.section6.InlandOrBorder.{form, Border, Inland}
import forms.section6.{BorderTransport, InlandOrBorder}
import models.DeclarationType.SUPPLEMENTARY
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section6.inland_border

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
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val actionBuilder = authenticate andThen journeyAction.onAdditionalTypes(additionalDeclTypesAllowedOnInlandOrBorder)

  def displayPage: Action[AnyContent] = actionBuilder { implicit request =>
    if (inlandOrBorderHelper.skipInlandOrBorder(request.cacheModel)) Redirect(RootController.displayPage)
    else {
      val frm = form.withSubmissionErrors
      request.cacheModel.locations.inlandOrBorder match {
        case Some(location) => Ok(inlandOrBorderPage(frm.fill(location)))
        case _              => Ok(inlandOrBorderPage(frm))
      }
    }
  }

  def submitPage(): Action[AnyContent] = actionBuilder.async { implicit request =>
    if (inlandOrBorderHelper.skipInlandOrBorder(request.cacheModel)) Future.successful(Redirect(RootController.displayPage))
    else
      form
        .bindFromRequest()
        .fold(formWithErrors => Future.successful(BadRequest(inlandOrBorderPage(formWithErrors))), updateExportsCache(_))
  }

  private def updateExportsCache(inlandOrBorder: InlandOrBorder)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateDeclarationFromRequest { declaration =>
      val declarationAfter = clearCacheOnSkippingTransportPages(
        declaration.copy(locations =
          declaration.locations.copy(
            inlandOrBorder = Some(inlandOrBorder),
            inlandModeOfTransportCode = if (inlandOrBorder == Border) None else declaration.locations.inlandModeOfTransportCode
          )
        )
      )
      if (inlandOrBorder == Border) declarationAfter.updateBorderTransport(BorderTransport("", "")) else declarationAfter
    }
      .map(declaration => navigator.continueTo(nextPage(declaration, inlandOrBorder)))

  private def nextPage(declaration: ExportsDeclaration, inlandOrBorder: InlandOrBorder): Call =
    inlandOrBorder match {
      case Border if skipTransportPages(declaration) =>
        if (declaration.isType(SUPPLEMENTARY)) ContainerController.displayContainerSummary
        else ExpressConsignmentController.displayPage

      case Border => DepartureTransportController.displayPage
      case Inland => InlandTransportDetailsController.displayPage
    }
}
