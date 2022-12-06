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
import controllers.navigation.Navigator
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypePage
import models.DeclarationType.CLEARANCE
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additional_declaration_type

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalDeclarationTypeController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalTypePage: additional_declaration_type
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val form = AdditionalDeclarationTypePage.form.withSubmissionErrors
    request.cacheModel.additionalDeclarationType match {
      case Some(data) => Ok(additionalTypePage(form.fill(data)))
      case _          => Ok(additionalTypePage(form))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val form = AdditionalDeclarationTypePage.form.bindFromRequest
    form
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalTypePage(formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(nextPage))
      )
  }

  private def nextPage(implicit request: JourneyRequest[_]): Call =
    if (request.declarationType == CLEARANCE) routes.DucrEntryController.displayPage
    else routes.DeclarantDetailsController.displayPage

  private def updateCache(adt: AdditionalDeclarationType)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.copy(additionalDeclarationType = Some(adt)))
}
