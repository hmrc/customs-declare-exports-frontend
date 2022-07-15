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
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.CatOrDogFurDetails
import forms.declaration.CatOrDogFurDetails.{EducationalOrTaxidermyPurposes, InvalidPurpose}
import models.CannotExportGoodsReason.CatAndDogFur
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.cat_or_dog_fur

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CatOrDogFurController @Inject() (
  authenticate: AuthAction,
  mcc: MessagesControllerComponents,
  override val exportsCacheService: ExportsCacheService,
  journeyType: JourneyAction,
  catOrDogFurPage: cat_or_dog_fur,
  navigator: Navigator
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithDefaultFormBinding with ModelCacheable with SubmissionErrors {

  private val selectedJourneys = journeyType(Seq(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL))

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen selectedJourneys) { implicit request =>
    val form = CatOrDogFurDetails.form.withSubmissionErrors

    request.cacheModel
      .itemBy(itemId)
      .flatMap(_.catOrDogFurDetails)
      .fold {
        Ok(catOrDogFurPage(mode, form, itemId))
      } { details =>
        Ok(catOrDogFurPage(mode, form.fill(details), itemId))
      }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen selectedJourneys).async { implicit request =>
    CatOrDogFurDetails.form.bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(catOrDogFurPage(mode, formWithErrors, itemId))),
        catOrDogFurDetails => updateExportsCache(itemId, catOrDogFurDetails).map(_ => nextPage(mode, itemId, catOrDogFurDetails))
      )
  }

  private def nextPage(mode: Mode, itemId: String, catOrDogFurDetails: CatOrDogFurDetails)(implicit request: JourneyRequest[AnyContent]): Result =
    catOrDogFurDetails match {
      case CatOrDogFurDetails(YesNoAnswers.yes, Some(EducationalOrTaxidermyPurposes)) | CatOrDogFurDetails(YesNoAnswers.no, _) =>
        navigator.continueTo(mode, routes.UNDangerousGoodsCodeController.displayPage(_, itemId))
      case _ => Redirect(controllers.routes.CannotExportGoodsController.displayPage(CatAndDogFur))
    }

  private def updateExportsCache(itemId: String, catOrDogFurDetails: CatOrDogFurDetails)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] = {
    // If invalid purpose is selected we must not save this in the cache and warn the user
    val finalCatOrDogFurDetails =
      if (catOrDogFurDetails == CatOrDogFurDetails(YesNoAnswers.yes, Some(InvalidPurpose))) None else Some(catOrDogFurDetails)

    updateDeclarationFromRequest { declaration =>
      declaration.updatedItem(itemId, item => item.copy(catOrDogFurDetails = finalCatOrDogFurDetails))
    }
  }
}
