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
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.nact_code_remove

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NactCodeRemoveController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  nactCodeRemove: nact_code_remove
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  val validTypes = Seq(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL)

  def displayPage(mode: Mode, itemId: String, code: String): Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    Ok(nactCodeRemove(mode, itemId, code, removeYesNoForm.withSubmissionErrors()))
  }

  def submitForm(mode: Mode, itemId: String, code: String): Action[AnyContent] =
    (authenticate andThen journeyType(validTypes)).async { implicit request =>
      removeYesNoForm
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[YesNoAnswer]) => Future.successful(BadRequest(nactCodeRemove(mode, itemId, code, formWithErrors))),
          formData => {
            formData.answer match {
              case YesNoAnswers.yes =>
                updateExportsCache(itemId, code)
                  .map(_ => navigator.continueTo(mode, routes.NactCodeSummaryController.displayPage(_, itemId)))
              case YesNoAnswers.no =>
                Future.successful(navigator.continueTo(mode, routes.NactCodeSummaryController.displayPage(_, itemId)))
            }
          }
        )
    }

  private def removeYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.nationalAdditionalCode.remove.answer.empty")

  private def updateExportsCache(itemId: String, nactCodeToRemove: String)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] = {
    val updatedCodes = request.cacheModel.itemBy(itemId).flatMap(_.nactCodes).getOrElse(Seq.empty).filterNot(_.nactCode == nactCodeToRemove)
    updateDeclarationFromRequest(model => model.updatedItem(itemId, _.copy(nactCodes = Some(updatedCodes.toList))))
  }
}
