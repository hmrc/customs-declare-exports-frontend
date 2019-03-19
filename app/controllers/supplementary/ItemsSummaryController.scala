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
import controllers.util.CacheIdGenerator.{itemsId, supplementaryCacheId}
import handlers.ErrorHandler
import javax.inject.Inject
import models.DeclarationFormats._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.GovernmentAgencyGoodsItem
import views.html.supplementary.items_summary

import scala.concurrent.ExecutionContext

class ItemsSummaryController @Inject()(
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  cacheService: CustomsCacheService
)(implicit ec: ExecutionContext, appConfig: AppConfig, override val messagesApi: MessagesApi)
    extends FrontendController with I18nSupport {

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    cacheService
      .fetchAndGetEntry[Seq[GovernmentAgencyGoodsItem]](supplementaryCacheId, itemsId)
      .map(items => Ok(items_summary(items.getOrElse(Seq.empty))))
  }

}
