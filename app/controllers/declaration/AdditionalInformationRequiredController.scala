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
import controllers.declaration.routes.{AdditionalDocumentsController, AdditionalInformationController}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.AdditionalInformationRequired
import models.declaration.AdditionalInformationData
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalInformation.additional_information_required

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationRequiredController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalInfoReq: additional_information_required
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    request.cacheModel.listOfAdditionalInformationOfItem(itemId) match {
      case additionalInformations if additionalInformations.isEmpty =>
        resolveBackLink(mode, itemId) map { backLink =>
          Ok(additionalInfoReq(mode, itemId, previousAnswer(itemId).withSubmissionErrors, backLink, request.cacheModel.procedureCodeOfItem(itemId)))
        }

      case _ => Future.successful(navigator.continueTo(mode, AdditionalInformationController.displayPage(_, itemId)))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form.bindFromRequest
      .fold(
        showFormWithErrors(mode, itemId, _),
        yesNo =>
          updateCache(yesNo, itemId).map { _ =>
            navigator.continueTo(mode, nextPage(yesNo, itemId))
        }
      )
  }

  private def form: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalInformationRequired.error")

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String): Mode => Call =
    yesNoAnswer.answer match {
      case YesNoAnswers.yes => AdditionalInformationController.displayPage(_, itemId)
      case YesNoAnswers.no  => AdditionalDocumentsController.displayPage(_, itemId)
    }

  private def previousAnswer(itemId: String)(implicit request: JourneyRequest[AnyContent]): Form[YesNoAnswer] =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation).flatMap(_.isRequired) match {
      case Some(answer) => form.fill(answer)
      case _            => form
    }

  private def resolveBackLink(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[Call] =
    navigator.backLinkForAdditionalInformation(AdditionalInformationRequired, mode, itemId)

  private def showFormWithErrors(mode: Mode, itemId: String, formWithErrors: Form[YesNoAnswer])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    resolveBackLink(mode, itemId) map { backLink =>
      BadRequest(additionalInfoReq(mode, itemId, formWithErrors, backLink, request.cacheModel.procedureCodeOfItem(itemId)))
    }

  private def updateCache(yesNoAnswer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] = {
    val updatedAdditionalInformation = yesNoAnswer.answer match {
      case YesNoAnswers.yes => AdditionalInformationData(Some(yesNoAnswer), request.cacheModel.listOfAdditionalInformationOfItem(itemId))
      case YesNoAnswers.no  => AdditionalInformationData(Some(yesNoAnswer), Seq.empty)
    }
    updateDeclarationFromRequest(model => model.updatedItem(itemId, _.copy(additionalInformation = Some(updatedAdditionalInformation))))
  }
}
