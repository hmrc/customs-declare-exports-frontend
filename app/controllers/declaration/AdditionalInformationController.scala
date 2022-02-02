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
import controllers.declaration.routes.{AdditionalInformationRequiredController, IsLicenseRequiredController}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.AdditionalInformationSummary
import models.Mode
import models.declaration.AdditionalInformationData
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalInformation.additional_information

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalInformationPage: additional_information
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val frm = form.withSubmissionErrors()
    cachedAdditionalInformationData(itemId) match {
      case Some(additionalInformationData) if additionalInformationData.items.nonEmpty =>
        resolveBackLink(mode, itemId) map { backLink =>
          Ok(additionalInformationPage(mode, itemId, frm, additionalInformationData.items, backLink))
        }

      case Some(_) =>
        Future.successful(navigator.continueTo(mode, routes.AdditionalInformationAddController.displayPage(_, itemId)))

      case _ =>
        Future.successful(navigator.continueTo(mode, AdditionalInformationRequiredController.displayPage(_, itemId)))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form.bindFromRequest
      .fold(showFormWithErrors(mode, itemId, _), yesNoAnswer => Future.successful(navigator.continueTo(mode, nextPage(yesNoAnswer, itemId))))
  }

  private def form: Form[YesNoAnswer] =
    YesNoAnswer.form(errorKey = "declaration.additionalInformation.add.another.empty")

  private def cachedAdditionalInformationData(itemId: String)(implicit request: JourneyRequest[_]): Option[AdditionalInformationData] =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation)

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String): Mode => Call =
    yesNoAnswer.answer match {
      case YesNoAnswers.yes => routes.AdditionalInformationAddController.displayPage(_, itemId)
      case YesNoAnswers.no  => IsLicenseRequiredController.displayPage(_, itemId)
    }

  private def resolveBackLink(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[Call] =
    navigator.backLinkForAdditionalInformation(AdditionalInformationSummary, mode, itemId)

  private def showFormWithErrors(mode: Mode, itemId: String, formWithErrors: Form[YesNoAnswer])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    resolveBackLink(mode, itemId) map {
      val items = cachedAdditionalInformationData(itemId).map(_.items).getOrElse(Seq.empty)
      backLink =>
        BadRequest(additionalInformationPage(mode, itemId, formWithErrors, items, backLink))
    }
}
