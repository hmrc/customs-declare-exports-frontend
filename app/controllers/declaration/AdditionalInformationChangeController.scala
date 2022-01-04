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
import controllers.declaration.AdditionalInformationAddController.AdditionalInformationFormGroupId
import controllers.navigation.Navigator
import controllers.helpers._
import forms.declaration.AdditionalInformation
import forms.declaration.AdditionalInformation.form
import models.declaration.AdditionalInformationData
import models.declaration.AdditionalInformationData.maxNumberOfItems
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ListItem
import views.html.declaration.additionalInformation.additional_information_change

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationChangeController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  changePage: additional_information_change
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    findAdditionalInformation(itemId, id) match {
      case Some(document) => Ok(changePage(mode, itemId, id, form().fill(document).withSubmissionErrors()))
      case _              => returnToSummary(mode, itemId)
    }
  }

  def submitForm(mode: Mode, itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    findAdditionalInformation(itemId, id) match {
      case Some(existingInformation) =>
        val boundForm = form().bindFromRequest()
        boundForm.fold(
          formWithErrors => Future.successful(BadRequest(changePage(mode, itemId, id, formWithErrors))),
          updatedInformation => changeInformation(mode, itemId, id, existingInformation, updatedInformation, boundForm)
        )
      case _ => Future.successful(returnToSummary(mode, itemId))
    }
  }

  private def returnToSummary(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    navigator.continueTo(mode, routes.AdditionalInformationController.displayPage(_, itemId))

  private def findAdditionalInformation(itemId: String, id: String)(implicit request: JourneyRequest[AnyContent]): Option[AdditionalInformation] =
    ListItem.findById(id, request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation).map(_.items).getOrElse(Seq.empty))

  private def cachedAdditionalInformationData(itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation).getOrElse(AdditionalInformationData.default)

  private def changeInformation(
    mode: Mode,
    itemId: String,
    id: String,
    existingInformation: AdditionalInformation,
    newInformation: AdditionalInformation,
    boundForm: Form[AdditionalInformation]
  )(implicit request: JourneyRequest[AnyContent]): Future[Result] = {

    val cachedData = cachedAdditionalInformationData(itemId)
    val itemsWithoutExisting = cachedData.items.filterNot(_ == existingInformation)

    MultipleItemsHelper
      .add(boundForm, itemsWithoutExisting, maxNumberOfItems, AdditionalInformationFormGroupId, "declaration.additionalInformation")
      .fold(
        formWithErrors => Future.successful(BadRequest(changePage(mode, itemId, id, formWithErrors))),
        _ => {
          val updatedDataItems = cachedData.items.map(item => if (item == existingInformation) newInformation else item)
          updateCache(itemId, cachedData.copy(items = updatedDataItems))
            .map(_ => returnToSummary(mode, itemId))
        }
      )
  }

  private def updateCache(itemId: String, updatedData: AdditionalInformationData)(
    implicit req: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      model.updatedItem(itemId, item => item.copy(additionalInformation = Some(updatedData)))
    })
}
