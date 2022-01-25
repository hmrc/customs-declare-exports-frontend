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
import controllers.declaration.routes._
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.YesNoAnswer.YesNoAnswers._
import models.DeclarationType._
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
    val formWithErrors = form.withSubmissionErrors

    val frm = request.cacheModel.itemBy(itemId).flatMap(_.isLicenseRequired).fold(form.withSubmissionErrors) {
      case true  => formWithErrors.fill(YesNoAnswer(yes))
      case false => formWithErrors.fill(YesNoAnswer(no))
    }

    Ok(is_license_required(mode, itemId, frm, representativeStatusCode))

  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form.bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(is_license_required(mode, itemId, formWithErrors, representativeStatusCode))),
        yesNo =>
          updateCache(yesNo, itemId) map { _ =>
            navigator.continueTo(mode, nextPage(yesNo, itemId))
        }
      )
  }

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[AnyContent]): Mode => Call =
    yesNoAnswer.answer match {
      case YesNoAnswers.yes => AdditionalDocumentAddController.displayPage(_, itemId)
      case YesNoAnswers.no if request.cacheModel.isAuthCodeRequiringAdditionalDocuments =>
        AdditionalDocumentsController.displayPage(_, itemId)
      case YesNoAnswers.no =>
        AdditionalDocumentsRequiredController.displayPage(_, itemId)
    }

  private def updateCache(yesNoAnswer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] = {
    val isLicenseRequired =
      if (yesNoAnswer.answer == YesNoAnswers.yes) true else false

    updateDeclarationFromRequest(_.updatedItem(itemId, _.copy(isLicenseRequired = Some(isLicenseRequired))))
  }

  private def representativeStatusCode(implicit request: JourneyRequest[AnyContent]): Option[String] =
    (request.cacheModel.parties.representativeDetails flatMap { _.statusCode }) orElse {
      if (request.cacheModel.parties.declarantIsExporter exists { _.isExporter }) Some("1")
      else None
    }

  private def form: Form[YesNoAnswer] = YesNoAnswer.form()
}
