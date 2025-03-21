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
import controllers.helpers._
import controllers.navigation.Navigator
import controllers.section5.AdditionalInformationAddController.AdditionalInformationFormGroupId
import controllers.section5.routes.AdditionalInformationController
import forms.section5.AdditionalInformation
import forms.section5.AdditionalInformation.form
import models.ExportsDeclaration
import models.declaration.AdditionalInformationData
import models.declaration.AdditionalInformationData.maxNumberOfItems
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ListItem
import views.html.section5.additionalInformation.additional_information_change

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationChangeController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  changePage: additional_information_change
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    findAdditionalInformation(itemId, id) match {
      case Some(document) => Ok(changePage(itemId, id, form.fill(document).withSubmissionErrors))
      case _              => returnToSummary(itemId)
    }
  }

  def submitForm(itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    findAdditionalInformation(itemId, id) match {
      case Some(existingInformation) =>
        val boundForm = form.bindFromRequest()
        boundForm.fold(
          formWithErrors => Future.successful(BadRequest(changePage(itemId, id, formWithErrors))),
          updatedInformation => changeInformation(itemId, id, existingInformation, updatedInformation, boundForm)
        )
      case _ => Future.successful(returnToSummary(itemId))
    }
  }

  private def returnToSummary(itemId: String)(implicit request: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(AdditionalInformationController.displayPage(itemId))

  private def findAdditionalInformation(itemId: String, id: String)(implicit request: JourneyRequest[AnyContent]): Option[AdditionalInformation] =
    ListItem.findById(id, request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation).map(_.items).getOrElse(Seq.empty))

  private def cachedAdditionalInformationData(itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation).getOrElse(AdditionalInformationData.default)

  private def changeInformation(
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
        formWithErrors => Future.successful(BadRequest(changePage(itemId, id, formWithErrors))),
        _ => {
          val updatedDataItems = cachedData.items.map(item => if (item == existingInformation) newInformation else item)
          updateCache(itemId, cachedData.copy(items = updatedDataItems))
            .map(_ => returnToSummary(itemId))
        }
      )
  }

  private def updateCache(itemId: String, updatedData: AdditionalInformationData)(
    implicit req: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.updatedItem(itemId, item => item.copy(additionalInformation = Some(updatedData))))
}
