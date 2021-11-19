/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.actions

import scala.concurrent.{ExecutionContext, Future}
import com.google.inject.ImplementedBy
import connectors.CustomsDeclareExportsConnector
import controllers.routes

import javax.inject.{Inject, Singleton}
import models.{EORI, Email}
import models.requests.{AuthenticatedRequest, VerifiedEmailRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

@ImplementedBy(classOf[VerifiedEmailActionImpl])
trait VerifiedEmailAction extends ActionRefiner[AuthenticatedRequest, VerifiedEmailRequest]

@Singleton
class VerifiedEmailActionImpl @Inject()(backendConnector: CustomsDeclareExportsConnector, mcc: MessagesControllerComponents)
    extends VerifiedEmailAction {

  implicit val executionContext: ExecutionContext = mcc.executionContext
  private lazy val onUnverified = Redirect(routes.UnverifiedEmailController.informUser)
  private lazy val onUndeclared = Redirect(routes.UndeclaredEmailController.informUser)

  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, VerifiedEmailRequest[A]]] = {

    val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    backendConnector.getVerifiedEmailAddress(EORI(request.user.eori))(hc, executionContext).map {
      case Some(Email(address, true))  => Right(VerifiedEmailRequest(request, address))
      case Some(Email(_, false)) => Left(onUndeclared)
      case _                           => Left(onUnverified)
    }
  }
}
