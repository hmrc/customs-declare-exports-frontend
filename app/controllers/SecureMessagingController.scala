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

import java.net.URLEncoder.encode

import scala.concurrent.ExecutionContext

import connectors.SecureMessagingFrontendConnector
import uk.gov.hmrc.play.partials.HeaderCarrierForPartialsConverter
import controllers.actions.{AuthAction, SecureMessagingAction, VerifiedEmailAction}
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.messaging.{inbox_wrapper, partial_wrapper}

@Singleton
class SecureMessagingController @Inject() (
  authenticate: AuthAction,
  verifiedEmail: VerifiedEmailAction,
  secureMessagingAction: SecureMessagingAction,
  secureMessagingFrontendConnector: SecureMessagingFrontendConnector,
  mcc: MessagesControllerComponents,
  inbox_wrapper: inbox_wrapper,
  partial_wrapper: partial_wrapper,
  headerCarrierForPartialsConverter: HeaderCarrierForPartialsConverter
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  val actions = authenticate andThen verifiedEmail andThen secureMessagingAction

  val displayInbox: Action[AnyContent] = actions.async { implicit request =>
    implicit val hc = headerCarrierForPartialsConverter.fromRequestWithEncryptedCookie(request)
    secureMessagingFrontendConnector
      .retrieveInboxPartial(request.user.eori)
      .map { partial =>
        Ok(inbox_wrapper(HtmlFormat.raw(partial.body)))
      }
  }

  def displayConversation(client: String, conversationId: String): Action[AnyContent] = actions.async { implicit request =>
    implicit val hc = headerCarrierForPartialsConverter.fromRequestWithEncryptedCookie(request)
    secureMessagingFrontendConnector
      .retrieveConversationPartial(client, conversationId)
      .map(partial =>
        Ok(
          partial_wrapper(
            HtmlFormat.raw(partial.body),
            "conversation.heading",
            defineUploadLink(routes.SecureMessagingController.displayConversation(client, conversationId).url),
            Some(routes.SecureMessagingController.displayInbox)
          )
        )
      )
  }

  def displayReplyResult(client: String, conversationId: String): Action[AnyContent] = actions.async { implicit request =>
    implicit val hc = headerCarrierForPartialsConverter.fromRequestWithEncryptedCookie(request)
    secureMessagingFrontendConnector
      .retrieveReplyResult(client, conversationId)
      .map(partial =>
        Ok(
          partial_wrapper(
            HtmlFormat.raw(partial.body),
            "replyResult.heading",
            defineUploadLink(routes.SecureMessagingController.displayReplyResult(client, conversationId).url)
          )
        )
      )
  }

  def submitReply(client: String, conversationId: String): Action[AnyContent] = actions.async { implicit request =>
    val formData = request.body.asFormUrlEncoded.getOrElse(Map.empty)

    secureMessagingFrontendConnector
      .submitReply(client, conversationId, formData)
      .map { maybeErrorPartial =>
        maybeErrorPartial match {
          case None => Redirect(routes.SecureMessagingController.displayReplyResult(client, conversationId))
          case Some(partial) =>
            Ok(
              partial_wrapper(
                HtmlFormat.raw(partial.body),
                "replyResult.heading",
                defineUploadLink(routes.SecureMessagingController.displayConversation(client, conversationId).url),
                Some(routes.SecureMessagingController.displayInbox)
              )
            )
        }
      }
  }

  private def defineUploadLink(refererUrl: String) = {
    val encodedRefererUrl = encode(refererUrl, "UTF-8")
    s"${routes.DeclarationDetailsController.displayPage(encodedRefererUrl).url}#action-submissions"
  }
}
