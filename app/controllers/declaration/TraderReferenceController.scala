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
import controllers.declaration.routes.ConfirmDucrController
import controllers.navigation.Navigator
import forms.declaration.TraderReference
import models.DeclarationType.{allDeclarationTypesExcluding, SUPPLEMENTARY}
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.trader_reference

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TraderReferenceController @Inject() (
  authorise: AuthAction,
  getJourney: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  override val exportsCacheService: ExportsCacheService,
  traderReferencePage: trader_reference
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with WithDefaultFormBinding with SubmissionErrors {

  def displayPage(): Action[AnyContent] = (authorise andThen getJourney(allDeclarationTypesExcluding(SUPPLEMENTARY))) { implicit request =>
    val traderReference = request.cacheModel.traderReference
    val form = TraderReference.form.withSubmissionErrors
    Ok(traderReferencePage(traderReference.fold(form)(value => form.fill(value))))
  }

  def submitForm(): Action[AnyContent] = (authorise andThen getJourney(allDeclarationTypesExcluding(SUPPLEMENTARY))).async { implicit request =>
    TraderReference.form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(traderReferencePage(formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(ConfirmDucrController.displayPage))
      )
  }

  private def updateCache(value: TraderReference)(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.copy(traderReference = Some(value)))
}
