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
import forms.declaration.FiscalInformation
import forms.declaration.FiscalInformation.{form, formId}
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.fiscal_information

import scala.concurrent.{ExecutionContext, Future}

class FiscalInformationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  mcc: MessagesControllerComponents
)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[FiscalInformation](cacheId, formId).map {
      case Some(data) => Ok(fiscal_information(form.fill(data)))
      case _ =>          Ok(fiscal_information(form))
    }
  }

  def saveFiscalInformation(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[FiscalInformation]) =>
          Future.successful(BadRequest(fiscal_information(formWithErrors))),
        validFiscalInformation =>
          customsCacheService
            .cache[FiscalInformation](cacheId, formId, validFiscalInformation)
            .map(_ => specifyNextPage(validFiscalInformation))
      )
  }

  private def specifyNextPage(answer: FiscalInformation): Result =
    if(answer.onwardSupplyRelief == FiscalInformation.AllowedFiscalInformationAnswers.yes)
      Redirect(routes.AdditionalFiscalReferencesController.displayPage())
    else Redirect(routes.ItemsSummaryController.displayForm())

}
