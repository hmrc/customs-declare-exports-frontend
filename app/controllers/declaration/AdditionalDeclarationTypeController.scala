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

import scala.concurrent.{ExecutionContext, Future}

import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype._
import javax.inject.Inject
import models.DeclarationType._
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionaldeclarationtype.declaration_type

class AdditionalDeclarationTypeController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarationTypePage: declaration_type
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val form = extractFormType(request).form().withSubmissionErrors()
    request.cacheModel.additionalDeclarationType match {
      case Some(data) => Ok(declarationTypePage(mode, form.fill(data)))
      case _          => Ok(declarationTypePage(mode, form))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val decType = extractFormType(request).form().bindFromRequest()

    decType
      .fold(
        formWithErrors => Future.successful(BadRequest(declarationTypePage(mode, formWithErrors))),
        validAdditionalDeclarationType => updateCache(validAdditionalDeclarationType).map(_ => navigator.continueTo(mode, nextPage))
      )
  }

  private def extractFormType(journeyRequest: JourneyRequest[_]): AdditionalDeclarationTypeTrait =
    journeyRequest.declarationType match {
      case SUPPLEMENTARY => AdditionalDeclarationTypeSupplementaryDec
      case STANDARD      => AdditionalDeclarationTypeStandardDec
      case SIMPLIFIED    => AdditionalDeclarationTypeSimplifiedDec
      case OCCASIONAL    => AdditionalDeclarationTypeOccasionalDec
      case CLEARANCE     => AdditionalDeclarationTypeClearanceDec
    }

  private def nextPage(implicit request: JourneyRequest[_]): Mode => Call =
    if (request.declarationType == CLEARANCE) routes.ConsignmentReferencesController.displayPage
    else routes.DeclarantDetailsController.displayPage

  private def updateCache(formData: AdditionalDeclarationType)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => {
      model.copy(additionalDeclarationType = Some(formData))
    })
}
