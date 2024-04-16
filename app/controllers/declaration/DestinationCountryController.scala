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

import connectors.CodeListConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.routes.{LocationOfGoodsController, OfficeOfExitController, RoutingCountriesController}
import controllers.navigation.Navigator
import forms.declaration.countries.Countries
import forms.declaration.countries.Countries.DestinationCountryPage
import models.DeclarationType._
import models.ExportsDeclaration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.TaggedAuthCodes
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.validators.forms.AutoCompleteFieldBinding
import views.html.declaration.destinationCountries.destination_country

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DestinationCountryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  taggedAuthCodes: TaggedAuthCodes,
  destinationCountryPage: destination_country
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector, auditService: AuditService)
    extends FrontendController(mcc) with AutoCompleteFieldBinding with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val form = (request.cacheModel.locations.destinationCountry match {
      case Some(destinationCountry) => Countries.form(DestinationCountryPage).fill(destinationCountry)
      case _                        => Countries.form(DestinationCountryPage)
    }).withSubmissionErrors

    Ok(destinationCountryPage(form))
  }

  val submit: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    Countries
      .form(DestinationCountryPage)
      .bindFromRequest(formValuesFromRequest(Countries.fieldId))
      .fold(
        formWithErrors => Future.successful(BadRequest(destinationCountryPage(formWithErrors))),
        validCountry =>
          updateDeclarationFromRequest(_.updateDestinationCountry(validCountry)).map(declaration =>
            navigator.continueTo(redirectToNextPage(declaration))
          )
      )
  }

  private def redirectToNextPage(declaration: ExportsDeclaration): Call =
    if (taggedAuthCodes.skipLocationOfGoods(declaration)) OfficeOfExitController.displayPage
    else
      declaration.`type` match {
        case SUPPLEMENTARY | CLEARANCE          => LocationOfGoodsController.displayPage
        case STANDARD | SIMPLIFIED | OCCASIONAL => RoutingCountriesController.displayRoutingQuestion
      }
}
