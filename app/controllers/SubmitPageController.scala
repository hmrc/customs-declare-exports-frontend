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

import api.declaration.{Declarant, Declaration, SubmitDeclaration}
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.SubmitPageFormProvider
import identifiers.SubmitPageId
import models.Mode
import utils.{Navigator, UserAnswers}
import views.html.submitPage

import scala.concurrent.Future

class SubmitPageController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: SubmitPageFormProvider,
                                        submitDeclaration: SubmitDeclaration) extends FrontendController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode) = (authenticate andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.submitPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(submitPage(appConfig, preparedForm, mode))
  }

  def onSubmit(mode: Mode) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(submitPage(appConfig, formWithErrors, mode))),
        (value) => {
          val bearerToken = hc.authorization.map(_.value).getOrElse("Non CSP")

          submitDeclaration.submit(new Declaration(new Declarant("123")), bearerToken).flatMap{ _ =>
            dataCacheConnector.save[String](request.externalId, SubmitPageId.toString, value).map(cacheMap =>
              Redirect(navigator.nextPage(SubmitPageId, mode)(new UserAnswers(cacheMap))))
          }
        }
      )
  }
}
