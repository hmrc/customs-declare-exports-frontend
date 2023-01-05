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
import controllers.helpers.MultipleItemsHelper
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.declaration.AdditionalInformation
import forms.declaration.AdditionalInformation.form
import models.ExportsDeclaration
import models.declaration.AdditionalInformationData
import models.declaration.AdditionalInformationData.maxNumberOfItems
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalInformation.additional_information_add

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationAddController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalInformationPage: additional_information_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(additionalInformationPage(itemId, form.withSubmissionErrors))
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form.bindFromRequest()

    boundForm.fold(
      formWithErrors => Future.successful(BadRequest(additionalInformationPage(itemId, formWithErrors))),
      _ => saveInformation(itemId, boundForm, cachedData(itemId))
    )
  }

  private def cachedData(itemId: String)(implicit request: JourneyRequest[AnyContent]): AdditionalInformationData =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation).getOrElse(AdditionalInformationData.default)

  private def saveInformation(itemId: String, boundForm: Form[AdditionalInformation], cachedData: AdditionalInformationData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData.items, maxNumberOfItems, AdditionalInformationFormGroupId, "declaration.additionalInformation")
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalInformationPage(itemId, formWithErrors))),
        updatedItems =>
          updateCache(itemId, cachedData.copy(isRequired = YesNoAnswer.Yes, items = updatedItems))
            .map(_ => navigator.continueTo(routes.AdditionalInformationController.displayPage(itemId)))
      )

  private def updateCache(itemId: String, updatedData: AdditionalInformationData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updatedItem(itemId, _.copy(additionalInformation = Some(updatedData))))
}

object AdditionalInformationAddController {
  val AdditionalInformationFormGroupId: String = "additionalInformation"
}
