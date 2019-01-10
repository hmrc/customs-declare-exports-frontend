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

package controllers.supplementary

import config.AppConfig
import controllers.actions.AuthAction
import forms.supplementary.ConsignmentReferences
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.AuthenticatedRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.consignment_references

import scala.concurrent.{ExecutionContext, Future}

class ConsignmentReferencesController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  private val supplementaryDeclarationCacheId = appConfig.appName

  def displayPage(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[ConsignmentReferences](supplementaryDeclarationCacheId, ConsignmentReferences.id)
      .map {
        case Some(data) => Ok(consignment_references(appConfig, ConsignmentReferences.form().fill(data)))
        case _ =>
          val initialData = buildConsignmentReferences()
          Ok(consignment_references(appConfig, ConsignmentReferences.form().fill(initialData)))
      }
  }

  def submitConsignmentReferences(): Action[AnyContent] = authenticate.async { implicit request =>
    ConsignmentReferences
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ConsignmentReferences]) =>
          Future.successful(BadRequest(consignment_references(appConfig, formWithErrors))),
        validConsignmentReferences => {
          val consignmentReferencesToSave = buildConsignmentReferences(
            userEnteredUcr = validConsignmentReferences.userEnteredUcr,
            lrn = validConsignmentReferences.lrn
          )
          customsCacheService
            .cache[ConsignmentReferences](
              supplementaryDeclarationCacheId,
              ConsignmentReferences.id,
              consignmentReferencesToSave
            )
            .map { _ =>
              Ok("You should be now redirected to \"Exporter ID\" page")
            }
        }
      )
  }

  private def buildConsignmentReferences(userEnteredUcr: String = "", lrn: String = "")(
    implicit request: AuthenticatedRequest[_]
  ) = ConsignmentReferences(prepopulatedPart = request.user.eori + "-", userEnteredUcr = userEnteredUcr, lrn = lrn)

}
