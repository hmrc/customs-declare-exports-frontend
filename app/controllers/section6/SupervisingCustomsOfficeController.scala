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

package controllers.section6

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.{ModelCacheable, SubmissionErrors}
import controllers.helpers.{InlandOrBorderHelper, SupervisingCustomsOfficeHelper}
import controllers.navigation.Navigator
import forms.section6.SupervisingCustomsOffice
import forms.section6.SupervisingCustomsOffice.{fieldId, form}
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.validators.forms.AutoCompleteFieldBinding
import views.html.section6.supervising_customs_office

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SupervisingCustomsOfficeController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  supervisingCustomsOfficePage: supervising_customs_office,
  inlandOrBorderHelper: InlandOrBorderHelper,
  supervisingCustomsOfficeHelper: SupervisingCustomsOfficeHelper
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with AutoCompleteFieldBinding with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.locations.supervisingCustomsOffice match {
      case Some(data) => Ok(supervisingCustomsOfficePage(frm.fill(data)))
      case _          => Ok(supervisingCustomsOfficePage(frm))
    }
  }

  val submit: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest(formValuesFromRequest(fieldId))
      .fold(
        formWithErrors => Future.successful(BadRequest(supervisingCustomsOfficePage(formWithErrors))),
        updateCache(_).map(declaration => navigator.continueTo(supervisingCustomsOfficeHelper.nextPage(declaration)))
      )
  }

  private def updateCache(formData: SupervisingCustomsOffice)(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      declaration.copy(locations =
        declaration.locations
          .copy(supervisingCustomsOffice = Some(formData), inlandOrBorder = inlandOrBorderHelper.resetInlandOrBorderIfRequired(declaration))
      )
    }
}
