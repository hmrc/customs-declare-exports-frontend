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
import connectors.DataCacheConnector
import controllers.actions._
import forms.RepresentativesAddressFormProvider
import identifiers.RepresentativesAddressId
import javax.inject.Inject
import models.{Address, NormalMode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import views.html.representativesAddress

import scala.concurrent.Future

class RepresentativesAddressController @Inject()(
    appConfig: AppConfig,
    override val messagesApi: MessagesApi,
    dataCacheConnector: DataCacheConnector,
    navigator: Navigator,
    authenticate: AuthAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: RepresentativesAddressFormProvider)
  extends FrontendController with I18nSupport {

  val form: Form[Address] = formProvider()

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.representativesAddress match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(representativesAddress(appConfig, preparedForm, NormalMode))
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(representativesAddress(appConfig, formWithErrors, NormalMode))),
        value =>
          dataCacheConnector.save[Address](request.externalId, RepresentativesAddressId.toString, value)
            .map { cacheMap =>
              navigator.redirect(RepresentativesAddressId, NormalMode, cacheMap)
            }
      )
  }
}
