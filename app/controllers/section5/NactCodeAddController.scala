/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section5

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.helpers.ItemHelper.nextPageAfterNactCodePages
import controllers.helpers.MultipleItemsHelper
import controllers.navigation.Navigator
import controllers.section5.routes.NactCodeSummaryController
import forms.section5.NactCode.nactCodeLimit
import forms.section5.{NactCode, NactCodeFirst}
import models.DeclarationType.nonClearanceJourneys
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section5.nactCode.{nact_code_add, nact_code_add_first}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NactCodeAddController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  nactCodeAddFirstPage: nact_code_add_first,
  nactCodeAdd: nact_code_add
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType(nonClearanceJourneys)) { implicit request =>
    val maybeItem = request.cacheModel.itemBy(itemId)
    maybeItem.flatMap(_.nactCodes) match {
      case Some(nactCodes) if nactCodes.nonEmpty => Ok(nactCodeAdd(itemId, NactCode.form.withSubmissionErrors))

      case Some(_) => // Empty list of National codes
        val form = NactCodeFirst.form.fill(NactCodeFirst(None)).withSubmissionErrors
        Ok(nactCodeAddFirstPage(itemId, form))

      case _ => Ok(nactCodeAddFirstPage(itemId, NactCodeFirst.form.withSubmissionErrors))
    }
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType(nonClearanceJourneys)).async { implicit request =>
    val maybeItem = request.cacheModel.itemBy(itemId)
    maybeItem.flatMap(_.nactCodes) match {
      case Some(nactCodes) if nactCodes.nonEmpty => saveAdditionalNactCode(itemId, NactCode.form.bindFromRequest(), nactCodes)

      case _ =>
        NactCodeFirst.form
          .bindFromRequest()
          .fold(
            (formWithErrors: Form[NactCodeFirst]) => Future.successful(BadRequest(nactCodeAddFirstPage(itemId, formWithErrors))),
            validForm => saveFirstNactCode(itemId, validForm.code)
          )
    }
  }

  private def saveFirstNactCode(itemId: String, maybeCode: Option[String])(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    maybeCode match {
      case Some(code) =>
        updateExportsCache(itemId, Seq(NactCode(code)))
          .map(_ => navigator.continueTo(NactCodeSummaryController.displayPage(itemId)))

      case None => updateExportsCache(itemId, Seq.empty).map(_ => navigator.continueTo(nextPageAfterNactCodePages(itemId)))
    }

  private def saveAdditionalNactCode(itemId: String, boundForm: Form[NactCode], cachedData: Seq[NactCode])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, nactCodeLimit, "nactCode", "declaration.nationalAdditionalCode")
      .fold(
        formWithErrors => Future.successful(BadRequest(nactCodeAdd(itemId, formWithErrors))),
        updatedCache =>
          updateExportsCache(itemId, updatedCache)
            .map(_ => navigator.continueTo(NactCodeSummaryController.displayPage(itemId)))
      )

  private def updateExportsCache(itemId: String, updatedCache: Seq[NactCode])(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.updatedItem(itemId, _.copy(nactCodes = Some(updatedCache.toList))))
}
