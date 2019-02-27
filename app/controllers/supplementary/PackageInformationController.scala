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
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.supplementary.PackageInformation
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import forms.supplementary.{PackageInformation, PackageInformationData, Packages}
import forms.supplementary.PackageInformation._
import handlers.ErrorHandler
import models.requests.AuthenticatedRequest
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.http.HeaderCarrier
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

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[PackageInformationData](supplementaryCacheId, formId).map {
      case Some(data) => Ok(package_information(appConfig, form, data.packages))
      case _          => Ok(package_information(appConfig, form, Seq()))
    }
  }

  def submitPackageInformation(): Action[AnyContent] = authenticate.async { implicit request =>
    val boundForm = form.bindFromRequest()

    val actionTypeOpt = request.body.asFormUrlEncoded.flatMap(FormAction.fromUrlEncoded(_))

    val cachedData =
      customsCacheService
        .fetchAndGetEntry[PackageInformationData](supplementaryCacheId, formId)
        .map(_.getOrElse(PackageInformationData(Seq(), None, None, None, None)))

    cachedData.flatMap { cache =>
      boundForm
        .fold(
          (formWithErrors: Form[PackageInformation]) =>
            Future.successful(BadRequest(package_information(appConfig, formWithErrors, cache.packages))),
          validForm => {
            actionTypeOpt match {
              case Some(Add)                             => addAnotherPackageAndTypeHandler(validForm, cache)
              case Some(SaveAndContinue)              => saveAndContinueHandler(validForm, cache)
              case Some(Remove(values)) =>
                val retrievedData = retrieveInformation(values)

                removePackageTypeHandler(
                  validForm.copy(
                    typesOfPackages = retrievedData.typesOfPackages,
                    numberOfPackages = retrievedData.numberOfPackages
                  ),
                  cache
                )
              case _                                 => errorHandler.displayErrorPage()
            }
          }
        )
    }
  }

  private def removePackageTypeHandler(code: PackageInformation,
                                 cachedData: PackageInformationData
                               )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    if (cachedData.containsTypesOfPackage(code) || cachedData.containsNumberOfPackage(code)) {
      val updatedCache =
        cachedData.copy(packages = cachedData.packages.filterNot(_ == code))

      customsCacheService.cache[PackageInformationData](supplementaryCacheId, formId, updatedCache).map { _ =>
        Redirect(controllers.supplementary.routes.PackageInformationController.displayForm())
      }
    } else displayErrorPage()

  private def addAnotherPackageAndTypeHandler(
    userInput: PackageInformation,
    cachedData: PackageInformationData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cachedData.packages) match {
      case (_, packages) if packages.length >= 99 => //TODO Extract 99
        handleErrorPage(
          Seq(("", "supplementary.declarationHolders.maximumAmount.error")),
          userInput,
          cachedData.packages
        )
      case(information, packages) if packages.contains(information) =>
        handleErrorPage(Seq(("", "supplementary.declarationHolders.duplicated")), userInput, cachedData.packages)

      case (information, packages) if information.typesOfPackages.isDefined && information.numberOfPackages.isDefined =>
        val updatedCache = PackageInformationData(packages :+ information.toPackages(information), None, None, None, None )

        customsCacheService.cache[PackageInformationData](supplementaryCacheId, formId, updatedCache).map { _ =>
          Redirect(controllers.supplementary.routes.PackageInformationController.displayForm())
        }
    }

  private def saveAndContinueHandler(
    userInput: PackageInformation,
    cachedData: PackageInformationData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cachedData.packages) match {
      case (information, Seq()) =>
        information match {
          case PackageInformation(Some(typesOfPackages), Some(numberOfPackages),
          Some(supplementaryUnits), Some(shippingMarks), Some(netMass), Some(grossMass)) =>
            val updatedCache = PackageInformationData(Seq(Packages(Some(typesOfPackages), Some(numberOfPackages))),
              Some(supplementaryUnits), Some(shippingMarks), Some(netMass), Some(grossMass))

            customsCacheService.cache[PackageInformationData](supplementaryCacheId, formId, updatedCache).map { _ =>
              Redirect(controllers.supplementary.routes.AdditionalInformationController.displayForm())
            }

          case PackageInformation(maybeTypesOfPackages, maybeNumberOfPackages, maybeSupplementaryUnits,
          maybeShippingMarks, maybeNetMass, maybeGrossMass) =>
            val typesOfPackageError = maybeTypesOfPackages.fold(
              Seq(("typesOfPackage", "supplementary.declarationHolder.authorisationCode.empty"))
            )(_ => Seq[(String, String)]())

            val numberOfPackagesError = maybeNumberOfPackages.fold(Seq(("numberOfPackages", "supplementary.eori.empty"))
            )(_ => Seq[(String, String)]())

            val supplementaryUnitsError = maybeSupplementaryUnits.fold(Seq(("numberOfPackages", "supplementary.eori.empty"))
            )(_ => Seq[(String, String)]())

            val shippingMarksError = maybeShippingMarks.fold(Seq(("numberOfPackages", "supplementary.eori.empty"))
            )(_ => Seq[(String, String)]())

            val netMassError = maybeNetMass.fold(Seq(("numberOfPackages", "supplementary.eori.empty"))
            )(_ => Seq[(String, String)]())

            val grossMassError = maybeGrossMass.fold(Seq(("numberOfPackages", "supplementary.eori.empty"))
            )(_ => Seq[(String, String)]())


            handleErrorPage(typesOfPackageError ++ numberOfPackagesError ++ supplementaryUnitsError ++ shippingMarksError
              ++ netMassError ++ grossMassError, userInput, Seq())
        }

      case (information, informations) =>
        information match {
          case _ if informations.length >= 99 =>
            handleErrorPage(Seq(("", "supplementary.declarationHolders.maximumAmount.error")), userInput, informations)

          case _ if informations.contains(information) =>
            handleErrorPage(Seq(("", "supplementary.declarationHolders.duplicated")), userInput, informations)

          case _ if information.typesOfPackages.isDefined == information.numberOfPackages.isDefined &&
            information.supplementaryUnits.isDefined && information.shippingMarks.isDefined && information.netMass.isDefined &&
            information.grossMass.isDefined=>
            val updatedInformations = if(information.typesOfPackages.isDefined && information.numberOfPackages.isDefined )
              informations :+ information.toPackages(information) else informations
            val updatedCache = PackageInformationData(updatedInformations, None, None, None, None)

            customsCacheService.cache[PackageInformationData](supplementaryCacheId, formId, updatedCache).map { _ =>
              Redirect(controllers.supplementary.routes.AdditionalInformationController.displayForm())
            }

          case PackageInformation(maybeTypesOfPackages, maybeNumberOfPackages, maybeSupplementaryUnits,
          maybeShippingMarks, maybeNetMass, maybeGrossMass) =>
            val typesOfPackageError = maybeTypesOfPackages.fold(
              Seq(("typesOfPackage", "supplementary.declarationHolder.authorisationCode.empty"))
            )(_ => Seq[(String, String)]())

            val numberOfPackagesError = maybeNumberOfPackages.fold(Seq(("numberOfPackages", "supplementary.eori.empty"))
            )(_ => Seq[(String, String)]())

            val supplementaryUnitsError = maybeSupplementaryUnits.fold(Seq(("numberOfPackages", "supplementary.eori.empty"))
            )(_ => Seq[(String, String)]())

            val shippingMarksError = maybeShippingMarks.fold(Seq(("numberOfPackages", "supplementary.eori.empty"))
            )(_ => Seq[(String, String)]())

            val netMassError = maybeNetMass.fold(Seq(("numberOfPackages", "supplementary.eori.empty"))
            )(_ => Seq[(String, String)]())

            val grossMassError = maybeGrossMass.fold(Seq(("numberOfPackages", "supplementary.eori.empty"))
            )(_ => Seq[(String, String)]())


            handleErrorPage(typesOfPackageError ++ numberOfPackagesError ++ supplementaryUnitsError ++ shippingMarksError
              ++ netMassError ++ grossMassError, userInput, Seq())
        }
    }

  private def retreivePackageType(action: String) : String = action.dropWhile(_ != ':').drop(1)

  private def displayErrorPage()(implicit request: Request[_]): Future[Result] =
    Future.successful(
      BadRequest(
        errorHandler.standardErrorTemplate(
          pageTitle = messagesApi("global.error.title"),
          heading = messagesApi("global.error.heading"),
          message = messagesApi("global.error.message")
        )
      )
    )

  private def handleErrorPage(
    fieldWithError: Seq[(String, String)],
    userInput: PackageInformation,
    information: Seq[Packages]
  )(implicit request: Request[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form.fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(package_information(appConfig, formWithError, information)))
  }

  private def retrieveInformation(values: Seq[String]): Packages =
    Packages.buildFromString(values.headOption.getOrElse("").dropWhile(_ != ':').drop(1))
}
