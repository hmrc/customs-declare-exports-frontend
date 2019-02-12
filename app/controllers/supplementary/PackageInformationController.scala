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
import controllers.util.CacheIdGenerator.supplementaryCacheId
import forms.supplementary.PackageInformation
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import forms.supplementary.PackageInformation
import handlers.ErrorHandler
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Request, Result}
import views.html.supplementary.package_information

import scala.concurrent.{ExecutionContext, Future}

class PackageInformationController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {
  import forms.supplementary.PackageInformation._

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[PackageInformation](supplementaryCacheId, formId).map {
      case Some(data) => Ok(package_information(appConfig, form.fill(data)))
      case _          => Ok(package_information(appConfig, form))
    }
  }

  def submit(): Action[AnyContent] = authenticate.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[PackageInformation]) =>
          Future.successful(BadRequest(package_information(appConfig, formWithErrors))),
        form =>
          customsCacheService.cache[PackageInformation](supplementaryCacheId, formId, form).map { _ =>
            Redirect(controllers.supplementary.routes.AdditionalInformationController.displayForm())
        }
      )
  }
//
//  def submitProcedureCodes(): Action[AnyContent] = authenticate.async { implicit request =>
//    val boundForm = form.bindFromRequest()
//
//    val actionType =
//      request.body.asFormUrlEncoded.flatMap(_.get("action")).flatMap(_.headOption).getOrElse("Wrong action")
//
//    val cachedData =
//      customsCacheService
//        .fetchAndGetEntry[PackageInformation](cacheId, formId)
//        .map(_.getOrElse(PackageInoformationData(None, Seq())))
//
//    cachedData.flatMap { cache =>
//      boundForm
//        .fold(
//          (formWithErrors: Form[PackageInformation]) =>
//            Future.successful(BadRequest(package_information(appConfig, formWithErrors, cache.additionalProcedureCodes))),
//          validForm => {
//            actionType match {
//              case "Add"                             => addAnotherPackageAndTypeHandler(validForm, cache)
//              case "Save and continue"               => saveAndContinueHandler(validForm, cache)
//              case value if value.contains("Remove") => removeTypeHandler(retrieveProcedureCode(value), cache)
//              case _                                 => displayErrorPage()
//            }
//          }
//        )
//    }
//  }
//
//  private def addAnotherPackageAndTypeHandler(typesOfPackages: PackageInformation, numberOfPackages: PackageInformation)
//
//  private def displayErrorPage()(implicit request: Request[_]): Future[Result] =
//    Future.successful(
//      BadRequest(
//        errorHandler.standardErrorTemplate(
//          pageTitle = messagesApi("global.error.title"),
//          heading = messagesApi("global.error.heading"),
//          message = messagesApi("global.error.message")
//        )
//      )
//    )
//  private def removeTypeHandler()

}
