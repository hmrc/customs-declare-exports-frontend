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
import play.api.mvc.{Action, AnyContent}
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
      .fetchAndGetEntry[Seq[PackageInformation]](supplementaryCacheId, formId)
      .map(items => Ok(package_information(form, items.getOrElse(Seq.empty))))

  }

  //TODO Validate payload - DONE
  //TODO Check GovermentAgencyGoodsItem Exists - DONE
  //TODO verify what action user performed - SEMI DONE
  //TODO handle add ,remove,  -- DONE
  //TODO check duplication  -- DONE
  //TODO validate for maximum no of entries -DONE
  //TODO continue - check items entered
  def submitForm(): Action[AnyContent] = authenticate.async { implicit authRequest =>
    val actionTypeOpt = authRequest.body.asFormUrlEncoded.flatMap(FormAction.fromUrlEncoded(_))
    cacheService
      .fetchAndGetEntry[Seq[PackageInformation]](supplementaryCacheId, formId)
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

  def remove(id: Option[String])(implicit authRequest: AuthenticatedRequest[AnyContent],
    packagings: Seq[PackageInformation]) =
    id match {
      case Some(id) => {
        val updatedPackagings =
          packagings.zipWithIndex.filterNot(_._2.toString == id).map(_._1)

        cacheService
          .cache[Seq[PackageInformation]](supplementaryCacheId(), formId, updatedPackagings)
          .map(_ => Redirect(routes.PackageInformationController.displayForm()))
      }
      case _ => errorHandler.displayErrorPage()
    }

  def continue()(implicit request: AuthenticatedRequest[_], packagings: Seq[PackageInformation]) = {
    val payload = form.bindFromRequest()
    if (payload.data.filter(_._2.size > 0).size > 1)
      Future.successful(
        BadRequest(
          package_information(payload.discardingErrors.withGlobalError("Use add button to add packaging"), packagings)
        )
      )
    else if (packagings.size == 0)
      Future.successful(
        BadRequest(
          package_information(
            payload.discardingErrors.withGlobalError("Add one or more package information using Add button"),
            packagings
          )
        )
      )
    else Future.successful(Redirect(controllers.supplementary.routes.AdditionalInformationController.displayForm()))

  }
  def validaeAddPayload() = true
  def validateContinuePayload() = true

  def addItem()(implicit authenticatedRequest: AuthenticatedRequest[AnyContent], packagings: Seq[PackageInformation]) =
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[PackageInformation]) =>
          Future.successful(BadRequest(package_information(formWithErrors, packagings))),
        validForm => {
          if (packagings.contains(validForm))
            Future.successful(
              BadRequest(
                package_information(
                  form.fill(validForm).withGlobalError("Input provided is a duplicate of existing packaging"),
                  packagings
                )
              )
            )
          else if (packagings.size > packagesMaxElements)
            Future.successful(
              BadRequest(
                package_information(
                  form.fill(validForm).withGlobalError("maximum number of packagings added"),
                  packagings
                )
              )
            )
          else {
            val updatedPackagings = packagings :+ validForm
            cacheService
              .cache[Seq[PackageInformation]](supplementaryCacheId(), formId, updatedPackagings)
              .map(_ => Redirect(routes.PackageInformationController.displayForm()))
          }
        }
      )

}
