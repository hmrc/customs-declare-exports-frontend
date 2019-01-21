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

package controllers.supplementary

import config.AppConfig
import controllers.actions.AuthAction
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import forms.supplementary.GoodItemNumber
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import views.html.supplementary.good_item_number

import scala.concurrent.{ExecutionContext, Future}

class GoodItemNumberController @Inject()(
  appConfig: AppConfig,
  val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  import forms.supplementary.GoodItemNumber._

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[GoodItemNumber](appConfig.appName, formId).map {
      case Some(data) => Ok(good_item_number(appConfig, form.fill(data)))
      case _          => Ok(good_item_number(appConfig, form))
    }
  }

  def submit(): Action[AnyContent] = authenticate.async { implicit request =>
    form
      .bindFromRequest()
      .fold( (formWithErrors: Form[GoodItemNumber]) => Future.successful(BadRequest(good_item_number(appConfig, formWithErrors))), form =>
        customsCacheService.cache[GoodItemNumber](appConfig.appName, formId, form).map { _ =>
            //Redirect(controllers.supplementary.???.???.???())
            Ok("Next page in progress")
        }
      )
  }
}
