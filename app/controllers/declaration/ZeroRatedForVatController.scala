/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.declaration.{NactCode, NactCodeFirst, ZeroRatedForVat}
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.zero_rated_for_vat

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ZeroRatedForVatController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  zero_rated_for_vat: zero_rated_for_vat
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  val validTypes = Seq(DeclarationType.STANDARD)

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.nactExemptionCode) match {
      case Some(code) => Ok(zero_rated_for_vat(mode, itemId, ZeroRatedForVat.form().withSubmissionErrors.fill(code), eligibleForZeroVat(itemId)))
      case _          => Ok(zero_rated_for_vat(mode, itemId, ZeroRatedForVat.form().withSubmissionErrors(), eligibleForZeroVat(itemId)))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    ZeroRatedForVat
      .form()
      .bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(zero_rated_for_vat(mode, itemId, formWithErrors, eligibleForZeroVat(itemId)))),
        updatedCache =>
          updateExportsCache(itemId, updatedCache)
            .map(_ => navigator.continueTo(mode, routes.NactCodeSummaryController.displayPage(_, itemId)))
      )
  }

  private def updateExportsCache(itemId: String, updatedCache: NactCode)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.updatedItem(itemId, _.copy(nactExemptionCode = Some(updatedCache))))

  val procedureCodesRestrictingZeroVat = List("1007", "1042", "2151", "2154", "2200", "3151", "3154", "3171", "2100", "2144", "2244", "2300", "3153")

  private def eligibleForZeroVat(itemId: String)(implicit request: JourneyRequest[_]): Boolean =
    request.cacheModel.procedureCodeOfItem(itemId).flatMap(_.procedureCode).fold(false)(procedureCodesRestrictingZeroVat.contains)

}
