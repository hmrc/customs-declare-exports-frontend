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
import forms.{ConsignmentData, ConsignmentFormProvider}
import identifiers.ConsignmentId
import javax.inject.Inject
import models.NormalMode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.consignment

import scala.concurrent.Future

class ConsignmentController @Inject()(
    appConfig: FrontendAppConfig,
    override val messagesApi: MessagesApi,
    dataCacheConnector: DataCacheConnector,
    navigator: Navigator,
    authenticate: AuthAction,
    getData: DataRetrievalAction,
    formProvider: ConsignmentFormProvider)
  extends FrontendController
  with I18nSupport
  with Enumerable.Implicits {

  val form: Form[ConsignmentData] = formProvider()

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.flatMap(_.consignment) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(consignment(appConfig, preparedForm, NormalMode))
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(consignment(appConfig, formWithErrors, NormalMode))),
        value => {
          val consignmentData = ConsignmentData.cleanConsignmentData(value)

          dataCacheConnector.save[ConsignmentData](request.externalId, ConsignmentId.toString, consignmentData)
            .map(cacheMap =>
              Redirect(navigator.nextPage(ConsignmentId, NormalMode)(new UserAnswers(cacheMap)))
            )
        }
      )
  }
}
