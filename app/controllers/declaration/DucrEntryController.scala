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
import forms.{Ducr, LrnValidator}
import forms.Ducr
import forms.Ducr.form
import models.DeclarationType.SUPPLEMENTARY
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.ducr_entry

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DucrEntryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  lrnValidator: LrnValidator,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  ducrEntryPage: ducr_entry
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form.withSubmissionErrors()
    request.cacheModel.ducrEntry match {
      case Some(data) => Ok(ducrEntryPage(frm.fill(data)))
      case _          => Ok(ducrEntryPage(frm))
    }
  }

  val submitDucr: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(formWithErrors => Future.successful(BadRequest(ducrEntryPage(formWithErrors))), updateCacheAndContinue(_))
  }

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Call =
    if (request.declarationType == SUPPLEMENTARY) routes.DeclarantExporterController.displayPage
    else routes.LinkDucrToMucrController.displayPage

  private def updateCacheAndContinue(ducr: Ducr)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateDeclarationFromRequest(_.copy(ducrEntry = Some(ducr)))
      .map(_ => navigator.continueTo(nextPage))

}
