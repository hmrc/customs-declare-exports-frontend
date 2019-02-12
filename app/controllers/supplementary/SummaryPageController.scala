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
import connectors.CustomsDeclarationsConnector
import controllers.actions.AuthAction
import javax.inject.Inject
import models.declaration.supplementary.SupplementaryDeclarationData
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.summary.summary_page

import scala.concurrent.ExecutionContext

class SummaryPageController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsCacheService: CustomsCacheService,
  customsDeclarationConnector: CustomsDeclarationsConnector
)(implicit ec: ExecutionContext)
  extends FrontendController with I18nSupport {

  val suppDecCacheId = appConfig.appName

  def displayPage(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetch(suppDecCacheId).map {
      case Some(cacheMap) => Ok(summary_page(appConfig, SupplementaryDeclarationData(cacheMap)))
      case None           => Ok(summary_page(appConfig, SupplementaryDeclarationData()))
    }
  }

  def submitSupplementaryDeclaration(): Action[AnyContent] = ???

}
