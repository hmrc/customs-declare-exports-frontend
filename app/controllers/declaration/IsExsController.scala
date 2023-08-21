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
import controllers.declaration.routes._
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.{IsExs, UNDangerousGoodsCode}
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.declaration.{ExportItem, Parties}
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.is_exs

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsExsController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  isExsPage: is_exs
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val allowedJourney: DeclarationType = CLEARANCE

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType(allowedJourney)) { implicit request =>
    val frm = IsExs.form.withSubmissionErrors
    request.cacheModel.parties.isExs match {
      case Some(data) => Ok(isExsPage(frm.fill(data)))
      case _          => Ok(isExsPage(frm))
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType(allowedJourney)).async { implicit request =>
    IsExs.form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(isExsPage(formWithErrors))),
        answer => updateCache(answer).map(_ => navigator.continueTo(nextPage(answer)))
      )
  }

  private def updateCache(answer: IsExs)(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      val updatedParties: Parties = answer.isExs match {
        case YesNoAnswers.yes => model.parties.copy(isExs = Some(answer))
        case YesNoAnswers.no  => model.parties.copy(isExs = Some(answer), carrierDetails = None, consignorDetails = None)
      }

      val updatedItems: Seq[ExportItem] = answer.isExs match {
        case YesNoAnswers.yes =>
          // CEDS-2621 - retain any existing UN dangerous goods code on the items if isExs is true.
          model.items.map(item => item.copy(dangerousGoodsCode = Some(item.dangerousGoodsCode.getOrElse(UNDangerousGoodsCode(None)))))
        case YesNoAnswers.no => model.items.map(_.copy(dangerousGoodsCode = None))
      }

      model.copy(parties = updatedParties, items = updatedItems)
    }

  private def nextPage(isExs: IsExs)(implicit request: JourneyRequest[_]): Call =
    isExs.isExs match {
      case YesNoAnswers.yes => ConsignorEoriNumberController.displayPage
      case YesNoAnswers.no =>
        if (request.cacheModel.isDeclarantExporter) AuthorisationProcedureCodeChoiceController.displayPage
        else RepresentativeAgentController.displayPage
    }
}
