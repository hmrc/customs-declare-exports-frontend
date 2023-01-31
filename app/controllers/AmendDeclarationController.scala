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

package controllers

import controllers.actions.{AuthAction, VerifiedEmailAction}
import forms.Lrn
import handlers.ErrorHandler
import models.requests.{AuthenticatedRequest, ExportsSessionKeys}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.Future

class AmendDeclarationController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents
) extends FrontendController(mcc) with I18nSupport with Logging with WithDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    getSessionData() match {
      case Some((_, mrn, lrn, ducr)) => Future.successful(Ok(""))
      case _                         => errorHandler.displayErrorPage
    }
  }

  private def getSessionData()(implicit request: AuthenticatedRequest[_]): Option[(String, String, Lrn, String)] =
    for {
      submissionId <- getSessionValue(ExportsSessionKeys.submissionId)
      mrn <- getSessionValue(ExportsSessionKeys.submissionMrn)
      lrn <- getSessionValue(ExportsSessionKeys.submissionLrn).map(Lrn(_))
      ducr <- getSessionValue(ExportsSessionKeys.submissionDucr)
    } yield (submissionId, mrn, lrn, ducr)

  private def getSessionValue(str: String)(implicit request: AuthenticatedRequest[_]): Option[String] = request.session.get(str)
}
