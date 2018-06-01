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

package uk.gov.hmrc.decex.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.decex.config.AppConfig
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.AuthProviders
import uk.gov.hmrc.decex.model.Consolidation
import uk.gov.hmrc.play.bootstrap.controller.{FrontendController, UnauthorisedAction}

import scala.concurrent.Future

@Singleton
class IsThisPartOfAConsolidation @Inject()(val messagesApi: MessagesApi, implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  val consolidationForm = Form(
    mapping(
      "id" -> text,
      "ducr" -> nonEmptyText(maxLength = 35)
    )(Consolidation.apply)(Consolidation.unapply)
  )

  def showChoice = Action.async { implicit request =>
    Future.successful(Ok(uk.gov.hmrc.decex.views.html.choose_consolidation(consolidationForm)))
  }

  def choose = Action.async { implicit request =>

    println("I am choosing...")
    
    Future.successful(Ok(uk.gov.hmrc.decex.views.html.choose_consolidation(consolidationForm)))
  }

//  def choose = UnauthorisedAction.async { implicit request =>
//
//    Logger.info(s"You clicked the save button with ducr: ${request.queryString.get("ducr")}")
//
//    consolidationForm.bindFromRequest().fold(
//      formWithErrors => Future.successful(BadRequest(uk.gov.hmrc.decex.views.html.choose_consolidation(formWithErrors))),
//
//      consolidation => {
//        val consolidationWithId = DeclarationStore.save(consolidation)
//        consolidationForm.bind(Map("id" -> consolidationWithId.id))
////        Future.successful(Ok)
//        Future.successful(Ok(uk.gov.hmrc.decex.views.html.choose_consolidation(consolidationForm)))
//      }
//    )
//  }


}


