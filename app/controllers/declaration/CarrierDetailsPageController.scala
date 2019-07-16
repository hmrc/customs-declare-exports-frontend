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
import forms.declaration.{CarrierDetails, ConsigneeDetails}
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.carrier_details

import scala.concurrent.{ExecutionContext, Future}

/**
  * This controller is not used in supp dec journey
  */
class CarrierDetailsPageController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  override val cacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  carrierDetailsPage: carrier_details
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    cacheService.get(journeySessionId).map(_.flatMap(_.parties.carrierDetails)).map {
      case Some(data) => Ok(carrierDetailsPage(CarrierDetails.form().fill(data)))
      case _          => Ok(carrierDetailsPage(CarrierDetails.form()))
    }
  }

  def saveAddress(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    CarrierDetails.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[CarrierDetails]) => Future.successful(BadRequest(carrierDetailsPage(formWithErrors))),
        form =>
          updateCache(journeySessionId, form).map { _ =>
            Redirect(controllers.declaration.routes.DeclarationAdditionalActorsController.displayForm())
        }
      )
  }

  private def updateCache(sessionId: String, formData: CarrierDetails)(implicit req: JourneyRequest[_]): Future[Unit] =
    for {
      _ <- getAndUpdateExportCacheModel(sessionId, model => {
        val updatedParties = model.parties.copy(carrierDetails = Some(formData))
        cacheService.update(sessionId, model.copy(parties = updatedParties))
      })
      _ <- customsCacheService.cache[CarrierDetails](cacheId, CarrierDetails.id, formData)
    } yield Unit
}
