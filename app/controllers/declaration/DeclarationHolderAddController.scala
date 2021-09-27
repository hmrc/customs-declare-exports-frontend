/*
 * Copyright 2021 HM Revenue & Customs
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

import scala.concurrent.{ExecutionContext, Future}

import controllers.actions.{AuthAction, JourneyAction}
import controllers.helpers.DeclarationHolderHelper.{cachedHolders, validateAuthCode, DeclarationHolderFormGroupId}
import controllers.helpers.MultipleItemsHelper
import controllers.navigation.Navigator
import forms.declaration.declarationHolder.DeclarationHolder
import forms.declaration.declarationHolder.DeclarationHolder.form
import javax.inject.Inject
import models.declaration.DeclarationHoldersData
import models.declaration.DeclarationHoldersData.limitOfHolders
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_add

class DeclarationHolderAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarationHolderPage: declaration_holder_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(declarationHolderPage(mode, form(request.eori).withSubmissionErrors(), request.eori))
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form(request.eori).bindFromRequest()

    boundForm.fold(formWithErrors => {
      Future.successful(BadRequest(declarationHolderPage(mode, formWithErrors, request.eori)))
    }, _ => saveHolder(mode, boundForm))
  }

  private def saveHolder(mode: Mode, boundForm: Form[DeclarationHolder])(implicit r: JourneyRequest[AnyContent]): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedHolders, limitOfHolders, DeclarationHolderFormGroupId, "declaration.declarationHolder")
      .fold(
        formWithErrors => Future.successful(BadRequest(declarationHolderPage(mode, formWithErrors, r.eori))),
        holders => {
          validateAuthCode(boundForm.value) match {
            case Some(error) =>
              val formWithError = boundForm.copy(errors = Seq(error))
              Future.successful(BadRequest(declarationHolderPage(mode, formWithError, r.eori)))

            case _ =>
              updateExportsCache(holders)
                .map(_ => navigator.continueTo(mode, routes.DeclarationHolderSummaryController.displayPage))
          }
        }
      )

  private def validateMutuallyExclusiveAuthTypeCodes(boundForm: Form[DeclarationHolder], holders: Seq[DeclarationHolder]): Option[FormError] = {
    val mutuallyExclusiveAuthTypeCodes = Seq("CSE", "EXRR")

    boundForm.value match {
      case Some(DeclarationHolder(Some(code), _, _)) if (mutuallyExclusiveAuthTypeCodes.contains(code)) =>
        val mustNotAlreadyContainCodes = mutuallyExclusiveAuthTypeCodes.filter(_ != code)

        if (holders.map(_.authorisationTypeCode.getOrElse("")).containsSlice(mustNotAlreadyContainCodes))
          Some(FormError(DeclarationHolderFormGroupId, s"declaration.declarationHolder.${code}.error.exclusive"))
        else
          None
      case _ => None
    }
  }

  private def updateExportsCache(holders: Seq[DeclarationHolder])(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val isRequired = model.parties.declarationHoldersData.flatMap(_.isRequired)
      val updatedParties = model.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders, isRequired)))
      model.copy(parties = updatedParties)
    })
}
