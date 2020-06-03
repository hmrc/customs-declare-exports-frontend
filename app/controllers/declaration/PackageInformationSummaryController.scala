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
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import javax.inject.Inject
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.Mode
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.package_information

import scala.concurrent.ExecutionContext

class PackageInformationSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  packageInformationPage: package_information
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  import PackageInformationSummaryController._

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.packageInformation) match {
      case Some(items) if items.nonEmpty => Ok(packageInformationPage(mode, itemId, YesNoAnswer.form().withSubmissionErrors(), items))
      case _                             => navigator.continueTo(mode, routes.PackageInformationAddController.displayPage(_, itemId))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val items = request.cacheModel.itemBy(itemId).flatMap(_.packageInformation).getOrElse(List.empty)
    YesNoAnswer
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) => BadRequest(packageInformationPage(mode, itemId, formWithErrors, items)),
        validYesNo =>
          validYesNo.answer match {
            case YesNoAnswers.yes => navigator.continueTo(mode, controllers.declaration.routes.PackageInformationAddController.displayPage(_, itemId))
            case YesNoAnswers.no  => navigator.continueTo(mode, nextPage(itemId))
        }
      )
  }

}

object PackageInformationSummaryController {

  def nextPage(itemId: String)(implicit request: JourneyRequest[_]): Mode => Call =
    request.declarationType match {
      case SUPPLEMENTARY | STANDARD | CLEARANCE =>
        controllers.declaration.routes.CommodityMeasureController.displayPage(_, itemId)
      case SIMPLIFIED | OCCASIONAL =>
        controllers.declaration.routes.AdditionalInformationRequiredController.displayPage(_, itemId)
    }
}
