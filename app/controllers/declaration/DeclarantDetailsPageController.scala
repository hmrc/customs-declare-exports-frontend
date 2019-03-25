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
import controllers.actions.AuthAction
import controllers.util.CacheIdGenerator.supplementaryCacheId
import forms.declaration.DeclarantDetails
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.declarant_details

import scala.concurrent.{ExecutionContext, Future}

class DeclarantDetailsPageController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  implicit val countries = services.Countries.allCountries

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[DeclarantDetails](supplementaryCacheId, DeclarantDetails.id).map {
      case Some(data) => Ok(declarant_details(appConfig, DeclarantDetails.form.fill(data)))
      case _          => Ok(declarant_details(appConfig, DeclarantDetails.form))
    }
  }

  def saveAddress(): Action[AnyContent] = authenticate.async { implicit request =>
    DeclarantDetails.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[DeclarantDetails]) =>
          Future.successful(BadRequest(declarant_details(appConfig, formWithErrors))),
        form =>
          customsCacheService.cache[DeclarantDetails](supplementaryCacheId, DeclarantDetails.id, form).map { _ =>
            Redirect(
              controllers.declaration.routes.RepresentativeDetailsPageController.displayRepresentativeDetailsPage()
            )
        }
      )
  }
}
