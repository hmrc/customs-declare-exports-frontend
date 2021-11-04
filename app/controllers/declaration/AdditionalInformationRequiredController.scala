/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.declaration.routes.{AdditionalDocumentsController, AdditionalInformationController}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.AdditionalInformation
import javax.inject.Inject
import models.declaration.AdditionalInformationData
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalInformation.additional_information_required

class AdditionalInformationRequiredController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalInfoReq: additional_information_required
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    cachedItems(itemId) match {
      case items if items.isEmpty => Ok(additionalInfoReq(mode, itemId, previousAnswer(itemId).withSubmissionErrors()))
      case _                      => navigator.continueTo(mode, AdditionalInformationController.displayPage(_, itemId))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) => Future.successful(BadRequest(additionalInfoReq(mode, itemId, formWithErrors))),
        validYesNo =>
          updateCache(validYesNo, itemId).map { _ =>
            navigator.continueTo(mode, nextPage(validYesNo, itemId))
        }
      )
  }

  private def form(): Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalInformationRequired.error")

  private def previousAnswer(itemId: String)(implicit request: JourneyRequest[AnyContent]): Form[YesNoAnswer] =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation).flatMap(_.isRequired) match {
      case Some(answer) => form().fill(answer)
      case _            => form()
    }

  private def cachedItems(itemId: String)(implicit request: JourneyRequest[AnyContent]): Seq[AdditionalInformation] =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation).getOrElse(AdditionalInformationData.default).items

  private def updateCache(yesNoAnswer: YesNoAnswer, itemId: String)(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] = {
    val updatedAdditionalInformation = yesNoAnswer.answer match {
      case YesNoAnswers.yes => AdditionalInformationData(Some(yesNoAnswer), cachedItems(itemId))
      case YesNoAnswers.no  => AdditionalInformationData(Some(yesNoAnswer), Seq.empty)
    }
    updateExportsDeclarationSyncDirect(model => model.updatedItem(itemId, _.copy(additionalInformation = Some(updatedAdditionalInformation))))
  }

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String): Mode => Call =
    yesNoAnswer.answer match {
      case YesNoAnswers.yes => AdditionalInformationController.displayPage(_, itemId)
      case YesNoAnswers.no  => AdditionalDocumentsController.displayPage(_, itemId)
    }
}
