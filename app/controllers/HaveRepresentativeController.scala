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

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import forms.HaveRepresentativeFormProvider
import identifiers.{HaveRepresentativeId, RepresentativesAddressId}
import javax.inject.Inject
import models.HaveRepresentative.{No, Yes}
import models.{HaveRepresentative, NormalMode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Enumerable, Navigator}
import views.html.haveRepresentative

import scala.concurrent.Future

class HaveRepresentativeController @Inject()(
    appConfig: FrontendAppConfig,
    override val messagesApi: MessagesApi,
    dataCacheConnector: DataCacheConnector,
    navigator: Navigator,
    authenticate: AuthAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: HaveRepresentativeFormProvider)
  extends FrontendController with I18nSupport with Enumerable.Implicits {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.haveRepresentative match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(haveRepresentative(appConfig, preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(haveRepresentative(appConfig, formWithErrors))),
        value =>
          dataCacheConnector
            .save[HaveRepresentative](request.externalId, HaveRepresentativeId.toString, value)
            .flatMap { cacheMap =>
              value match {
                case Yes =>
                  Future.successful(navigator.redirect(HaveRepresentativeId, NormalMode, cacheMap))
                case No =>
                  formProvider.clearRepresentativeCache(cacheMap).map { newCacheMap =>
                    navigator.redirect(RepresentativesAddressId, NormalMode, newCacheMap)
                  }
              }
          }
      )
  }
}
