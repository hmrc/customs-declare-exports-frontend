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

package controllers.declaration

import connectors.CodeLinkConnector
import connectors.Tag.CodesRestrictingZeroVat
import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.routes.NactCodeSummaryController
import controllers.helpers.ItemHelper.skipZeroRatedForVatPage
import controllers.navigation.Navigator
import forms.declaration.{NactCode, ZeroRatedForVat}
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.zero_rated_for_vat

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ZeroRatedForVatController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  codeLinkConnector: CodeLinkConnector,
  zero_rated_for_vat: zero_rated_for_vat
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val validJourneys = occasionalAndSimplified :+ STANDARD

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validJourneys)).async { implicit request =>
    if (skipZeroRatedForVatPage(request.cacheModel, itemId)) updateExportsCache(itemId, None).map(_ => redirectToNextPage(itemId))
    else {
      val form = request.cacheModel.itemBy(itemId).flatMap(_.nactExemptionCode) match {
        case Some(code) => ZeroRatedForVat.form.fill(code).withSubmissionErrors
        case _          => ZeroRatedForVat.form.withSubmissionErrors
      }

      Future.successful(Ok(zero_rated_for_vat(itemId, form, restrictedForZeroVat(itemId))))
    }
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validJourneys)).async { implicit request =>
    if (skipZeroRatedForVatPage(request.cacheModel, itemId)) updateExportsCache(itemId, None).map(_ => redirectToNextPage(itemId))
    else
      ZeroRatedForVat.form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(zero_rated_for_vat(itemId, formWithErrors, restrictedForZeroVat(itemId)))),
          code => updateExportsCache(itemId, Some(code)).map(_ => navigator.continueTo(NactCodeSummaryController.displayPage(itemId)))
        )
  }

  private lazy val procedureCodesRestrictingZeroVat = codeLinkConnector.getValidProcedureCodesForTag(CodesRestrictingZeroVat)

  private def redirectToNextPage(itemId: String): Result =
    Redirect(NactCodeSummaryController.displayPage(itemId))

  private def restrictedForZeroVat(itemId: String)(implicit request: JourneyRequest[_]): Boolean =
    request.cacheModel.procedureCodeOfItem(itemId).flatMap(_.procedureCode).fold(false)(procedureCodesRestrictingZeroVat.contains)

  private def updateExportsCache(itemId: String, maybeCode: Option[NactCode])(implicit r: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updatedItem(itemId, _.copy(nactExemptionCode = maybeCode)))
}
