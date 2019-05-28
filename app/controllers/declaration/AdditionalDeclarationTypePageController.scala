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

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.cacheId
import forms.Choice.AllowedChoiceValues.{StandardDec, SupplementaryDec}
import forms.declaration.additionaldeclarationtype.{AdditionalDeclarationType, AdditionalDeclarationTypeStandardDec, AdditionalDeclarationTypeSupplementaryDec, AdditionalDeclarationTypeTrait}
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.additionaldeclarationtype.declaration_type

import scala.concurrent.{ExecutionContext, Future}

class AdditionalDeclarationTypePageController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  mcc: MessagesControllerComponents
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val decType = extractFormType(request)
    customsCacheService.fetchAndGetEntry[AdditionalDeclarationType](cacheId, decType.formId).map {
      case Some(data) => Ok(declaration_type(decType.form.fill(data)))
      case _          => Ok(declaration_type(decType.form))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val decType = extractFormType(request)
    decType
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[AdditionalDeclarationType]) =>
          Future.successful(BadRequest(declaration_type(formWithErrors))),
        validAdditionalDeclarationType =>
          customsCacheService
            .cache[AdditionalDeclarationType](cacheId, decType.formId, validAdditionalDeclarationType)
            .map(_ => Redirect(controllers.declaration.routes.ConsignmentReferencesController.displayPage()))
      )
  }

  private def extractFormType(journeyRequest: JourneyRequest[_]): AdditionalDeclarationTypeTrait =
    journeyRequest.choice.value match {
      case SupplementaryDec => AdditionalDeclarationTypeSupplementaryDec
      case StandardDec      => AdditionalDeclarationTypeStandardDec
    }
}
