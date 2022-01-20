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

import scala.concurrent.{ExecutionContext, Future}

import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import controllers.helpers.MultipleItemsHelper
import forms.declaration.NactCode.nactCodeLimit
import forms.declaration.{NactCode, NactCodeFirst}
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.{nact_code_add, nact_code_add_first}

class NactCodeAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  nactCodeAddFirstPage: nact_code_add_first,
  nactCodeAdd: nact_code_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  import NactCodeSummaryController._

  val validTypes = Seq(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL)

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    val maybeItem = request.cacheModel.itemBy(itemId)
    maybeItem.flatMap(_.nactCodes) match {
      case Some(nactCode) if nactCode.nonEmpty => Ok(nactCodeAdd(mode, itemId, NactCode.form.withSubmissionErrors))

      case Some(_) =>
        val form = NactCodeFirst.form.fill(NactCodeFirst(None)).withSubmissionErrors
        Ok(nactCodeAddFirstPage(mode, itemId, form))

      case _ => Ok(nactCodeAddFirstPage(mode, itemId, NactCodeFirst.form.withSubmissionErrors))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    val maybeItem = request.cacheModel.itemBy(itemId)
    maybeItem.flatMap(_.nactCodes) match {
      case Some(nactCodes) if nactCodes.nonEmpty => saveAdditionalNactCode(mode, itemId, NactCode.form.bindFromRequest, nactCodes)

      case _ =>
        NactCodeFirst.form.bindFromRequest
          .fold(
            (formWithErrors: Form[NactCodeFirst]) => Future.successful(BadRequest(nactCodeAddFirstPage(mode, itemId, formWithErrors))),
            validForm => saveFirstNactCode(mode, itemId, validForm.code)
          )
    }
  }

  private def saveFirstNactCode(mode: Mode, itemId: String, maybeCode: Option[String])(implicit request: JourneyRequest[AnyContent]) =
    maybeCode match {
      case Some(code) =>
        updateExportsCache(itemId, Seq(NactCode(code)))
          .map(_ => navigator.continueTo(mode, routes.NactCodeSummaryController.displayPage(_, itemId)))

      case None => updateExportsCache(itemId, Seq.empty).map(_ => navigator.continueTo(mode, nextPage(itemId)))
    }

  private def saveAdditionalNactCode(mode: Mode, itemId: String, boundForm: Form[NactCode], cachedData: Seq[NactCode])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, nactCodeLimit, "nactCode", "declaration.nationalAdditionalCode")
      .fold(
        formWithErrors => Future.successful(BadRequest(nactCodeAdd(mode, itemId, formWithErrors))),
        updatedCache =>
          updateExportsCache(itemId, updatedCache)
            .map(_ => navigator.continueTo(mode, routes.NactCodeSummaryController.displayPage(_, itemId)))
      )

  private def updateExportsCache(itemId: String, updatedCache: Seq[NactCode])(
    implicit r: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.updatedItem(itemId, _.copy(nactCodes = Some(updatedCache.toList))))
}
