/*
 * Copyright 2019 HM Revenue & Customs
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

package test.controllers

import config.AppConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, JourneyAction}
import javax.inject.{Inject, Singleton}
import models.SignedInUser
import play.api.Logger
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{Action, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.MetaData

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestingUtilitiesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  connector: CustomsDeclareExportsConnector
)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController {

  def submitDeclarationXml: Action[String] = authenticate.async(parse.tolerantText) { implicit authenticatedRequest =>
    implicit val user: SignedInUser = authenticatedRequest.user

    Logger.debug("######### Starting submit declaration")

    val metaData = MetaData.fromXml(authenticatedRequest.request.body)
    val lrnOpt = metaData.declaration.flatMap(_.functionalReferenceId)
    val ducrOpt = metaData.declaration.flatMap(_.goodsShipment.flatMap(_.ucr.flatMap(_.traderAssignedReferenceId)))

    val result = for {
      lrn <- lrnOpt
      ducr <- ducrOpt
    } yield callConnector(lrn, ducr, metaData)

    result.getOrElse(Future.successful(InternalServerError))
  }

  private def callConnector(lrn: String, ducr: String, metaData: MetaData)(implicit hc: HeaderCarrier): Future[Result] =
    connector.submitExportDeclaration(ducr, Some(lrn), metaData).map { _ =>
      Logger.debug("######### Declaration submitted successfully")
      Created("")
    } recover {
      case e: Throwable =>
        Logger.error("Error calling backend", e)
        InternalServerError("Error calling backend")
    }
}

case class SubmissionWrapper(conversationId: String, lrn: Option[String] = None, mrn: Option[String] = None)

object SubmissionWrapper {

  implicit val format: OFormat[SubmissionWrapper] = Json.format[SubmissionWrapper]

}
