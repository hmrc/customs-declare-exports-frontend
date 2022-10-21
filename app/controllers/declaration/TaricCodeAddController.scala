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
import controllers.declaration.routes.{TaricCodeSummaryController, ZeroRatedForVatController}
import controllers.helpers.MultipleItemsHelper
import controllers.navigation.Navigator
import forms.declaration.NatureOfTransaction._
import forms.declaration.TaricCode.taricCodeLimit
import forms.declaration.{NatureOfTransaction, TaricCode, TaricCodeFirst}
import models.DeclarationType.STANDARD
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.{taric_code_add, taric_code_add_first}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaricCodeAddController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  taricCodeAddFirstPage: taric_code_add_first,
  taricCodeAdd: taric_code_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.taricCodes) match {
      case Some(taricCodes) if taricCodes.nonEmpty => Ok(taricCodeAdd(itemId, TaricCode.form.withSubmissionErrors))

      case Some(_) =>
        val form = TaricCodeFirst.form.fill(TaricCodeFirst.none).withSubmissionErrors
        Ok(taricCodeAddFirstPage(itemId, form))

      case _ =>
        val form = TaricCodeFirst.form.withSubmissionErrors
        Ok(taricCodeAddFirstPage(itemId, form))
    }
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    def showFormWithErrors(formWithErrors: Form[TaricCodeFirst]): Future[Result] =
      Future.successful(BadRequest(taricCodeAddFirstPage(itemId, formWithErrors)))

    val exportItem = request.cacheModel.itemBy(itemId)
    exportItem.flatMap(_.taricCodes) match {
      case Some(taricCodes) if taricCodes.nonEmpty => saveAdditionalTaricCode(itemId, TaricCode.form.bindFromRequest, taricCodes)

      case _ => TaricCodeFirst.form.bindFromRequest.fold(showFormWithErrors, validForm => saveFirstTaricCode(itemId, validForm.code))
    }
  }

  private def saveFirstTaricCode(itemId: String, maybeCode: Option[String])(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    maybeCode match {
      case Some(code) =>
        updateExportsCache(itemId, Seq(TaricCode(code)))
          .map(_ => navigator.continueTo(TaricCodeSummaryController.displayPage(itemId)))

      case None =>
        val call = if (eligibleForZeroVat) ZeroRatedForVatController.displayPage(itemId) else routes.NactCodeSummaryController.displayPage(itemId)
        updateExportsCache(itemId, Seq.empty).map(_ => navigator.continueTo(call))
    }

  private def saveAdditionalTaricCode(itemId: String, boundForm: Form[TaricCode], cachedData: Seq[TaricCode])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, taricCodeLimit, "taricCode", "declaration.taricAdditionalCodes")
      .fold(
        formWithErrors => Future.successful(BadRequest(taricCodeAdd(itemId, formWithErrors))),
        updateExportsCache(itemId, _).map(_ => navigator.continueTo(TaricCodeSummaryController.displayPage(itemId)))
      )

  private def updateExportsCache(itemId: String, updatedCache: Seq[TaricCode])(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.updatedItem(itemId, _.copy(taricCodes = Some(updatedCache.toList))))

  private def eligibleForZeroVat(implicit request: JourneyRequest[_]): Boolean =
    request.cacheModel.natureOfTransaction match {
      case Some(NatureOfTransaction(`Sale`) | NatureOfTransaction(`BusinessPurchase`)) => request.declarationType == STANDARD
      case _                                                                           => false
    }
}
