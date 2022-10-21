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
import controllers.declaration.routes.{AdditionalInformationRequiredController, CommodityMeasureController}
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.packageInformation.package_information

import javax.inject.{Inject, Singleton}

@Singleton
class PackageInformationSummaryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  packageInformationPage: package_information
) extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  import PackageInformationSummaryController._

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.packageInformation) match {
      case Some(items) if items.nonEmpty =>
        Ok(packageInformationPage(itemId, yesNoForm.withSubmissionErrors(), items))

      case _ => navigator.continueTo(routes.PackageInformationAddController.displayPage(itemId))
    }
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    yesNoForm.bindFromRequest
      .fold(
        formWithErrors => {
          val items = request.cacheModel.itemBy(itemId).flatMap(_.packageInformation).getOrElse(List.empty)
          BadRequest(packageInformationPage(itemId, formWithErrors, items))
        },
        _.answer match {
          case YesNoAnswers.yes => navigator.continueTo(routes.PackageInformationAddController.displayPage(itemId))
          case YesNoAnswers.no  => navigator.continueTo(nextPage(itemId))
        }
      )
  }

  private def yesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.packageInformation.add.empty")
}

object PackageInformationSummaryController {

  def nextPage(itemId: String)(implicit request: JourneyRequest[_]): Call =
    request.declarationType match {
      case SUPPLEMENTARY | STANDARD | CLEARANCE => CommodityMeasureController.displayPage(itemId)
      case SIMPLIFIED | OCCASIONAL              => AdditionalInformationRequiredController.displayPage(itemId)
    }
}
