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

package controllers.section2

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section2.routes.{CarrierEoriNumberController, ConsigneeDetailsController}
import forms.common.YesNoAnswer
import models.DeclarationType._
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section2.third_party_goods_transportation

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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

  val submitPage: Action[AnyContent] = (authenticate andThen journeyType(allowedJourneys)).async { implicit request =>
    formWithErrorKey
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(thirdPartyGoodTransportPage(formWithErrors))),
        yesNoAnswer => saveAndRedirect(yesNoAnswer).map(navigator.continueTo)
      )
  }

  private def populateForm(form: Form[YesNoAnswer])(implicit request: JourneyRequest[_]): Form[YesNoAnswer] =
    request.cacheModel.parties.carrierDetails match {
      case Some(carrier) if carrier.details.nonEmpty                                   => form.fill(YesNoAnswer.Yes.get)
      case _ if request.cacheModel.parties.consigneeDetails.exists(_.details.nonEmpty) => form.fill(YesNoAnswer.No.get)
      case _                                                                           => form
    }

  private def saveAndRedirect(answer: YesNoAnswer)(implicit request: JourneyRequest[_]): Future[Call] =
    Some(answer) match {
      case YesNoAnswer.Yes => Future.successful(CarrierEoriNumberController.displayPage)

      case _ =>
        updateDeclarationFromRequest(model => model.copy(parties = model.parties.copy(carrierDetails = None)))
          .map(_ => ConsigneeDetailsController.displayPage)
    }
}
