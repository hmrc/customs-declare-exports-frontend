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
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section5.routes.{AdditionalDocumentAddController, ItemsSummaryController}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.{form, YesNoAnswers}
import models.ExportsDeclaration
import models.declaration.AdditionalDocuments
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section5.additionalDocuments.additional_documents_required

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalDocumentsRequiredController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalDocumentsRequired: additional_documents_required
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val emptyKey = "declaration.additionalDocumentsRequired.empty"

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form(errorKey = emptyKey).withSubmissionErrors
    request.cacheModel.additionalDocumentsRequired(itemId) match {
      case Some(yesNoAnswer) => Ok(additionalDocumentsRequired(itemId, frm.fill(yesNoAnswer)))
      case _                 => Ok(additionalDocumentsRequired(itemId, frm))
    }
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form(errorKey = emptyKey)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalDocumentsRequired(itemId, formWithErrors))),
        yesNoAnswer => updateCache(yesNoAnswer, itemId).map(_ => navigator.continueTo(nextPage(yesNoAnswer, itemId)))
      )
  }

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String): Call =
    if (yesNoAnswer.answer == YesNoAnswers.yes) AdditionalDocumentAddController.displayPage(itemId)
    else ItemsSummaryController.displayItemsSummaryPage

  private def updateCache(yesNoAnswer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] = {
    val documents =
      if (yesNoAnswer.answer == YesNoAnswers.no) Seq.empty else request.cacheModel.listOfAdditionalDocuments(itemId)

    val additionalDocuments = AdditionalDocuments(Some(yesNoAnswer), documents)
    updateDeclarationFromRequest(_.updatedItem(itemId, _.copy(additionalDocuments = Some(additionalDocuments))))
  }
}
