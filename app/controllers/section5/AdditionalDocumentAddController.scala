/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.helpers.MultipleItemsHelper
import controllers.navigation.Navigator
import controllers.section5.routes.{AdditionalDocumentsController, ItemsSummaryController}
import forms.common.YesNoAnswer.Yes
import forms.section5.additionaldocuments.AdditionalDocument
import forms.section5.additionaldocuments.AdditionalDocument._
import models.ExportsDeclaration
import models.declaration.AdditionalDocuments
import models.declaration.AdditionalDocuments.maxNumberOfItems
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import services.{TaggedAdditionalDocumentCodes, TaggedAuthCodes}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section5.additionalDocuments.additional_document_add

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalDocumentAddController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  taggedAuthCodes: TaggedAuthCodes,
  taggedAdditionalDocumentCodes: TaggedAdditionalDocumentCodes,
  additionalDocumentAddPage: additional_document_add
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  implicit val taggedAuthCodes2form: TaggedAuthCodes = taggedAuthCodes
  implicit val taggedAdditionalDocCodes2form: TaggedAdditionalDocumentCodes = taggedAdditionalDocumentCodes

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(additionalDocumentAddPage(itemId, form(request.cacheModel).withSubmissionErrors))
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = globalErrors(form(request.cacheModel).bindFromRequest())

    boundForm.fold(
      formWithErrors => Future.successful(BadRequest(additionalDocumentAddPage(itemId, formWithErrors))),
      document => {
        val documents = request.cacheModel.additionalDocumentsInformation(itemId)
        if (document.isDefined) saveDocuments(itemId, boundForm, documents)
        else continue(itemId, documents)
      }
    )
  }

  private def continue(itemId: String, documents: AdditionalDocuments)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateCache(itemId, documents).map { _ =>
      if (documents.documents.isEmpty)
        navigator.continueTo(ItemsSummaryController.displayItemsSummaryPage)
      else
        navigator.continueTo(AdditionalDocumentsController.displayPage(itemId))
    }

  private def saveDocuments(itemId: String, boundForm: Form[AdditionalDocument], documents: AdditionalDocuments)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, documents.documents, maxNumberOfItems, AdditionalDocumentFormGroupId, "declaration.additionalDocument")
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalDocumentAddPage(itemId, formWithErrors))),
        docs =>
          updateCache(itemId, documents.copy(documents = docs))
            .map(_ => navigator.continueTo(AdditionalDocumentsController.displayPage(itemId)))
      )

  private def updateCache(itemId: String, docs: AdditionalDocuments)(implicit r: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updatedItem(itemId, _.copy(additionalDocuments = Some(docs.copy(isRequired = Yes)))))
}
