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
import controllers.navigation.Navigator
import forms.declaration.CusCode
import forms.declaration.CusCode.form
import models.DeclarationType.DeclarationType
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.cus_code

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CusCodeController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  cusCodePage: cus_code
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  val validTypes = Seq(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL)

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    val frm = form().withSubmissionErrors()
    request.cacheModel.itemBy(itemId).flatMap(_.cusCode) match {
      case Some(cusCode) => Ok(cusCodePage(mode, itemId, frm.fill(cusCode)))
      case _             => Ok(cusCodePage(mode, itemId, frm))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(cusCodePage(mode, itemId, formWithErrors))),
        validForm =>
          updateExportsCache(itemId, validForm).map { _ =>
            navigator.continueTo(mode, nextPage(itemId, request.declarationType))
        }
      )
  }

  private def updateExportsCache(itemId: String, updatedItem: CusCode)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      model.updatedItem(itemId, item => item.copy(cusCode = Some(updatedItem)))
    }

  def nextPage(itemId: String, declarationType: DeclarationType): Mode => Call =
    if (declarationType == DeclarationType.CLEARANCE) {
      controllers.declaration.routes.NactCodeSummaryController.displayPage(_, itemId)
    } else {
      controllers.declaration.routes.TaricCodeSummaryController.displayPage(_, itemId)
    }
}
