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
import controllers.util.{FormAction, SaveAndContinue, SaveAndReturn}
import forms.Choice.AllowedChoiceValues.{StandardDec, SupplementaryDec}
import forms.declaration.additionaldeclarationtype._
import javax.inject.Inject
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.additionaldeclarationtype.declaration_type

import scala.concurrent.{ExecutionContext, Future}

class AdditionalDeclarationTypeController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarationTypePage: declaration_type
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val decType = extractFormType(request)
    request.cacheModel.additionalDeclarationType match {
      case Some(data) => Ok(declarationTypePage(decType.form().fill(data)))
      case _          => Ok(declarationTypePage(decType.form()))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val decType = extractFormType(request).form().bindFromRequest()

    decType
      .fold(
        formWithErrors => Future.successful(BadRequest(declarationTypePage(formWithErrors))),
        validAdditionalDeclarationType =>
          updateCache(validAdditionalDeclarationType).map { _ =>
            navigator.continueTo(controllers.declaration.routes.ConsignmentReferencesController.displayPage())
        }
      )
  }

  private def extractFormType(journeyRequest: JourneyRequest[_]): AdditionalDeclarationTypeTrait =
    journeyRequest.choice.value match {
      case SupplementaryDec => AdditionalDeclarationTypeSupplementaryDec
      case StandardDec      => AdditionalDeclarationTypeStandardDec
    }

  private def updateCache(
    formData: AdditionalDeclarationType
  )(implicit request: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      model.copy(additionalDeclarationType = Some(formData))
    })

}
