/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.navigation.Navigator
import forms.declaration.ConsignmentReferences
import forms.declaration.ConsignmentReferences.form
import javax.inject.Inject
import models.DeclarationStatus.DeclarationStatus
import models.requests.JourneyRequest
import models.{DeclarationStatus, DeclarationType, ExportsDeclaration, Mode}
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
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  consignmentReferencesPage: consignment_references
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form().withSubmissionErrors()
    request.cacheModel.consignmentReferences match {
      case Some(data) => Ok(consignmentReferencesPage(mode, frm.fill(data)))
      case _          => Ok(consignmentReferencesPage(mode, frm))
    }
  }

  def submitConsignmentReferences(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ConsignmentReferences]) => Future.successful(BadRequest(consignmentReferencesPage(mode, formWithErrors))),
        validConsignmentReferences =>
          updateCache(validConsignmentReferences)
            .map(
              _ =>
                navigator.continueTo(mode, request.declarationType match {
                  case DeclarationType.CLEARANCE => controllers.declaration.routes.EntryIntoDeclarantsRecordsController.displayPage
                  case _                         => controllers.declaration.routes.DeclarantDetailsController.displayPage
                })
          )
      )
  }

  private def updateCache(formData: ConsignmentReferences)(implicit req: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      model.copy(status = DeclarationStatus.DRAFT, consignmentReferences = Some(formData))
    })

}
