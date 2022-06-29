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
import controllers.helpers._
import controllers.navigation.Navigator
import forms.declaration.additionaldocuments.AdditionalDocument
import forms.declaration.additionaldocuments.AdditionalDocument._
import models.declaration.AdditionalDocuments
import models.declaration.AdditionalDocuments.maxNumberOfItems
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalDocuments.additional_document_add

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalDocumentAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalDocumentAddPage: additional_document_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(additionalDocumentAddPage(mode, itemId, form(request.cacheModel).withSubmissionErrors))
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = globalErrors(form(request.cacheModel).bindFromRequest)

    boundForm.fold(formWithErrors => Future.successful(BadRequest(additionalDocumentAddPage(mode, itemId, formWithErrors))), document => {
      val documents = request.cacheModel.additionalDocumentsInformation(itemId)
      if (document.isDefined) saveDocuments(mode, itemId, boundForm, documents)
      else continue(mode, itemId, documents)
    })
  }

  private def continue(mode: Mode, itemId: String, documents: AdditionalDocuments)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateCache(itemId, documents).map { _ =>
      if (documents.documents.isEmpty)
        navigator.continueTo(mode, routes.ItemsSummaryController.displayItemsSummaryPage)
      else
        navigator.continueTo(mode, routes.AdditionalDocumentsController.displayPage(_, itemId))
    }

  private def saveDocuments(mode: Mode, itemId: String, boundForm: Form[AdditionalDocument], documents: AdditionalDocuments)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, documents.documents, maxNumberOfItems, AdditionalDocumentFormGroupId, "declaration.additionalDocument")
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalDocumentAddPage(mode, itemId, formWithErrors))),
        docs =>
          updateCache(itemId, documents.copy(documents = docs))
            .map(_ => navigator.continueTo(mode, routes.AdditionalDocumentsController.displayPage(_, itemId)))
      )

  private def updateCache(itemId: String, docs: AdditionalDocuments)(implicit r: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updatedItem(itemId, _.copy(additionalDocuments = Some(docs))))
}
