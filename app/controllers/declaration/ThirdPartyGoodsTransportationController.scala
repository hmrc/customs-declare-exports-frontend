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

package controllers.declaration

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.routes._
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.declaration.carrier.CarrierDetails
import models.DeclarationType._
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.third_party_goods_transportation

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ThirdPartyGoodsTransportationController @Inject() (
  mcc: MessagesControllerComponents,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  thirdPartyGoodTransportPage: third_party_goods_transportation
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with WithUnsafeDefaultFormBinding with I18nSupport with SubmissionErrors with ModelCacheable {

  private val formWithErrorKey = YesNoAnswer.form(errorKey = "declaration.thirdPartyGoodsTransportation.radio.error")

  private val allowedJourneys = Seq(STANDARD, OCCASIONAL, SIMPLIFIED, CLEARANCE)

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType(allowedJourneys)) { implicit request =>
    Ok(thirdPartyGoodTransportPage(populateForm(formWithErrorKey.withSubmissionErrors)))
  }

  val submitPage: Action[AnyContent] = (authenticate andThen journeyType(allowedJourneys)) { implicit request =>
    formWithErrorKey
      .bindFromRequest()
      .fold(formWithErrors => BadRequest(thirdPartyGoodTransportPage(formWithErrors)), answer => navigator.continueTo(saveAndRedirect(answer)))
  }

  private def populateForm(form: Form[YesNoAnswer])(implicit request: JourneyRequest[_]): Form[YesNoAnswer] =
    request.cacheModel.isUsingOwnTransport match {
      case Some(true)  => form.fill(YesNoAnswer.No.get)
      case Some(false) => form.fill(YesNoAnswer.Yes.get)
      case _           => form
    }

  private def saveAndRedirect(answer: YesNoAnswer)(implicit request: JourneyRequest[_]): Call = Some(answer) match {
    case YesNoAnswer.Yes =>
      if (request.cacheModel.parties.carrierDetails.flatMap(_.details.eori).exists(_.value == request.eori))
        updateDeclarationFromRequest(model => model.copy(parties = model.parties.copy(carrierDetails = None)))
      CarrierEoriNumberController.displayPage
    case _ =>
      updateDeclarationFromRequest(model => model.copy(parties = model.parties.copy(carrierDetails = Some(CarrierDetails.from(request.eori)))))
      ConsigneeDetailsController.displayPage
  }
}
