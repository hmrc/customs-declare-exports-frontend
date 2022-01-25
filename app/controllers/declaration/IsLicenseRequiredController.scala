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
import controllers.declaration.routes.{AdditionalDocumentAddController, AdditionalDocumentsRequiredController, AdditionalInformationController}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType._
import models.declaration.AdditionalInformationData
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.is_license_required

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsLicenseRequiredController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  is_license_required: is_license_required
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  private val validTypes = Seq(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL)

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    request.cacheModel.listOfAdditionalInformationOfItem(itemId) match {
      case additionalInformation if additionalInformation.isEmpty =>
        Ok(is_license_required(mode, itemId, form))
      case _ => navigator.continueTo(mode, AdditionalInformationController.displayPage(_, itemId))
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

  private def showFormWithErrors(mode: Mode, itemId: String, formWithErrors: Form[YesNoAnswer])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = Future.successful(BadRequest(is_license_required(mode, itemId, formWithErrors)))

  private def updateCache(yesNoAnswer: YesNoAnswer, itemId: String)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] = {
    val updatedAdditionalInformation = yesNoAnswer.answer match {
      case YesNoAnswers.yes => AdditionalInformationData(Some(yesNoAnswer), request.cacheModel.listOfAdditionalInformationOfItem(itemId))
      case YesNoAnswers.no  => AdditionalInformationData(Some(yesNoAnswer), Seq.empty)
    }
    updateExportsDeclarationSyncDirect(model => model.updatedItem(itemId, _.copy(additionalInformation = Some(updatedAdditionalInformation))))
  }

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String): Mode => Call =
    yesNoAnswer.answer match {
      case YesNoAnswers.yes => AdditionalDocumentAddController.displayPage(_, itemId)
      case YesNoAnswers.no  => AdditionalDocumentsRequiredController.displayPage(_, itemId)
    }

  private def form: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalInformationRequired.error")
}
