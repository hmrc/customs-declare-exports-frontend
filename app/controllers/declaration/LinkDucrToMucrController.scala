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

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.{form, YesNoAnswers}
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.link_ducr_to_mucr

@Singleton
class LinkDucrToMucrController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  linkDucrToMucrPage: link_ducr_to_mucr
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form().withSubmissionErrors
    request.cacheModel.linkDucrToMucr match {
      case Some(yesNoAnswer) => Ok(linkDucrToMucrPage(mode, frm.fill(yesNoAnswer)))
      case _                 => Ok(linkDucrToMucrPage(mode, frm))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(linkDucrToMucrPage(mode, formWithErrors))),
        yesNoAnswer => updateCache(yesNoAnswer).map(_ => navigator.continueTo(mode, nextPage(yesNoAnswer)))
      )
  }

  private def nextPage(yesNoAnswer: YesNoAnswer)(implicit request: JourneyRequest[_]): Mode => Call =
    if (yesNoAnswer.answer == YesNoAnswers.yes) routes.MucrController.displayPage
    else {
      request.declarationType match {
        case DeclarationType.CLEARANCE => routes.EntryIntoDeclarantsRecordsController.displayPage
        case _                         => routes.DeclarantExporterController.displayPage
      }
    }

  private def updateCache(yesNoAnswer: YesNoAnswer)(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      model.copy(linkDucrToMucr = Some(yesNoAnswer), mucr = if (yesNoAnswer.answer == YesNoAnswers.no) None else model.mucr)
    }
}
