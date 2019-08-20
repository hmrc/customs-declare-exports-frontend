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

import controllers.actions.{AuthAction, JourneyAction}
import forms.declaration.ConsignmentReferences
import javax.inject.Inject
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.consignment_references

import scala.concurrent.{ExecutionContext, Future}

class ConsignmentReferencesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  consignmentReferencesPage: consignment_references
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.consignmentReferences match {
      case Some(data) => Ok(consignmentReferencesPage(ConsignmentReferences.form().fill(data)))
      case _          => Ok(consignmentReferencesPage(ConsignmentReferences.form()))
    }
  }

  def submitConsignmentReferences(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    ConsignmentReferences.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ConsignmentReferences]) =>
          Future.successful(BadRequest(consignmentReferencesPage(formWithErrors))),
        validConsignmentReferences =>
          updateCache(validConsignmentReferences)
            .map(_ => Redirect(controllers.declaration.routes.ExporterDetailsController.displayForm()))
      )
  }

  private def updateCache(
    formData: ConsignmentReferences
  )(implicit req: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      model.copy(consignmentReferences = Some(formData))
    })

}
