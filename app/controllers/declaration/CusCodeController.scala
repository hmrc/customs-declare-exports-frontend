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
import controllers.declaration.routes.ZeroRatedForVatController
import controllers.navigation.Navigator
import forms.declaration.CusCode
import forms.declaration.CusCode.form
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.cus_code

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CusCodeController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  cusCodePage: cus_code
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyAction(nonClearanceJourneys)) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.itemBy(itemId).flatMap(_.cusCode) match {
      case Some(cusCode) => Ok(cusCodePage(itemId, frm.fill(cusCode)))
      case _             => Ok(cusCodePage(itemId, frm))
    }
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyAction(nonClearanceJourneys)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(cusCodePage(itemId, formWithErrors))),
        updateExportsCache(itemId, _).map(_ => navigator.continueTo(ZeroRatedForVatController.displayPage(itemId)))
      )
  }

  private def updateExportsCache(itemId: String, cusCode: CusCode)(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updatedItem(itemId, item => item.copy(cusCode = Some(cusCode))))
}
