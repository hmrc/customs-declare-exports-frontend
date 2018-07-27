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
import forms.WhoseDeclarationFormProvider
import identifiers.WhoseDeclarationId
import models.{Mode, NormalMode, WhoseDeclaration}
import play.api.mvc.{Action, AnyContent}
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.whoseDeclaration

import scala.concurrent.Future

class WhoseDeclarationController @Inject()(
    appConfig: FrontendAppConfig,
    override val messagesApi: MessagesApi,
    dataCacheConnector: DataCacheConnector,
    navigator: Navigator,
    authenticate: AuthAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: WhoseDeclarationFormProvider)
  extends FrontendController with I18nSupport with Enumerable.Implicits {

  val form = formProvider()

  def onPageLoad():Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.whoseDeclaration match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(whoseDeclaration(appConfig, preparedForm, NormalMode))
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(whoseDeclaration(appConfig, formWithErrors, NormalMode))),
        (value) =>
          dataCacheConnector.save[WhoseDeclaration](request.externalId, WhoseDeclarationId.toString, value).map {
            cacheMap => Redirect(navigator.nextPage(WhoseDeclarationId, NormalMode)(new UserAnswers(cacheMap)))
          }
      )
  }
}
