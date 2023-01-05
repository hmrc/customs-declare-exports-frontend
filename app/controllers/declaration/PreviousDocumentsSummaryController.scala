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
import controllers.declaration.routes.ItemsSummaryController
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.previousDocuments.previous_documents_summary

import javax.inject.Inject

class PreviousDocumentsSummaryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  previousDocumentsSummary: previous_documents_summary
) extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val form = yesNoForm.withSubmissionErrors
    request.cacheModel.previousDocuments.map(_.documents) match {
      case Some(documents) if documents.nonEmpty => Ok(previousDocumentsSummary(form, documents))

      case _ => navigator.continueTo(routes.PreviousDocumentsController.displayPage)
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    yesNoForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          val previousDocuments = request.cacheModel.previousDocuments.map(_.documents).getOrElse(Seq.empty)
          BadRequest(previousDocumentsSummary(formWithErrors, previousDocuments))
        },
        _.answer match {
          case YesNoAnswers.yes => navigator.continueTo(routes.PreviousDocumentsController.displayPage)
          case YesNoAnswers.no  => navigator.continueTo(ItemsSummaryController.displayAddItemPage)
        }
      )
  }

  private def yesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.previousDocuments.add.another.empty")
}
