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
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.consignor.ConsignorEoriNumber.form
import forms.declaration.consignor.{ConsignorDetails, ConsignorEoriNumber}
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.consignor_eori_number

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignorEoriNumberController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  consignorEoriDetailsPage: consignor_eori_number,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  val validJourneys = Seq(DeclarationType.CLEARANCE)

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
    if (hasEori == YesNoAnswers.yes && request.cacheModel.isDeclarantExporter) {
      controllers.declaration.routes.CarrierEoriNumberController.displayPage
    } else if (hasEori == YesNoAnswers.yes) {
      controllers.declaration.routes.RepresentativeAgentController.displayPage
    } else {
      controllers.declaration.routes.ConsignorDetailsController.displayPage
    }

  private def updateCache(formData: ConsignorEoriNumber, savedConsignorDetails: Option[ConsignorDetails])(
    implicit r: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model =>
      model.copy(parties = model.parties.copy(consignorDetails = Some(ConsignorDetails.from(formData, savedConsignorDetails))))
    )
}
