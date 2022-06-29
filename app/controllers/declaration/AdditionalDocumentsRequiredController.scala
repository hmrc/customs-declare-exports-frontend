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

import scala.concurrent.{ExecutionContext, Future}
import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.{form, YesNoAnswers}

import javax.inject.Inject
import models.declaration.AdditionalDocuments
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalDocuments.additional_documents_required

class AdditionalDocumentsRequiredController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalDocumentsRequired: additional_documents_required
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  private val emptyKey = "declaration.additionalDocumentsRequired.empty"

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form(errorKey = emptyKey).withSubmissionErrors()
    request.cacheModel.additionalDocumentsRequired(itemId) match {
      case Some(yesNoAnswer) => Ok(additionalDocumentsRequired(mode, itemId, frm.fill(yesNoAnswer)))
      case _                 => Ok(additionalDocumentsRequired(mode, itemId, frm))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form(errorKey = emptyKey)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalDocumentsRequired(mode, itemId, formWithErrors))),
        yesNoAnswer => updateCache(yesNoAnswer, itemId).map(_ => navigator.continueTo(mode, nextPage(yesNoAnswer, itemId)))
      )
  }

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String): Mode => Call =
    if (yesNoAnswer.answer == YesNoAnswers.yes) routes.AdditionalDocumentAddController.displayPage(_, itemId)
    else routes.ItemsSummaryController.displayItemsSummaryPage(_)

  private def updateCache(yesNoAnswer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] = {
    val documents =
      if (yesNoAnswer.answer == YesNoAnswers.no) Seq.empty else request.cacheModel.listOfAdditionalDocuments(itemId)

    val additionalDocuments = AdditionalDocuments(Some(yesNoAnswer), documents)
    updateDeclarationFromRequest(_.updatedItem(itemId, _.copy(additionalDocuments = Some(additionalDocuments))))
  }
}
