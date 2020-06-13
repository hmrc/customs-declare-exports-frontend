/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.declaration.additionaldocuments.DocumentsProduced.form
import javax.inject.Inject
import models.Mode
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.documentsProduced.documents_produced

import scala.concurrent.ExecutionContext

class DocumentsProducedController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  documentProducedPage: documents_produced
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form().withSubmissionErrors()
    cachedDocuments(itemId) match {
      case documents if documents.nonEmpty => Ok(documentProducedPage(mode, itemId, frm, documents))
      case _                               => navigator.continueTo(mode, controllers.declaration.routes.DocumentsProducedAddController.displayPage(_, itemId))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    YesNoAnswer
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) => BadRequest(documentProducedPage(mode, itemId, formWithErrors, cachedDocuments(itemId))),
        validYesNo =>
          validYesNo.answer match {
            case YesNoAnswers.yes => navigator.continueTo(mode, controllers.declaration.routes.DocumentsProducedAddController.displayPage(_, itemId))
            case YesNoAnswers.no  => navigator.continueTo(mode, routes.ItemsSummaryController.displayPage)
        }
      )
  }

  private def cachedDocuments(itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.itemBy(itemId).flatMap(_.documentsProducedData).map(_.documents).getOrElse(Seq.empty)

}
