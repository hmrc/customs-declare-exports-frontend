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
import controllers.declaration.routes.{DucrEntryController, TraderReferenceController}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers.no
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.ducr_choice

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DucrChoiceController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  ducrChoicePage: ducr_choice
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with AmendmentDraftFilter with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val nextPage: JourneyRequest[_] => Call = _ => DucrEntryController.displayPage
  private val validTypes = allDeclarationTypesExcluding(SUPPLEMENTARY)

  private val actionFilters = authenticate andThen journeyAction(validTypes) andThen nextPageIfAmendmentDraft

  val displayPage: Action[AnyContent] = actionFilters { implicit request =>
    val form = YesNoAnswer.form(errorKey = "declaration.ducr.choice.answer.empty").withSubmissionErrors
    Ok(ducrChoicePage(form))
  }

  val submitForm: Action[AnyContent] = actionFilters.async { implicit request =>
    YesNoAnswer
      .form(errorKey = "declaration.ducr.choice.answer.empty")
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(ducrChoicePage(formWithErrors))),
        yesNoAnswer =>
          if (yesNoAnswer.answer == no) Future.successful(navigator.continueTo(TraderReferenceController.displayPage))
          else updateCache.map(_ => navigator.continueTo(nextPage(request)))
      )
  }

  private def updateCache(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      declaration.copy(consignmentReferences = declaration.consignmentReferences.map(_.copy(ducr = None)))
    }
}
