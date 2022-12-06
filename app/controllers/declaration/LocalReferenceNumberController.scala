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
import forms.declaration.ConsignmentReferences
import forms.Lrn.form
import forms.{Lrn, LrnValidator}
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.local_reference_number

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocalReferenceNumberController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  lrnValidator: LrnValidator,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  LrnPage: local_reference_number
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(): Action[AnyContent] =
    (authenticate andThen journeyType) { implicit request =>
      val frm = form.withSubmissionErrors()
      request.cacheModel.consignmentReferences.flatMap(_.lrn) match {
        case Some(data) => Ok(LrnPage(frm.fill(data)))
        case _          => Ok(LrnPage(frm))
      }
    }

  def submitLrn(): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      form
        .bindFromRequest()
        .verifyLrnValidity(lrnValidator)
        .flatMap(_.fold(formWithErrors => Future.successful(BadRequest(LrnPage(formWithErrors))), updateCacheAndContinue(_)))
    }

  private def updateCacheAndContinue(lrn: Lrn)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateDeclarationFromRequest { dec =>
      dec.copy(consignmentReferences = dec.ducr.map(ducr => ConsignmentReferences(ducr, Some(lrn))))
    } map (_ => navigator.continueTo(routes.LinkDucrToMucrController.displayPage))

}
