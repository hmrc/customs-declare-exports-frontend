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

package controllers

import connectors.CustomsDeclareExportsConnector
import controllers.actions.AuthAction
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{notifications, submission_notifications}

import scala.concurrent.ExecutionContext

class NotificationsController @Inject()(
  authenticate: AuthAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents,
  notificationsPage: notifications,
  submissionsNotificationPage: submission_notifications
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def listOfNotifications(): Action[AnyContent] = authenticate.async { implicit request =>
    customsDeclareExportsConnector.fetchNotifications().map { results =>
      Ok(notificationsPage(request.user.eori, results.sorted.reverse))
    }
  }

  def listOfNotificationsForSubmission(mrn: String): Action[AnyContent] =
    authenticate.async { implicit request =>
      customsDeclareExportsConnector.fetchNotificationsByMrn(mrn).map { results =>
        Ok(submissionsNotificationPage(results.sorted.reverse))
      }
    }

}
