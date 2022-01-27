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
import forms.declaration.declarationHolder.AuthorizationTypeCodes
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
    commodityCodeFromRequest(mode, itemId) { commodityCode =>
      Ok(is_license_required(mode, itemId, form, commodityCode, representativeStatusCode))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form.bindFromRequest
      .fold(
        formWithErrors =>
          Future.successful {
            commodityCodeFromRequest(mode, itemId) { commodityCode =>
              BadRequest(is_license_required(mode, itemId, formWithErrors, commodityCode, representativeStatusCode))
            }
        },
        yesNo =>
          updateCache(yesNo, itemId).map { _ =>
            navigator.continueTo(mode, nextPage(yesNo, itemId))
        }
      )
  }

  private def commodityCodeFromRequest(mode: Mode, itemId: String)(view: String => Result)(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.commodityCodeOfItem(itemId) match {
      case Some(commodityCode) =>
        view(commodityCode)
      case _ =>
        navigator.continueTo(mode, AdditionalInformationController.displayPage(_, itemId))
    }

  private def updateCache(yesNoAnswer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] = {
    val updatedAdditionalInformation = yesNoAnswer.answer match {
      case YesNoAnswers.yes => AdditionalInformationData(Some(yesNoAnswer), request.cacheModel.listOfAdditionalInformationOfItem(itemId))
      case YesNoAnswers.no  => AdditionalInformationData(Some(yesNoAnswer), Seq.empty)
    }
    updateDeclarationFromRequest(model => model.updatedItem(itemId, _.copy(additionalInformation = Some(updatedAdditionalInformation))))
  }

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[AnyContent]): Mode => Call =
    yesNoAnswer.answer match {
      case YesNoAnswers.yes => AdditionalDocumentAddController.displayPage(_, itemId)
      case YesNoAnswers.no if containsAuthCodeRequireDocumentation =>
        AdditionalDocumentsController.displayPage(_, itemId)
      case YesNoAnswers.no =>
        AdditionalDocumentsRequiredController.displayPage(_, itemId)
    }

  private def containsAuthCodeRequireDocumentation(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.parties.declarationHoldersData.exists(
      _.holders
        .flatMap(_.authorisationTypeCode)
        .toSet
        .intersect(AuthorizationTypeCodes.codesRequiringDocumentation)
        .nonEmpty
    )

  private def representativeStatusCode(implicit request: JourneyRequest[AnyContent]): Option[String] =
    request.cacheModel.parties.representativeDetails flatMap { _.statusCode }

  private def form: Form[YesNoAnswer] = YesNoAnswer.form()
}
