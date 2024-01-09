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
import controllers.declaration.routes.{AdditionalDocumentsController, AdditionalInformationController, IsLicenceRequiredController}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.AdditionalInformationRequired
import models.declaration.AdditionalInformationData
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalInformation.additional_information_required

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationRequiredController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalInfoReq: additional_information_required
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    request.cacheModel.listOfAdditionalInformationOfItem(itemId) match {
      case additionalInformations if additionalInformations.isEmpty =>
        resolveBackLink(itemId) map { backLink =>
          val code = request.cacheModel.procedureCodeOfItem(itemId)
          Ok(additionalInfoReq(itemId, previousAnswer(itemId).withSubmissionErrors, backLink, code))
        }

      case _ => Future.successful(navigator.continueTo(AdditionalInformationController.displayPage(itemId)))
    }
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(showFormWithErrors(itemId, _), yesNo => updateCache(yesNo, itemId).map(_ => navigator.continueTo(nextPage(yesNo, itemId))))
  }

  private def form: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalInformationRequired.error")

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[_]): Call = {
    val isClearanceJourney = request.declarationType == DeclarationType.CLEARANCE

    yesNoAnswer.answer match {
      case YesNoAnswers.yes                      => AdditionalInformationController.displayPage(itemId)
      case YesNoAnswers.no if isClearanceJourney => AdditionalDocumentsController.displayPage(itemId)
      case _                                     => IsLicenceRequiredController.displayPage(itemId)
    }
  }

  private def previousAnswer(itemId: String)(implicit request: JourneyRequest[AnyContent]): Form[YesNoAnswer] =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation).flatMap(_.isRequired) match {
      case Some(answer) => form.fill(answer)
      case _            => form
    }

  private def resolveBackLink(itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[Call] =
    navigator.backLinkForAdditionalInformation(AdditionalInformationRequired, itemId)

  private def showFormWithErrors(itemId: String, formWithErrors: Form[YesNoAnswer])(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    resolveBackLink(itemId) map { backLink =>
      BadRequest(additionalInfoReq(itemId, formWithErrors, backLink, request.cacheModel.procedureCodeOfItem(itemId)))
    }

  private def updateCache(answer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] = {
    val updatedAdditionalInformation = answer.answer match {
      case YesNoAnswers.yes => AdditionalInformationData(Some(answer), request.cacheModel.listOfAdditionalInformationOfItem(itemId))
      case YesNoAnswers.no  => AdditionalInformationData(Some(answer), Seq.empty)
    }
    updateDeclarationFromRequest(_.updatedItem(itemId, _.copy(additionalInformation = Some(updatedAdditionalInformation))))
  }
}
