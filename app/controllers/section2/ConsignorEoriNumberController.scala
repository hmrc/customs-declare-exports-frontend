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

package controllers.section2

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section2.routes._
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section2.consignor.ConsignorEoriNumber.form
import forms.section2.consignor.{ConsignorDetails, ConsignorEoriNumber}
import models.DeclarationType.CLEARANCE
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section2.consignor_eori_number

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignorEoriNumberController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  consignorEoriDetailsPage: consignor_eori_number,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  val validJourneys: Seq[DeclarationType.Value] = Seq(CLEARANCE)

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType(validJourneys)) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.parties.consignorDetails match {
      case Some(data) => Ok(consignorEoriDetailsPage(frm.fill(ConsignorEoriNumber(data))))
      case _          => Ok(consignorEoriDetailsPage(frm))
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType(validJourneys)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ConsignorEoriNumber]) => {
          val formWithAdjustedErrors = formWithErrors

          Future.successful(BadRequest(consignorEoriDetailsPage(formWithAdjustedErrors)))
        },
        form =>
          updateCache(form, request.cacheModel.parties.consignorDetails)
            .map(_ => navigator.continueTo(nextPage(form.hasEori)))
      )
  }

  private def nextPage(hasEori: String)(implicit request: JourneyRequest[_]): Call =
    request.cacheModel.`type` match {
      case CLEARANCE if hasEori == YesNoAnswers.yes                                   => ThirdPartyGoodsTransportationController.displayPage
      case CLEARANCE                                                                  => ConsignorDetailsController.displayPage
      case _ if hasEori == YesNoAnswers.yes && request.cacheModel.isDeclarantExporter => CarrierEoriNumberController.displayPage
      case _ if hasEori == YesNoAnswers.yes                                           => RepresentativeAgentController.displayPage
      case _                                                                          => ConsignorDetailsController.displayPage
    }

  private def updateCache(formData: ConsignorEoriNumber, savedConsignorDetails: Option[ConsignorDetails])(
    implicit r: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model =>
      model.copy(parties = model.parties.copy(consignorDetails = Some(ConsignorDetails.from(formData, savedConsignorDetails))))
    )
}
