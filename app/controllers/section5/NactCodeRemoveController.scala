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
import controllers.section5.routes.NactCodeSummaryController
import controllers.declaration.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section5.nactCode.nact_code_remove

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NactCodeRemoveController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  nactCodeRemove: nact_code_remove
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val validTypes = journeyType(nonClearanceJourneys)

  def displayPage(itemId: String, code: String): Action[AnyContent] = (authenticate andThen validTypes).async { implicit request =>
    checkIfNactExists(itemId: String, code) {
      Future.successful(Ok(nactCodeRemove(itemId, code, removeYesNoForm.withSubmissionErrors)))
    }
  }

  def submitForm(itemId: String, code: String): Action[AnyContent] = (authenticate andThen validTypes).async { implicit request =>
    checkIfNactExists(itemId: String, code) {
      removeYesNoForm
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(nactCodeRemove(itemId, code, formWithErrors))),
          _.answer match {
            case YesNoAnswers.yes => updateExportsCache(itemId, code).map(_ => nextPage(itemId))
            case YesNoAnswers.no  => Future.successful(nextPage(itemId))
          }
        )
    }
  }

  private def checkIfNactExists(itemId: String, code: String)(action: => Future[Result])(implicit request: JourneyRequest[_]): Future[Result] =
    request.cacheModel
      .itemBy(itemId)
      .flatMap(_.nactCodes.find(_.exists(_.nactCode == code)))
      .fold(Future.successful(Redirect(NactCodeSummaryController.displayPage(itemId))))(_ => action)

  private def nextPage(itemId: String)(implicit request: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(NactCodeSummaryController.displayPage(itemId))

  private def removeYesNoForm: Form[YesNoAnswer] =
    YesNoAnswer.form(errorKey = "declaration.nationalAdditionalCode.remove.answer.empty")

  private def updateExportsCache(itemId: String, nactCodeToRemove: String)(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] = {
    val updatedCodes = request.cacheModel.itemBy(itemId).flatMap(_.nactCodes.map(_.filterNot(_.nactCode == nactCodeToRemove)))
    updateDeclarationFromRequest(model => model.updatedItem(itemId, _.copy(nactCodes = updatedCodes)))
  }
}
