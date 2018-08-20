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
import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import forms.DeclarationSummary
import identifiers.SubmitDeclarationId
import javax.inject.Inject
import models.NormalMode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import views.html.declarationSummary

class DeclarationSummaryController @Inject()(
    appConfig: FrontendAppConfig,
    override val messagesApi: MessagesApi,
    dataCacheConnector: DataCacheConnector,
    navigator: Navigator,
    authenticate: AuthAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    submitDeclaration: SubmitDeclaration)
  extends FrontendController
  with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (authenticate andThen getData) {
    implicit request =>
      val declaration = request.userAnswers match {
        case None => DeclarationSummary()
        case Some(answers) => DeclarationSummary.buildFromAnswers(answers)
      }

      Ok(declarationSummary(appConfig, declaration, NormalMode))
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData).async {
    implicit request =>
      // Below declaration from users answers
      // val declaration = DeclarationSummary.buildFromAnswers(request.userAnswers)
      // TODO No CSP is only for test purposes, we should throw exception here
      val bearerToken = hc.authorization.map(_.value).getOrElse("Non CSP")

      // TODO generate declaration based on user answers
      submitDeclaration.submit(new Declaration(new Declarant("123")), bearerToken).flatMap{ _ =>
        dataCacheConnector.clearAndRetrieveCache(request.externalId).map{ cache =>
          navigator.redirect(SubmitDeclarationId, NormalMode, cache)
        }
      }
  }
}
