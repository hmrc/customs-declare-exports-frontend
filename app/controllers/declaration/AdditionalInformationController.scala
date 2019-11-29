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
import controllers.navigation.Navigator
import controllers.util.MultipleItemsHelper.remove
import controllers.util._
import forms.declaration.AdditionalInformation
import forms.declaration.AdditionalInformation.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.AdditionalInformationData
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
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
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalInformationPage: additional_information
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  val elementLimit = 99

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation) match {
      case Some(data) => Ok(additionalInformationPage(mode, itemId, form(), data.items))
      case _          => Ok(additionalInformationPage(mode, itemId, form(), Seq()))
    }
  }

  def saveAdditionalInfo(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    val actionTypeOpt = FormAction.bindFromRequest()

    val cache = request.cacheModel
      .itemBy(itemId)
      .flatMap(_.additionalInformation)
      .getOrElse(AdditionalInformationData(Seq()))

    actionTypeOpt match {
      case Add                             => handleAdd(mode, itemId, boundForm, cache.items)
      case Remove(ids)                     => handleRemove(mode, itemId, ids, boundForm, cache.items)
      case SaveAndContinue | SaveAndReturn => handleSaveAndContinue(mode, itemId, boundForm, cache.items)
      case _                               => errorHandler.displayErrorPage()
    }
  }

  private def handleAdd(mode: Mode, itemId: String, boundForm: Form[AdditionalInformation], cachedData: Seq[AdditionalInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, elementLimit)
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalInformationPage(mode, itemId, formWithErrors, cachedData))),
        updatedCache =>
          updateCache(itemId, AdditionalInformationData(updatedCache))
            .map(_ => navigator.continueTo(controllers.declaration.routes.AdditionalInformationController.displayPage(mode, itemId)))
      )

  private def handleSaveAndContinue(mode: Mode, itemId: String, boundForm: Form[AdditionalInformation], cachedData: Seq[AdditionalInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .saveAndContinue(boundForm, cachedData, isMandatory = false, elementLimit)
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalInformationPage(mode: Mode, itemId, formWithErrors, cachedData))),
        updatedCache =>
          if (updatedCache != cachedData)
            updateCache(itemId, AdditionalInformationData(updatedCache))
              .map(
                _ =>
                  navigator
                    .continueTo(controllers.declaration.routes.DocumentsProducedController.displayPage(mode, itemId))
              )
          else
            Future.successful(navigator.continueTo(controllers.declaration.routes.DocumentsProducedController.displayPage(mode, itemId)))
      )

  private def handleRemove(mode: Mode, itemId: String, ids: Seq[String], boundForm: Form[AdditionalInformation], items: Seq[AdditionalInformation])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {
    val updatedCache = remove(items, (addItem: AdditionalInformation) => addItem.toString == ids.head)
    updateCache(itemId, AdditionalInformationData(updatedCache))
      .map(_ => navigator.continueTo(controllers.declaration.routes.AdditionalInformationController.displayPage(mode, itemId)))
  }

  private def updateCache(itemId: String, updatedAdditionalInformation: AdditionalInformationData)(
    implicit r: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => model.updatedItem(itemId, _.copy(additionalInformation = Some(updatedAdditionalInformation))))
}
