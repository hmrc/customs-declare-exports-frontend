/*
 * Copyright 2019 HM Revenue & Customs
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
import controllers.util.MultipleItemsHelper.remove
import controllers.util._
import forms.declaration.AdditionalInformation
import forms.declaration.AdditionalInformation.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.ExportsDeclaration
import models.declaration.AdditionalInformationData
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.additional_information

import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  additionalInformationPage: additional_information
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  val elementLimit = 99

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation) match {
      case Some(data) => Ok(additionalInformationPage(itemId, form(), data.items))
      case _          => Ok(additionalInformationPage(itemId, form(), Seq()))
    }
  }

  def saveAdditionalInfo(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      val boundForm = form().bindFromRequest()
      val actionTypeOpt = FormAction.bindFromRequest()

      val cache = request.cacheModel
        .itemBy(itemId)
        .flatMap(_.additionalInformation)
        .getOrElse(AdditionalInformationData(Seq()))

      actionTypeOpt match {
        case Some(Add)             => handleAdd(itemId, boundForm, cache.items)
        case Some(Remove(ids))     => handleRemove(itemId, ids, boundForm, cache.items)
        case Some(SaveAndContinue) => handleSaveAndContinue(itemId, boundForm, cache.items)
        case _                     => errorHandler.displayErrorPage()
      }
  }

  private def handleAdd(itemId: String, boundForm: Form[AdditionalInformation], cachedData: Seq[AdditionalInformation])(
    implicit request: JourneyRequest[_]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, elementLimit)
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalInformationPage(itemId, formWithErrors, cachedData))),
        updatedCache =>
          updateCache(itemId, AdditionalInformationData(updatedCache))
            .map(_ => Redirect(controllers.declaration.routes.AdditionalInformationController.displayPage(itemId)))
      )

  private def handleSaveAndContinue(
    itemId: String,
    boundForm: Form[AdditionalInformation],
    cachedData: Seq[AdditionalInformation]
  )(implicit request: JourneyRequest[_]): Future[Result] =
    MultipleItemsHelper
      .saveAndContinue(boundForm, cachedData, true, elementLimit)
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalInformationPage(itemId, formWithErrors, cachedData))),
        updatedCache =>
          if (updatedCache != cachedData)
            updateCache(itemId, AdditionalInformationData(updatedCache))
              .map(_ => Redirect(controllers.declaration.routes.DocumentsProducedController.displayPage(itemId)))
          else
            Future.successful(Redirect(controllers.declaration.routes.DocumentsProducedController.displayPage(itemId)))
      )

  private def handleRemove(
    itemId: String,
    ids: Seq[String],
    boundForm: Form[AdditionalInformation],
    items: Seq[AdditionalInformation]
  )(implicit request: JourneyRequest[_]): Future[Result] = {
    val updatedCache = remove(items, (addItem: AdditionalInformation) => addItem.toString == ids.head)
    updateCache(itemId, AdditionalInformationData(updatedCache))
      .map(_ => Ok(additionalInformationPage(itemId, boundForm.discardingErrors, updatedCache)))
  }

  private def updateCache(itemId: String, updatedAdditionalInformation: AdditionalInformationData)(
    implicit r: JourneyRequest[_]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val itemList = model.items
        .find(item => item.id.equals(itemId))
        .map(_.copy(additionalInformation = Some(updatedAdditionalInformation)))
        .fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)

      model.copy(items = itemList)
    })
}
