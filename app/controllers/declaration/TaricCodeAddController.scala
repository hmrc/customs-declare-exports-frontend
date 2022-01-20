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
import controllers.helpers.MultipleItemsHelper
import forms.declaration.TaricCode.taricCodeLimit
import forms.declaration.{TaricCode, TaricCodeFirst}
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.{taric_code_add, taric_code_add_first}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaricCodeAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  taricCodeAddFirstPage: taric_code_add_first,
  taricCodeAdd: taric_code_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.taricCodes) match {
      case Some(taricCodes) if taricCodes.nonEmpty => Ok(taricCodeAdd(mode, itemId, TaricCode.form().withSubmissionErrors()))

      case Some(_) =>
        val form = TaricCodeFirst.form().fill(TaricCodeFirst.none).withSubmissionErrors()
        Ok(taricCodeAddFirstPage(mode, itemId, commodityCode(itemId), form))

      case _ =>
        val form = TaricCodeFirst.form().withSubmissionErrors()
        Ok(taricCodeAddFirstPage(mode, itemId, commodityCode(itemId), form))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    def showFormWithErrors(formWithErrors: Form[TaricCodeFirst]): Future[Result] =
      Future.successful(BadRequest(taricCodeAddFirstPage(mode, itemId, commodityCode(itemId), formWithErrors)))

    val exportItem = request.cacheModel.itemBy(itemId)
    exportItem.flatMap(_.taricCodes) match {
      case Some(taricCodes) if taricCodes.nonEmpty =>
        saveAdditionalTaricCode(mode, itemId, TaricCode.form().bindFromRequest(), taricCodes)

      case _ =>
        TaricCodeFirst
          .form()
          .bindFromRequest()
          .fold(showFormWithErrors, validForm => saveFirstTaricCode(mode, itemId, validForm.code))
    }
  }

  private def commodityCode(itemId: String)(implicit request: JourneyRequest[_]): Option[String] =
    request.cacheModel.commodityCodeOfItem(itemId)

  private def saveFirstTaricCode(mode: Mode, itemId: String, maybeCode: Option[String])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    maybeCode match {
      case Some(code) =>
        updateExportsCache(itemId, Seq(TaricCode(code)))
          .map(_ => navigator.continueTo(mode, controllers.declaration.routes.TaricCodeSummaryController.displayPage(_, itemId)))

      case None =>
        updateExportsCache(itemId, Seq.empty)
          .map(_ => navigator.continueTo(mode, controllers.declaration.routes.NactCodeSummaryController.displayPage(_, itemId)))
    }

  private def saveAdditionalTaricCode(mode: Mode, itemId: String, boundForm: Form[TaricCode], cachedData: Seq[TaricCode])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, taricCodeLimit, "taricCode", "declaration.taricAdditionalCodes")
      .fold(
        formWithErrors => Future.successful(BadRequest(taricCodeAdd(mode, itemId, formWithErrors))),
        updatedCache =>
          updateExportsCache(itemId, updatedCache)
            .map(_ => navigator.continueTo(mode, controllers.declaration.routes.TaricCodeSummaryController.displayPage(_, itemId)))
      )

  private def updateExportsCache(itemId: String, updatedCache: Seq[TaricCode])(
    implicit r: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.updatedItem(itemId, _.copy(taricCodes = Some(updatedCache.toList))))
}
