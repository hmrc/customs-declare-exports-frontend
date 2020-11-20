/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.declaration.carrier.{CarrierDetails, CarrierEoriNumber}
import javax.inject.Inject
import models.{ExportsDeclaration, Mode}
import models.requests.JourneyRequest
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.carrier_eori_number

import scala.concurrent.{ExecutionContext, Future}

class CarrierEoriNumberController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  carrierEoriDetailsPage: carrier_eori_number,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  val validJourneys = Seq(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE)

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validJourneys)) { implicit request =>
    request.cacheModel.parties.carrierDetails match {
      case Some(data) => Ok(carrierEoriDetailsPage(mode, form().fill(CarrierEoriNumber(data))))
      case _          => Ok(carrierEoriDetailsPage(mode, form()))
    }
  }

  private def form()(implicit request: JourneyRequest[_]) = CarrierEoriNumber.form().withSubmissionErrors()

  def submit(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validJourneys)).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[CarrierEoriNumber]) => {
          val formWithAdjustedErrors = formWithErrors
          Future.successful(BadRequest(carrierEoriDetailsPage(mode, formWithAdjustedErrors)))
        },
        form =>
          updateCache(form, request.cacheModel.parties.carrierDetails)
            .map(_ => navigator.continueTo(mode, nextPage(form.hasEori)))
      )
  }

  private def nextPage(hasEori: String): Mode => Call =
    if (hasEori == YesNoAnswers.yes) {
      controllers.declaration.routes.ConsigneeDetailsController.displayPage
    } else {
      controllers.declaration.routes.CarrierDetailsController.displayPage
    }

  private def updateCache(formData: CarrierEoriNumber, savedCarrierDetails: Option[CarrierDetails])(
    implicit r: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(
      model => model.copy(parties = model.parties.copy(carrierDetails = Some(CarrierDetails.from(formData, savedCarrierDetails))))
    )
}
