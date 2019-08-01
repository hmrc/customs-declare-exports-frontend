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
import forms.declaration.ConsigneeDetails
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import services.cache.{ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.consignee_details

import scala.concurrent.{ExecutionContext, Future}

/**
  * This controller is not used in supp dec journey
  */
class ConsigneeDetailsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  consigneeDetailsPage: consignee_details
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.parties.consigneeDetails match {
      case Some(data) => Ok(consigneeDetailsPage(ConsigneeDetails.form().fill(data)))
      case _          => Ok(consigneeDetailsPage(ConsigneeDetails.form()))
    }
  }

  def saveAddress(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    ConsigneeDetails.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ConsigneeDetails]) => Future.successful(BadRequest(consigneeDetailsPage(formWithErrors))),
        form =>
          for {
            _ <- updateCache(journeySessionId, form)
            _ <- customsCacheService.cache[ConsigneeDetails](cacheId, ConsigneeDetails.id, form)
          } yield Redirect(controllers.declaration.routes.DeclarantDetailsController.displayForm())
      )
  }

  private def updateCache(sessionId: String, formData: ConsigneeDetails): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(sessionId, model => {
      val updatedParties = model.parties.copy(consigneeDetails = Some(formData))
      exportsCacheService.update(sessionId, model.copy(parties = updatedParties))
    })
}
