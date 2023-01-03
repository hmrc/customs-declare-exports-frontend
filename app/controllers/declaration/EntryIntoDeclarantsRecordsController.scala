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

import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.EntryIntoDeclarantsRecords.form
import models.DeclarationType.CLEARANCE
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.entry_into_declarants_records

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EntryIntoDeclarantsRecordsController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  entryIntoDeclarantsRecordsPage: entry_into_declarants_records
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType(CLEARANCE)) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.parties.isEntryIntoDeclarantsRecords match {
      case Some(data) => Ok(entryIntoDeclarantsRecordsPage(frm.fill(data)))
      case _          => Ok(entryIntoDeclarantsRecordsPage(frm))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType(CLEARANCE)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(entryIntoDeclarantsRecordsPage(formWithErrors))),
        validData => updateCache(validData).map(_ => navigator.continueTo(nextPage(validData)))
      )
  }

  private def updateCache(validData: YesNoAnswer)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      val updatedParties = validData.answer match {
        case YesNoAnswers.yes => model.parties.copy(isEntryIntoDeclarantsRecords = Some(validData))
        case YesNoAnswers.no =>
          model.removeAuthorisationProcedureCodeChoice.parties
            .copy(isEntryIntoDeclarantsRecords = Some(validData), personPresentingGoodsDetails = None)
      }

      model.copy(parties = updatedParties)
    }

  private def nextPage(answer: YesNoAnswer): Call =
    if (answer.answer == YesNoAnswers.yes) routes.PersonPresentingGoodsDetailsController.displayPage
    else routes.DeclarantDetailsController.displayPage
}
