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

package controllers.declaration
import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.cacheId
import forms.declaration.SupervisingCustomsOffice
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.supervising_office

import scala.concurrent.{ExecutionContext, Future}

class SupervisingCustomsOfficeController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction, journeyType: JourneyAction,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  import forms.declaration.SupervisingCustomsOffice._

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[SupervisingCustomsOffice](cacheId, formId).map {
      case Some(data) => Ok(supervising_office(appConfig, form.fill(data)))
      case _          => Ok(supervising_office(appConfig, form))
    }
  }

  def saveSupervisingOffice(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[SupervisingCustomsOffice]) =>
          Future.successful(BadRequest(supervising_office(appConfig, formWithErrors))),
        validForm =>
          customsCacheService.cache[SupervisingCustomsOffice](cacheId, formId, validForm).map { _ =>
            Redirect(controllers.declaration.routes.WarehouseIdentificationController.displayForm())
        }
      )
  }
}
