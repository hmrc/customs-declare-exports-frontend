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
import forms.Ducr
import forms.Ducr.form
import forms.declaration.IntermediaryConsignmentReferences
import models.DeclarationType.{allDeclarationTypes, allDeclarationTypesExcluding, SUPPLEMENTARY}
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
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  ducrEntryPage: ducr_entry
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType(allDeclarationTypesExcluding(SUPPLEMENTARY))) { implicit request =>
    val frm = form.withSubmissionErrors()
    request.cacheModel.ducr match {
      case Some(data) => Ok(ducrEntryPage(frm.fill(Ducr(data.ducr))))
      case _          => Ok(ducrEntryPage(frm))
    }
  }

  val submitDucr: Action[AnyContent] = (authenticate andThen journeyType(allDeclarationTypesExcluding(SUPPLEMENTARY))).async { implicit request =>
    form
      .bindFromRequest()
      .fold(formWithErrors => Future.successful(BadRequest(ducrEntryPage(formWithErrors))), updateCacheAndContinue(_))
  }

  private def updateCacheAndContinue(ducr: Ducr)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateDeclarationFromRequest(_.copy(intermediaryConsignmentReferences = Some(IntermediaryConsignmentReferences(Some(ducr), None))))
      .map(_ => navigator.continueTo(routes.LocalReferenceNumberController.displayPage))

}
