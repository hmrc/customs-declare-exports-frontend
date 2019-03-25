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
import forms.declaration.WarehouseIdentification
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.warehouse_identification

import scala.concurrent.{ExecutionContext, Future}

class WarehouseIdentificationController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction, journeyType: JourneyAction,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  import forms.declaration.WarehouseIdentification._

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[WarehouseIdentification](cacheId, formId).map {
      case Some(data) => Ok(warehouse_identification(appConfig, form.fill(data)))
      case _          => Ok(warehouse_identification(appConfig, form))
    }
  }

  def saveWarehouse(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[WarehouseIdentification]) =>
          Future.successful(BadRequest(warehouse_identification(appConfig, formWithErrors))),
        form =>
          customsCacheService.cache[WarehouseIdentification](cacheId, formId, form).map { _ =>
            Redirect(controllers.declaration.routes.ItemsSummaryController.displayForm())
        }
      )
  }
}
