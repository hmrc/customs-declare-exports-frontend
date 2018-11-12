/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers

import config.AppConfig
import connectors.CustomsInventoryLinkingExportsConnector
import controllers.actions.AuthAction
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import views.html.{arrival_confirmation_page, arrivals}

import scala.concurrent.Future

class ArrivalsController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsInventoryLinkingExportsConnector: CustomsInventoryLinkingExportsConnector,
  errorHandler: ErrorHandler
) extends FrontendController with I18nSupport {

  object ArrivalForm {
    implicit val agentFormat = Json.format[AgentDetails]
    implicit val ucrFormat = Json.format[UcrBlock]
    implicit val transportFormat = Json.format[TransportDetails]
    implicit val inventoryMovementFormat = Json.format[InventoryLinkingMovementRequest]
  }

  val correctUcrType = Seq("D", "M")
  val correctMasterOpt = Seq("A", "F", "R", "X")

  val form = Form(
    mapping(
      "messageCode" -> ignored("EAL"),
      "agentDetails" -> optional(mapping(
        "eori" -> optional(text(maxLength = 17)),
        "agentLocation" -> optional(text(maxLength = 12)),
        "agentRole" -> optional(text(maxLength = 3))
      )(AgentDetails.apply)(AgentDetails.unapply)),
      "ucrBlock" -> mapping(
        "ucr" -> nonEmptyText(maxLength = 35),
        "ucrType" -> nonEmptyText.verifying(correctUcrType.contains(_))
      )(UcrBlock.apply)(UcrBlock.unapply),
      "goodsLocation" -> nonEmptyText(maxLength = 17),
      "goodsArrivalDateTime" -> optional(text),
      "goodsDepartureDateTime" -> optional(text),
      "shedOPID" -> optional(text(maxLength = 3)),
      "masterUCR" -> optional(text(maxLength = 35)),
      "masterOpt" -> optional(text.verifying(correctMasterOpt.contains(_))),
      "movementReference" -> optional(text(maxLength = 25)),
      "transportDetails" -> optional(mapping(
        "transportId" -> optional(text(maxLength = 35)),
        "transportMode" -> optional(text(maxLength = 1)),
        "transportNationality" -> optional(text(maxLength = 2))
      )(TransportDetails.apply)(TransportDetails.unapply))
    )(InventoryLinkingMovementRequest.apply)(InventoryLinkingMovementRequest.unapply)
  )

  def displayForm(): Action[AnyContent] =  authenticate.async { implicit request =>
    Future.successful(Ok(arrivals(appConfig, form)))
  }

  def send(): Action[AnyContent] = authenticate.async { implicit request =>
    form.bindFromRequest().fold(
      (formWithErrors: Form[InventoryLinkingMovementRequest]) =>
        Future.successful(BadRequest(arrivals(appConfig, formWithErrors))),
      form => {
        val eori = request.user.eori
        customsInventoryLinkingExportsConnector.sendArrival(eori, form.toXml).map {
          case accepted if accepted.status == ACCEPTED =>
            Ok(arrival_confirmation_page(appConfig, form.movementReference.getOrElse("Movement reference")))
          case error =>
            Logger.error(s"Error from Customs Inventory Linking ${error.toString}")
            BadRequest(
              errorHandler.standardErrorTemplate(
                pageTitle = messagesApi("global.error.title"),
                heading = messagesApi("global.error.heading"),
                message = messagesApi("global.error.message")
              )
            )
        }
      }
    )
  }
}
