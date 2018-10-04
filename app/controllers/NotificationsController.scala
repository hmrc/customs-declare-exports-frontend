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
import connectors.CustomsDeclareExportsConnector
import controllers.actions.AuthAction
import javax.inject.Inject
import models.Notification
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

class NotificationsController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector
) extends FrontendController with I18nSupport {

  def listOfNotifications(): Action[AnyContent] = authenticate.async { implicit request =>
    val eori = request.user.eori
    customsDeclareExportsConnector.fetchNotifications(eori).map { results =>
      val notifications = Notification.randomNotifications()
      Ok(views.html.notifications(appConfig, notifications))
    }
  }
}
