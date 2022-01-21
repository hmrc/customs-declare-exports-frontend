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
import controllers.navigation.{ItemId, Navigator}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.TaricCode
import models.Mode
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.taric_codes

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaricCodeSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  taricCodesPage: taric_codes
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.taricCodes) match {
      case Some(taricCodes) if taricCodes.nonEmpty =>
        resolveBackLink(mode, itemId).map { backLink =>
          Ok(taricCodesPage(mode, itemId, addYesNoForm.withSubmissionErrors(), taricCodes, backLink))
        }
      case _ => Future.successful(navigator.continueTo(mode, routes.TaricCodeAddController.displayPage(_, itemId)))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val taricCodes = request.cacheModel.itemBy(itemId).flatMap(_.taricCodes).getOrElse(List.empty)
    addYesNoForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) =>
          resolveBackLink(mode, itemId).map { backLink =>
            BadRequest(taricCodesPage(mode, itemId, formWithErrors, taricCodes, backLink))
        },
        validYesNo =>
          validYesNo.answer match {
            case YesNoAnswers.yes =>
              Future.successful(navigator.continueTo(mode, controllers.declaration.routes.TaricCodeAddController.displayPage(_, itemId)))
            case YesNoAnswers.no =>
              Future.successful(navigator.continueTo(mode, controllers.declaration.routes.NactCodeSummaryController.displayPage(_, itemId)))
        }
      )
  }

  private def resolveBackLink(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[Call] =
    navigator.backLink(TaricCode, mode, ItemId(itemId))

  private def addYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.taricAdditionalCodes.add.answer.empty")
}
