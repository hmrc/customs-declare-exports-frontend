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

import connectors.CodeListConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.AdditionalFiscalReferencesAddController.AdditionalFiscalReferencesFormGroupId
import controllers.navigation.Navigator
import controllers.helpers.MultipleItemsHelper
import forms.declaration.AdditionalFiscalReference.form
import forms.declaration.AdditionalFiscalReferencesData._
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.fiscalInformation.additional_fiscal_references_add

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalFiscalReferencesAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalFiscalReferencesPage: additional_fiscal_references_add
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form().withSubmissionErrors()
    Ok(additionalFiscalReferencesPage(mode, itemId, frm))
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()

    boundForm.fold(
      formWithErrors => Future.successful(BadRequest(additionalFiscalReferencesPage(mode, itemId, formWithErrors))),
      _ => saveAndContinue(mode, itemId, boundForm, cachedData(itemId))
    )
  }

  private def cachedData(itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalFiscalReferencesData).getOrElse(AdditionalFiscalReferencesData.default)

  private def saveAndContinue(mode: Mode, itemId: String, form: Form[AdditionalFiscalReference], cachedData: AdditionalFiscalReferencesData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(form, cachedData.references, limit, AdditionalFiscalReferencesFormGroupId, "declaration.additionalFiscalReferences")
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalFiscalReferencesPage(mode, itemId, formWithErrors))),
        updatedCache =>
          updateExportsCache(itemId, cachedData.copy(references = updatedCache))
            .map(_ => navigator.continueTo(mode, routes.AdditionalFiscalReferencesController.displayPage(_, itemId)))
      )

  private def updateExportsCache(itemId: String, updatedAdditionalFiscalReferencesData: AdditionalFiscalReferencesData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      model.updatedItem(itemId, item => item.copy(additionalFiscalReferencesData = Some(updatedAdditionalFiscalReferencesData)))
    }
}

object AdditionalFiscalReferencesAddController {
  val AdditionalFiscalReferencesFormGroupId: String = "additionalFiscalReferences"
}
