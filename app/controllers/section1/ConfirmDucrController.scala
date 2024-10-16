/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section1

import controllers.actions.{AmendmentDraftFilter, AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section1.routes.{DucrEntryController, LocalReferenceNumberController}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.i18n.Lang.logger
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section1.confirm_ducr

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmDucrController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  override val exportsCacheService: ExportsCacheService,
  confirmDucrPage: confirm_ducr
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with AmendmentDraftFilter with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val nextPage: JourneyRequest[_] => Call = _ => DucrEntryController.displayPage

  private val validTypes = allDeclarationTypesExcluding(SUPPLEMENTARY)

  private val actionFilters = authenticate andThen journeyAction(validTypes) andThen nextPageIfAmendmentDraft

  val displayPage: Action[AnyContent] = actionFilters { implicit request =>
    request.cacheModel.ducr.fold {
      logger.warn("No generated DUCR found in cache!")
      Redirect(DucrEntryController.displayPage)
    } { ducr =>
      Ok(confirmDucrPage(form.withSubmissionErrors, ducr))
    }
  }

  val submitForm: Action[AnyContent] = actionFilters.async { implicit request =>
    request.cacheModel.ducr.fold {
      logger.warn("No generated DUCR found in cache!")
      Future.successful(Redirect(DucrEntryController.displayPage))
    } { ducr =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(confirmDucrPage(formWithErrors, ducr))),
          {
            case YesNoAnswer(YesNoAnswers.yes) => Future.successful(navigator.continueTo(LocalReferenceNumberController.displayPage))
            case _                             => updateCache.map(_ => navigator.continueTo(nextPage(request)))
          }
        )
    }
  }

  private def form: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.confirmDucr.error.empty")

  private def updateCache(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] =
    request.cacheModel.consignmentReferences.fold(Future.successful(request.cacheModel))(consignmentRefs =>
      updateDeclarationFromRequest(_.copy(consignmentReferences = Some(consignmentRefs.copy(ducr = None))))
    )
}
