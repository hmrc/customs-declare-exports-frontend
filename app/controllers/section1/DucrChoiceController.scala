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
import controllers.section1.routes.{DucrEntryController, TraderReferenceController}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers.no
import forms.section1.ConsignmentReferences
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section1.ducr_choice

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DucrChoiceController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  ducrChoicePage: ducr_choice
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with AmendmentDraftFilter with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val nextPage: JourneyRequest[_] => Call = _ => DucrEntryController.displayPage

  private val validTypes = allDeclarationTypesExcluding(SUPPLEMENTARY)

  private val actionFilters = authenticate andThen journeyAction(validTypes) andThen nextPageIfAmendmentDraft

  val displayPage: Action[AnyContent] = actionFilters { implicit request =>
    val form = YesNoAnswer.form(errorKey = "declaration.ducr.choice.answer.empty")
    Ok(ducrChoicePage(populateForm(form.withSubmissionErrors)))
  }

  val submitForm: Action[AnyContent] = actionFilters.async { implicit request =>
    YesNoAnswer
      .form(errorKey = "declaration.ducr.choice.answer.empty")
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(ducrChoicePage(formWithErrors))),
        yesNoAnswer =>
          updateCache(yesNoAnswer).map { _ =>
            navigator.continueTo(if (yesNoAnswer.answer == no) TraderReferenceController.displayPage else nextPage(request))
          }
      )
  }

  private def updateCache(yesNoAnswer: YesNoAnswer)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      declaration.copy(consignmentReferences = declaration.consignmentReferences match {
        case Some(consignmentRefs) => Some(consignmentRefs.copy(hasDucr = Some(yesNoAnswer.answer)))
        case _                     => Some(ConsignmentReferences(ducr = None, hasDucr = Some(yesNoAnswer.answer)))
      })
    }

  private def populateForm(form: Form[YesNoAnswer])(implicit request: JourneyRequest[_]): Form[YesNoAnswer] =
    request.cacheModel.consignmentReferences.flatMap(_.hasDucr) match {
      case Some("Yes") =>
        println("Got to Yes Value")
        form.fill(YesNoAnswer.Yes.get)
      case Some("No") =>
        println("Got to No Value")
        form.fill(YesNoAnswer.No.get)
      case _ => form
    }
}
