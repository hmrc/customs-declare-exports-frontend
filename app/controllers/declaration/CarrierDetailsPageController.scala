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
import forms.declaration.CarrierDetails
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.carrier_details

import scala.concurrent.{ExecutionContext, Future}

/**
  * This controller is not used in supp dec journey
  */
class CarrierDetailsPageController @Inject()(
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController with I18nSupport {

  implicit val countries = services.Countries.allCountries

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[CarrierDetails](cacheId, CarrierDetails.id).map {
      case Some(data) => Ok(carrier_details(CarrierDetails.form.fill(data)))
      case _          => Ok(carrier_details(CarrierDetails.form))
    }
  }

  def saveAddress(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    CarrierDetails.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[CarrierDetails]) => Future.successful(BadRequest(carrier_details(formWithErrors))),
        form =>
          customsCacheService.cache[CarrierDetails](cacheId, CarrierDetails.id, form).map { _ =>
            Redirect(controllers.declaration.routes.DeclarationAdditionalActorsController.displayForm())
        }
      )
  }
}
