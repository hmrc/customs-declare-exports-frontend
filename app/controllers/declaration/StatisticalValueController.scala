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
import controllers.navigation.Navigator
import forms.declaration.StatisticalValue
import forms.declaration.StatisticalValue.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.statistical_value

import scala.concurrent.{ExecutionContext, Future}

class StatisticalValueController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  itemTypePage: statistical_value
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.statisticalValue) match {
      case Some(itemType) => Ok(itemTypePage(mode, itemId, StatisticalValue.form.fill(itemType)))
      case _              => Ok(itemTypePage(mode, itemId, StatisticalValue.form))
    }
  }

  def submitItemType(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[StatisticalValue]) => Future.successful(BadRequest(itemTypePage(mode, itemId, formWithErrors))),
        validForm =>
          updateExportsCache(itemId, validForm).map { _ =>
            navigator
              .continueTo(controllers.declaration.routes.PackageInformationController.displayPage(mode, itemId))
        }
      )
  }

  private def updateExportsCache(itemId: String, updatedItem: StatisticalValue)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect { model =>
      model.updatedItem(itemId, item => item.copy(statisticalValue = Some(updatedItem)))
    }
}
