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
import models.{Arrival, ArrivalForm}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{arrival_confirmation_page, arrivals}

import scala.concurrent.Future

class ArrivalsController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsInventoryLinkingExportsConnector: CustomsInventoryLinkingExportsConnector,
  errorHandler: ErrorHandler
) extends FrontendController with I18nSupport {

  val exampleXml: String =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
      |    <messageCode>EAL</messageCode>
      |    <ucrBlock>
      |        <ucr>GB/AAAA-00000</ucr>
      |        <ucrType>M</ucrType>
      |    </ucrBlock>
      |    <goodsLocation>goodsLocation</goodsLocation>
      |    <goodsArrivalDateTime>2001-12-31T12:00:00</goodsArrivalDateTime>
      |    <goodsDepartureDateTime>2001-12-31T12:00:00</goodsDepartureDateTime>
      |    <shedOPID>PID</shedOPID>
      |    <masterUCR>GB/MAAM-01010</masterUCR>
      |    <masterOpt>A</masterOpt>
      |    <movementReference>movementReference</movementReference>
      |    <transportDetails>
      |        <transportID>transportID</transportID>
      |        <transportMode>M</transportMode>
      |        <transportNationality>ZZ</transportNationality>
      |    </transportDetails>
      |</inventoryLinkingMovementRequest>""".stripMargin

  val form = Form(
    mapping(
      "ducr" -> nonEmptyText
    )(ArrivalForm.apply)(ArrivalForm.unapply)
  )

  def displayForm(): Action[AnyContent] =  authenticate.async { implicit request =>
    Future.successful(Ok(arrivals(appConfig, form)))
  }

  def send(): Action[AnyContent] = authenticate.async { implicit request =>
    form.bindFromRequest().fold(
      (formWithErrors: Form[ArrivalForm]) =>
        Future.successful(BadRequest(arrivals(appConfig, formWithErrors))),
      form => {
        val eori = request.user.eori
        val arrival = Arrival(eori, form.ducr, exampleXml)
        customsInventoryLinkingExportsConnector.sendArrival(arrival).map {
          case accepted if accepted.status == ACCEPTED => Ok(arrival_confirmation_page(appConfig, form.ducr))
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
