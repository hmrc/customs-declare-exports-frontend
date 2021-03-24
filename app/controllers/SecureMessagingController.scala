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

package controllers

import scala.concurrent.ExecutionContext

import connectors.SecureMessagingConnector
import controllers.actions.{AuthAction, SecureMessagingAction, VerifiedEmailAction}
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.messaging.inbox_wrapper

@Singleton
class SecureMessagingController @Inject()(
  authenticate: AuthAction,
  verifiedEmail: VerifiedEmailAction,
  secureMessagingAction: SecureMessagingAction,
  secureMessagingConnector: SecureMessagingConnector,
  mcc: MessagesControllerComponents,
  inbox_wrapper: inbox_wrapper
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  val actions = authenticate andThen verifiedEmail andThen secureMessagingAction

  val displayInbox: Action[AnyContent] = actions.async { implicit request =>
    secureMessagingConnector
      .retrieveInboxPartial(request.user.eori)
      .map { partial =>
        Ok(inbox_wrapper(HtmlFormat.raw(partial.body)))
      }
  }
}
