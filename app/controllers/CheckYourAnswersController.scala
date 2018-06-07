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

import com.google.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import utils.CheckYourAnswersHelper
import viewmodels.AnswerSection
import views.html.check_your_answers
import config.FrontendAppConfig
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction) extends FrontendController with I18nSupport {

  def onPageLoad() = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val checkYourAnswersHelper = new CheckYourAnswersHelper(request.userAnswers)
      val sections = Seq(AnswerSection(None, Seq()))
      Ok(check_your_answers(appConfig, sections))
  }
}
