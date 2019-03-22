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
import controllers.util.CacheIdGenerator._
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.supplementary.PackageInformation
import forms.supplementary.PackageInformation._
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.AuthenticatedRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.package_information

import scala.concurrent.{ExecutionContext, Future}

class PackageInformationController @Inject()(
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  cacheService: CustomsCacheService
)(implicit ec: ExecutionContext, appConfig: AppConfig, override val messagesApi: MessagesApi)
    extends FrontendController with I18nSupport {

  val packagesMaxElements = 99

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    cacheService
      .fetchAndGetEntry[Seq[PackageInformation]](goodsItemCacheId, formId)
      .map(items => Ok(package_information(form, items.getOrElse(Seq.empty))))
  }

  def submitForm(): Action[AnyContent] = authenticate.async { implicit authRequest =>
    val actionTypeOpt = authRequest.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))
    cacheService
      .fetchAndGetEntry[Seq[PackageInformation]](goodsItemCacheId, formId)
      .flatMap { data =>
        implicit val packagings = data.getOrElse(Seq.empty)

        actionTypeOpt match {
          case Some(Add)             => addItem()
          case Some(Remove(ids))     => remove(ids.headOption)
          case Some(SaveAndContinue) => continue()
          case _                     => errorHandler.displayErrorPage()
        }
      }
  }

  def remove(
    id: Option[String]
  )(implicit authRequest: AuthenticatedRequest[AnyContent], packages: Seq[PackageInformation]): Future[Result] =
    id match {
      case Some(id) => {
        val updatedPackages =
          packages.zipWithIndex.filterNot(_._2.toString == id).map(_._1)

        cacheService
          .cache[Seq[PackageInformation]](goodsItemCacheId(), formId, updatedPackages)
          .map(_ => Redirect(routes.PackageInformationController.displayForm()))
      }
      case _ => errorHandler.displayErrorPage()
    }

  def continue()(
    implicit request: AuthenticatedRequest[AnyContent],
    packages: Seq[PackageInformation]
  ): Future[Result] = {
    val payload = form.bindFromRequest()
    if (!isFormEmpty(payload)) badRequest(payload, USE_ADD)
    else if (packages.size == 0) badRequest(payload, ADD_ONE)
    else
      Future.successful(Redirect(controllers.supplementary.routes.CommodityMeasureController.displayForm()))

  }

  private def isFormEmpty[A](form: Form[A]): Boolean =
    retrieveData(form).filter { case (_, value) => value.nonEmpty }.isEmpty

  private def retrieveData[A](form: Form[A]): Map[String, String] =
    form.data.filter { case (name, _) => name != "csrfToken" }

  def addItem()(
    implicit authenticatedRequest: AuthenticatedRequest[AnyContent],
    packages: Seq[PackageInformation]
  ): Future[Result] =
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[PackageInformation]) =>
          Future.successful(BadRequest(package_information(formWithErrors, packages))),
        validForm => {
          isAdditionValid[PackageInformation](validForm).fold(
            cacheService
              .cache[Seq[PackageInformation]](goodsItemCacheId(), formId, (packages :+ validForm))
              .map(_ => Redirect(routes.PackageInformationController.displayForm()))
          )(badRequest(form.fill(validForm), _))
        }
      )

  private def isAdditionValid[A](item: A)(implicit cachedItems: Seq[A]): Option[String] =
    if (cachedItems.contains(item)) Some(DUPLICATE_MSG_KEY)
    else if (cachedItems.size >= packagesMaxElements) Some(LIMIT_MSG_KEY)
    else None

  private def badRequest(
    form: Form[_],
    error: String
  )(implicit authenticatedRequest: AuthenticatedRequest[AnyContent], packages: Seq[PackageInformation]) =
    Future.successful(BadRequest(package_information(form.withGlobalError(error), packages)))
}
