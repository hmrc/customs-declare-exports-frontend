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
import controllers.navigation.{ItemId, Navigator}
import controllers.helpers.MultipleItemsHelper
import forms.declaration.AdditionalInformation
import forms.declaration.AdditionalInformation.form
import models.declaration.AdditionalInformationData
import models.declaration.AdditionalInformationData.maxNumberOfItems
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalInformation.additional_information_add

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalInformationPage: additional_information_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    resolveBackLink(mode, itemId).map { backLink =>
      Ok(additionalInformationPage(mode, itemId, form().withSubmissionErrors(), backLink))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()

    boundForm.fold(
      formWithErrors =>
        resolveBackLink(mode, itemId).map { backLink =>
          BadRequest(additionalInformationPage(mode, itemId, formWithErrors, backLink))
      },
      _ => saveInformation(mode, itemId, boundForm, cachedData(itemId))
    )
  }

  private def cachedData(itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation).getOrElse(AdditionalInformationData.default)

  private def saveInformation(mode: Mode, itemId: String, boundForm: Form[AdditionalInformation], cachedData: AdditionalInformationData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData.items, maxNumberOfItems, AdditionalInformationFormGroupId, "declaration.additionalInformation")
      .fold(
        formWithErrors =>
          resolveBackLink(mode, itemId).map { backLink =>
            BadRequest(additionalInformationPage(mode, itemId, formWithErrors, backLink))
        },
        updatedItems =>
          updateCache(itemId, cachedData.copy(items = updatedItems))
            .map(_ => navigator.continueTo(mode, routes.AdditionalInformationController.displayPage(_, itemId)))
      )

  private def updateCache(itemId: String, updatedData: AdditionalInformationData)(
    implicit req: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      model.updatedItem(itemId, item => item.copy(additionalInformation = Some(updatedData)))
    })

  private def resolveBackLink(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[Call] =
    navigator.backLink(AdditionalInformation, mode, ItemId(itemId))
}

object AdditionalInformationAddController {
  val AdditionalInformationFormGroupId: String = "additionalInformation"
}
