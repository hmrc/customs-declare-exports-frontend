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

import controllers.actions.{AmendmentDraftFilter, AuthAction, JourneyAction}
import controllers.declaration.routes.{DeclarantExporterController, EntryIntoDeclarantsRecordsController, MucrController}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.{form, YesNoAnswers}
import models.DeclarationType.CLEARANCE
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.link_ducr_to_mucr

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LinkDucrToMucrController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  linkDucrToMucrPage: link_ducr_to_mucr
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with AmendmentDraftFilter with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val nextPage: JourneyRequest[_] => Call =
    request =>
      if (request.declarationType == CLEARANCE) EntryIntoDeclarantsRecordsController.displayPage
      else DeclarantExporterController.displayPage

  private val actionFilters = authenticate andThen journeyAction andThen nextPageIfAmendmentDraft

  val displayPage: Action[AnyContent] = actionFilters { implicit request =>
    val frm = form().withSubmissionErrors
    request.cacheModel.linkDucrToMucr match {
      case Some(yesNoAnswer) => Ok(linkDucrToMucrPage(frm.fill(yesNoAnswer)))
      case _                 => Ok(linkDucrToMucrPage(frm))
    }
  }

  val submitForm: Action[AnyContent] = actionFilters.async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(linkDucrToMucrPage(formWithErrors))),
        yesNoAnswer => updateCache(yesNoAnswer).map(_ => navigator.continueTo(nextPage(yesNoAnswer)))
      )
  }

  private def nextPage(yesNoAnswer: YesNoAnswer)(implicit request: JourneyRequest[_]): Call =
    if (yesNoAnswer.answer == YesNoAnswers.yes) MucrController.displayPage else nextPage(request)

  private def updateCache(yesNoAnswer: YesNoAnswer)(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      model.copy(linkDucrToMucr = Some(yesNoAnswer), mucr = if (yesNoAnswer.answer == YesNoAnswers.no) None else model.mucr)
    }
}
