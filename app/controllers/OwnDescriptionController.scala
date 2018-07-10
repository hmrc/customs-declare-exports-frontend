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

import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.{OwnDescriptionData, OwnDescriptionFormProvider}
import identifiers.OwnDescriptionId
import models.Mode
import play.api.libs.json.Json
import utils.{Navigator, UserAnswers}
import views.html.ownDescription

import scala.concurrent.Future


class OwnDescriptionController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         dataCacheConnector: DataCacheConnector,
                                         navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: OwnDescriptionFormProvider) extends FrontendController with I18nSupport {

  val form: Form[OwnDescriptionData] = formProvider()
  import OwnDescriptionData._

  def onPageLoad(mode: Mode) = (authenticate andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.ownDescription) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(ownDescription(appConfig, preparedForm, mode))
  }

  def onSubmit(mode: Mode) = (authenticate).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          Future.successful(BadRequest(ownDescription(appConfig, formWithErrors, mode)))
        },
        (value) => {
          dataCacheConnector.save[OwnDescriptionData](request.user.externalId, OwnDescriptionId.toString, value).map(cacheMap =>
            Redirect(navigator.nextPage(OwnDescriptionId, mode)(new UserAnswers(cacheMap))))
  }
      )
  }
}
