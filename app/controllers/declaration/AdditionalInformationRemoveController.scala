/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.declaration.routes.{AdditionalInformationController, AdditionalInformationRequiredController}
import controllers.navigation.Navigator
import controllers.helpers.MultipleItemsHelper.remove
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.AdditionalInformation
import models.declaration.AdditionalInformationData
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ListItem
import views.html.declaration.additionalInformation.additional_information_remove

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationRemoveController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  removePage: additional_information_remove
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    findAdditionalInformation(itemId, id) match {
      case Some(information) => Ok(removePage(itemId, id, information, removeYesNoForm.withSubmissionErrors))
      case _                 => returnToSummary(itemId)
    }
  }

  def submitForm(itemId: String, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    findAdditionalInformation(itemId, id) match {
      case Some(information) =>
        removeYesNoForm
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(removePage(itemId, id, information, formWithErrors))),
            _.answer match {
              case YesNoAnswers.yes => removeAdditionalInformation(itemId, information).map(declaration => afterRemove(itemId, declaration))
              case YesNoAnswers.no  => Future.successful(returnToSummary(itemId))
            }
          )
      case _ => Future.successful(returnToSummary(itemId))
    }
  }

  private def removeYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalInformation.remove.empty")

  private def afterRemove(itemId: String, declaration: ExportsDeclaration)(implicit request: JourneyRequest[AnyContent]): Result =
    declaration.itemBy(itemId).flatMap(_.additionalInformation).map(_.items) match {
      case Some(items) if items.nonEmpty => returnToSummary(itemId)
      case _                             => navigator.continueTo(AdditionalInformationRequiredController.displayPage(itemId))
    }

  private def returnToSummary(itemId: String)(implicit request: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(AdditionalInformationController.displayPage(itemId))

  private def findAdditionalInformation(itemId: String, id: String)(implicit request: JourneyRequest[AnyContent]): Option[AdditionalInformation] =
    ListItem.findById(id, request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation).map(_.items).getOrElse(Seq.empty))

  private def removeAdditionalInformation(itemId: String, itemToRemove: AdditionalInformation)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] = {
    val cachedInformation = request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation).getOrElse(AdditionalInformationData.default)
    val updatedInformation = cachedInformation.copy(items = remove(cachedInformation.items, itemToRemove.equals(_: AdditionalInformation)))
    updateDeclarationFromRequest(model => model.updatedItem(itemId, item => item.copy(additionalInformation = Some(updatedInformation))))
  }
}
