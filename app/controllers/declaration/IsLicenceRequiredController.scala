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
import controllers.declaration.routes._
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.YesNoAnswer.YesNoAnswers.{no, yes}
import forms.declaration.IsLicenceRequired
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.TaggedAuthCodes
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.is_licence_required

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsLicenceRequiredController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  taggedAuthCodes: TaggedAuthCodes,
  is_licence_required: is_licence_required
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val validTypes = Seq(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL)

  def displayPage(itemId: String): Action[AnyContent] =
    (authenticate andThen journeyType(validTypes)) { implicit request =>
      val formWithErrors = form.withSubmissionErrors.fill(_)

      val frm = request.cacheModel.itemBy(itemId).flatMap(_.isLicenceRequired).fold(form.withSubmissionErrors) {
        case true  => formWithErrors(YesNoAnswer(yes))
        case false => formWithErrors(YesNoAnswer(no))
      }

      Ok(is_licence_required(itemId, frm))

    }

  def submitForm(itemId: String): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(is_licence_required(itemId, formWithErrors))),
          yesNo => {

            val isLicenceRequired = yesNo.answer == YesNoAnswers.yes

            updateCache(isLicenceRequired, itemId) map { _ =>
              navigator.continueTo(nextPage(yesNo, itemId))
            }
          }
        )
    }

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[AnyContent]): Call =
    yesNoAnswer.answer match {
      case _ if request.cacheModel.listOfAdditionalDocuments(itemId).nonEmpty =>
        AdditionalDocumentsController.displayPage(itemId)

      case YesNoAnswers.yes =>
        AdditionalDocumentAddController.displayPage(itemId)

      case YesNoAnswers.no if taggedAuthCodes.hasAuthCodeRequiringAdditionalDocs(request.cacheModel) =>
        AdditionalDocumentAddController.displayPage(itemId)

      case YesNoAnswers.no =>
        AdditionalDocumentsRequiredController.displayPage(itemId)
    }

  private def updateCache(isLicenceRequired: Boolean, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updatedItem(itemId, _.copy(isLicenceRequired = Some(isLicenceRequired))))

  private def form: Form[YesNoAnswer] = IsLicenceRequired.form
}
