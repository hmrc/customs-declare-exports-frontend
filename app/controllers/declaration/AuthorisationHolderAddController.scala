/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.helpers.AuthorisationHolderHelper._
import controllers.helpers.MultipleItemsHelper
import controllers.navigation.Navigator
import forms.declaration.authorisationHolder.AuthorisationHolder
import forms.declaration.authorisationHolder.AuthorisationHolder.{authorisationHolderFormGroupId, AuthorisationTypeCodeId}
import models.ExportsDeclaration
import models.declaration.AuthorisationHolders
import models.declaration.AuthorisationHolders.limitOfHolders
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.TaggedAuthCodes
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.authorisationHolder.authorisation_holder_add

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisationHolderAddController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  taggedAuthCodes: TaggedAuthCodes,
  authorisationHolderPage: authorisation_holder_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(authorisationHolderPage(form.withSubmissionErrors, request.eori))
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form.bindFromRequest()

    boundForm.fold(formWithErrors => Future.successful(BadRequest(authorisationHolderPage(formWithErrors, request.eori))), _ => saveHolder(boundForm))
  }

  private def saveHolder(boundForm: Form[AuthorisationHolder])(implicit r: JourneyRequest[AnyContent]): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, authorisationHolders, limitOfHolders, authorisationHolderFormGroupId, "declaration.authorisationHolder")
      .fold(
        formWithErrors => Future.successful(BadRequest(authorisationHolderPage(formWithErrors, r.eori))),
        updatedHolders =>
          validateMutuallyExclusiveAuthCodes(boundForm.value, authorisationHolders) match {
            case Some(error) =>
              val formWithError = boundForm.copy(errors = Seq(error))
              Future.successful(BadRequest(authorisationHolderPage(formWithError, r.eori)))

            case _ =>
              updateExportsCache(updatedHolders)
                .map(_ => navigator.continueTo(routes.AuthorisationHolderSummaryController.displayPage))
          }
      )

  private def updateExportsCache(holders: Seq[AuthorisationHolder])(implicit r: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      val isRequired = model.parties.declarationHoldersData.flatMap(_.isRequired)
      val updatedParties = model.parties.copy(declarationHoldersData = Some(AuthorisationHolders(holders, isRequired)))
      model.copy(parties = updatedParties)
    }

  // Note that this validation takes places only when adding a new authorisation, not when changing one.
  def validateMutuallyExclusiveAuthCodes(maybeHolder: Option[AuthorisationHolder], holders: Seq[AuthorisationHolder]): Option[FormError] =
    maybeHolder match {
      case Some(AuthorisationHolder(Some(code), _, _)) if taggedAuthCodes.codesMutuallyExclusive.contains(code) =>
        val mustNotAlreadyContainCodes = taggedAuthCodes.codesMutuallyExclusive.filter(_ != code)

        if (!holders.map(_.authorisationTypeCode.getOrElse("")).containsSlice(mustNotAlreadyContainCodes)) None
        else Some(FormError(AuthorisationTypeCodeId, s"declaration.authorisationHolder.${code}.error.exclusive"))

      case _ => None
    }
}
