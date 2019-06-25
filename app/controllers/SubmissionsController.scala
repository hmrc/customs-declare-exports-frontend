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

import config.AppConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.AuthAction
import javax.inject.Inject
import models.declaration.submissions.{SubmissionStatus, UnknownStatus}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext

class SubmissionsController @Inject()(
  appConfig: AppConfig,
  authenticate: AuthAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayListOfSubmissions(): Action[AnyContent] = authenticate.async { implicit request =>
    for {
      submissions <- customsDeclareExportsConnector.fetchSubmissions()
      notifications <- customsDeclareExportsConnector.fetchNotifications()
      result = submissions.map { submission =>
        val conversationIds = submission.actions.map(_.conversationId)
        val notificationsAmount =
          notifications.count(notification => conversationIds.contains(notification.conversationId))
        val submissionStatus = notifications.filter(n => conversationIds.contains(n.conversationId)).sorted.reverse.headOption.map(n => SubmissionStatus.retrieve(n.conversationId, n.nameCode))
        (submission, notificationsAmount, submissionStatus.getOrElse(UnknownStatus))
      }

    } yield Ok(views.html.submissions(appConfig, result))
  }

}
