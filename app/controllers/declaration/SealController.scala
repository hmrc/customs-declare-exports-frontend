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

import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import controllers.helpers.MultipleItemsHelper.saveAndContinue
import controllers.helpers.{FormAction, Remove}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.Seal
import handlers.ErrorHandler
import models.Mode
import models.declaration.Container
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.{seal_add, seal_remove, seal_summary}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SealController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  addPage: seal_add,
  removePage: seal_remove,
  summaryPage: seal_summary
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayAddSeal(mode: Mode, containerId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(addPage(mode, Seal.form().withSubmissionErrors(), containerId))
  }

  def submitAddSeal(mode: Mode, containerId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    Seal
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Seal]) => Future.successful(BadRequest(addPage(mode, formWithErrors, containerId))),
        validSeal =>
          request.cacheModel.containerBy(containerId) match {
            case Some(container) =>
              saveSeal(mode, Seal.form.fill(validSeal), container)
            case _ => errorHandler.displayErrorPage()
        }
      )
  }

  def displaySealSummary(mode: Mode, containerId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(summaryPage(mode, addSealYesNoForm(containerId).withSubmissionErrors(), containerId, seals(containerId)))
  }

  def submitSummaryAction(mode: Mode, containerId: String): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      FormAction.bindFromRequest() match {
        case Remove(values) => confirmRemoveSeal(containerId, sealId(values), mode)
        case _              => addSealAnswer(mode, containerId)
      }
    }

  def displaySealRemove(mode: Mode, containerId: String, sealId: String): Action[AnyContent] =
    (authenticate andThen journeyType) { implicit request =>
      Ok(removePage(mode, removeSealYesNoForm, containerId, sealId))
    }

  def submitSealRemove(mode: Mode, containerId: String, sealId: String): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      removeSealAnswer(mode, containerId, sealId)
    }

  private def addSealYesNoForm(containerId: String)(implicit request: JourneyRequest[AnyContent]): Form[YesNoAnswer] =
    YesNoAnswer.form(errorKey = if (seals(containerId).isEmpty) "declaration.seal.add.answer.empty" else "declaration.seal.another.answer.empty")

  private def removeSealYesNoForm = YesNoAnswer.form(errorKey = "declaration.seal.remove.answer.empty")

  private def sealId(values: Seq[String]): String = values.headOption.getOrElse("")

  private def seals(containerId: String)(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.containerBy(containerId).map(_.seals).getOrElse(Seq.empty)

  private def addSealAnswer(mode: Mode, containerId: String)(implicit request: JourneyRequest[AnyContent]) =
    addSealYesNoForm(containerId)
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) => Future.successful(BadRequest(summaryPage(mode, formWithErrors, containerId, seals(containerId)))),
        formData =>
          formData.answer match {
            case YesNoAnswers.yes =>
              Future.successful(navigator.continueTo(mode, routes.SealController.displayAddSeal(_, containerId)))
            case YesNoAnswers.no =>
              Future
                .successful(navigator.continueTo(mode, routes.TransportContainerController.displayContainerSummary))
        }
      )

  private def removeSealAnswer(mode: Mode, containerId: String, sealId: String)(implicit request: JourneyRequest[AnyContent]) =
    removeSealYesNoForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) => Future.successful(BadRequest(removePage(mode, formWithErrors, containerId, sealId))),
        formData =>
          formData.answer match {
            case YesNoAnswers.yes =>
              removeSeal(containerId, sealId, mode)
            case YesNoAnswers.no =>
              Future
                .successful(navigator.continueTo(mode, routes.SealController.displaySealSummary(_, containerId)))
        }
      )

  private def confirmRemoveSeal(containerId: String, sealId: String, mode: Mode)(implicit request: JourneyRequest[AnyContent]) =
    Future.successful(navigator.continueTo(mode, routes.SealController.displaySealRemove(_, containerId, sealId)))

  private def removeSeal(containerId: String, sealId: String, mode: Mode)(implicit request: JourneyRequest[AnyContent]) = {
    val result =
      request.cacheModel.containerBy(containerId).map(c => c.copy(seals = c.seals.filterNot(_.id == sealId))) match {
        case Some(container) => updateCache(container)
        case _               => Future.successful(None)
      }
    result.map(_ => navigator.continueTo(mode, routes.SealController.displaySealSummary(_, containerId)))
  }

  private def saveSeal(mode: Mode, boundForm: Form[Seal], cachedContainer: Container)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    saveAndContinue(boundForm, cachedContainer.seals, isMandatory = true, Seal.sealsAllowed, "id", "declaration.seal").fold(
      formWithErrors => Future.successful(BadRequest(addPage(mode, formWithErrors, cachedContainer.id))),
      updatedCache =>
        if (updatedCache != cachedContainer.seals) updateCache(cachedContainer.copy(seals = updatedCache)).map { _ =>
          navigator.continueTo(mode, routes.SealController.displaySealSummary(_, cachedContainer.id))
        } else
          Future.successful(navigator.continueTo(mode, routes.SealController.displaySealSummary(_, cachedContainer.id)))
    )

  private def updateCache(updatedContainer: Container)(implicit req: JourneyRequest[AnyContent]) =
    updateExportsDeclarationSyncDirect(model => model.addOrUpdateContainer(updatedContainer))
}
