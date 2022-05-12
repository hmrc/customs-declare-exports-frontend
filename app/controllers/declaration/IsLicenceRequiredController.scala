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

import controllers.actions.{AuthAction, FeatureFlagAction, JourneyAction}
import controllers.declaration.routes._
import controllers.navigation.Navigator
import features.Feature
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.YesNoAnswer.YesNoAnswers.{no, yes}
import forms.declaration.IsLicenceRequired
import models.DeclarationType._
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.is_licence_required

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsLicenceRequiredController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  featureFlagAction: FeatureFlagAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  is_licence_required: is_licence_required
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  private val validTypes = Seq(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL)

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] =
    (authenticate andThen journeyType(validTypes) andThen featureFlagAction(Feature.waiver999L)) { implicit request =>
      val formWithErrors = form.withSubmissionErrors.fill(_)

      val frm = request.cacheModel.itemBy(itemId).flatMap(_.isLicenceRequired).fold(form.withSubmissionErrors) {
        case true  => formWithErrors(YesNoAnswer(yes))
        case false => formWithErrors(YesNoAnswer(no))
      }

      Ok(is_licence_required(mode, itemId, frm))

    }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] =
    (authenticate andThen journeyType andThen featureFlagAction(Feature.waiver999L)).async { implicit request =>
      form.bindFromRequest
        .fold(formWithErrors => Future.successful(BadRequest(is_licence_required(mode, itemId, formWithErrors))), yesNo => {

          val isLicenceRequired = yesNo.answer == YesNoAnswers.yes

          updateCache(isLicenceRequired, itemId) map { _ =>
            navigator.continueTo(mode, nextPage(yesNo, itemId))
          }
        })
    }

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[AnyContent]): Mode => Call =
    yesNoAnswer.answer match {
      case _ if request.cacheModel.listOfAdditionalDocuments(itemId).nonEmpty =>
        AdditionalDocumentsController.displayPage(_, itemId)

      case YesNoAnswers.yes =>
        AdditionalDocumentAddController.displayPage(_, itemId)

      case YesNoAnswers.no if request.cacheModel.hasAuthCodeRequiringAdditionalDocs =>
        AdditionalDocumentAddController.displayPage(_, itemId)

      case YesNoAnswers.no =>
        AdditionalDocumentsRequiredController.displayPage(_, itemId)
    }

  private def updateCache(isLicenceRequired: Boolean, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updatedItem(itemId, _.copy(isLicenceRequired = Some(isLicenceRequired))))

  private def form: Form[YesNoAnswer] = IsLicenceRequired.form
}
