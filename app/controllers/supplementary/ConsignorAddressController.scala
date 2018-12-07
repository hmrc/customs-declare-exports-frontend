/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.supplementary

import config.AppConfig
import controllers.actions.AuthAction
import forms.supplementary.Address
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.consignor_address

import scala.concurrent.Future

class ConsignorAddressController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsCacheService: CustomsCacheService
) extends FrontendController with I18nSupport {

  val formId = "ConsignorAddress"
  val form = Form(Address.addressMapping)

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[Map[String, String]](appConfig.appName, formId).map {
      case Some(data) => Ok(consignor_address(appConfig, form.fill(Address.fromConsignorMetadataProperties(data))))
      case _          => Ok(consignor_address(appConfig, form))
    }
  }

  def saveAddress(): Action[AnyContent] = authenticate.async { implicit request =>
    form.bindFromRequest().fold(
      (formWithErrors: Form[Address]) =>
        Future.successful(BadRequest(consignor_address(appConfig, formWithErrors))),
      form =>
        customsCacheService.cache[Map[String, String]](appConfig.appName, formId, Address.toConsignorMetadataProperties(form)).map { _ =>
          Redirect(controllers.supplementary.routes.DeclarantAddressController.displayForm())
        }
    )
  }
}
