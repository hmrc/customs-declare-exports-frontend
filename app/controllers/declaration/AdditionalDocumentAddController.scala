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
import controllers.navigation.{ItemId, Navigator}
import controllers.helpers._
import forms.declaration.additionaldocuments.AdditionalDocument
import forms.declaration.additionaldocuments.AdditionalDocument._

import javax.inject.Inject
import models.declaration.AdditionalDocuments
import models.declaration.AdditionalDocuments.maxNumberOfItems
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalDocuments.additional_document_add

class AdditionalDocumentAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalDocumentAddPage: additional_document_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    resolveBackLink(mode, itemId).map { backLink =>
      Ok(additionalDocumentAddPage(mode, itemId, form(request.cacheModel).withSubmissionErrors(), backLink))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = globalErrors(form(request.cacheModel).bindFromRequest())

    boundForm.fold(formWithErrors => {
      resolveBackLink(mode, itemId).map { backLink =>
        BadRequest(additionalDocumentAddPage(mode, itemId, formWithErrors, backLink))
      }
    }, documents => {
      val additionalDocuments = request.cacheModel.additionalDocuments(itemId)
      if (documents.isDefined) saveDocuments(mode, itemId, boundForm, additionalDocuments)
      else continue(mode, itemId, additionalDocuments)
    })
  }

  private def continue(mode: Mode, itemId: String, additionalDocuments: AdditionalDocuments)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    updateCache(itemId, additionalDocuments)
      .map(
        _ =>
          if (additionalDocuments.documents.isEmpty)
            navigator.continueTo(mode, routes.ItemsSummaryController.displayItemsSummaryPage)
          else
            navigator.continueTo(mode, routes.AdditionalDocumentsController.displayPage(_, itemId))
      )

  private def saveDocuments(mode: Mode, itemId: String, boundForm: Form[AdditionalDocument], additionalDocuments: AdditionalDocuments)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, additionalDocuments.documents, maxNumberOfItems, AdditionalDocumentFormGroupId, "declaration.additionalDocument")
      .fold(
        formWithErrors =>
          resolveBackLink(mode, itemId).map { backLink =>
            BadRequest(additionalDocumentAddPage(mode, itemId, formWithErrors, backLink))
        },
        documents =>
          updateCache(itemId, additionalDocuments.copy(documents = documents))
            .map(_ => navigator.continueTo(mode, routes.AdditionalDocumentsController.displayPage(_, itemId)))
      )

  private def updateCache(itemId: String, additionalDocuments: AdditionalDocuments)(
    implicit req: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      model.updatedItem(itemId, item => item.copy(additionalDocuments = Some(additionalDocuments)))
    })

  private def resolveBackLink(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[Call] =
    navigator.backLink(AdditionalDocument, mode, ItemId(itemId))
}
