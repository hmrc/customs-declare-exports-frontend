/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.section3

import connectors.CodeListConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.routes.RootController
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section3.routes.OfficeOfExitController
import forms.section3.LocationOfGoods
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.TaggedAuthCodes
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.validators.forms.AutoCompleteFieldBinding
import views.helpers.LocationOfGoodsHelper
import views.html.section3.location_of_goods

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LocationOfGoodsController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  mcc: MessagesControllerComponents,
  locationOfGoods: location_of_goods,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  taggedAuthCodes: TaggedAuthCodes
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector, auditService: AuditService)
    extends FrontendController(mcc) with AutoCompleteFieldBinding with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (taggedAuthCodes.skipLocationOfGoods(request.cacheModel)) Redirect(RootController.displayPage)
    else if (request.cacheModel.isAmendmentDraft) nextPage
    else {
      val version = LocationOfGoodsHelper.versionSelection
      val form = LocationOfGoods.form(version).withSubmissionErrors
      request.cacheModel.locations.goodsLocation match {
        case Some(data) => Ok(locationOfGoods(form.fill(data.toForm)))
        case _          => Ok(locationOfGoods(form))
      }
    }
  }

  val saveLocation: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    if (taggedAuthCodes.skipLocationOfGoods(request.cacheModel)) Future.successful(Redirect(RootController.displayPage))
    else if (request.cacheModel.isAmendmentDraft) Future.successful(nextPage)
    else
      LocationOfGoods
        .form(LocationOfGoodsHelper.versionSelection)
        .bindFromRequest(formValuesFromRequest(LocationOfGoods.locationId))
        .fold(
          formWithErrors => Future.successful(BadRequest(locationOfGoods(formWithErrors))),
          locationOfGoods => updateDeclarationFromRequest(updateDeclaration(_, locationOfGoods)).map(_ => nextPage)
        )
  }

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(OfficeOfExitController.displayPage)

  private val updateDeclaration =
    (declaration: ExportsDeclaration, locationOfGoods: LocationOfGoods) =>
      declaration.copy(locations = declaration.locations.copy(goodsLocation = Some(locationOfGoods.toModel)))
}
