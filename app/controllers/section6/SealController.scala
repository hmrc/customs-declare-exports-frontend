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

package controllers.section6

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.{ModelCacheable, SubmissionErrors}
import controllers.helpers.MultipleItemsHelper.saveAndContinue
import controllers.helpers.SequenceIdHelper.handleSequencing
import controllers.helpers.{FormAction, Remove}
import controllers.navigation.Navigator
import controllers.section6.routes.{ContainerController, SealController}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section6.Seal
import handlers.ErrorHandler
import models.ExportsDeclaration
import models.declaration.Container
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section6.{seal_add, seal_remove, seal_summary}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SealController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  addPage: seal_add,
  removePage: seal_remove,
  summaryPage: seal_summary
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayAddSeal(containerId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(addPage(Seal.form.withSubmissionErrors, containerId))
  }

  def submitAddSeal(containerId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    Seal.form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(addPage(formWithErrors, containerId))),
        validSeal =>
          request.cacheModel.containerBy(containerId) match {
            case Some(container) => saveSeal(Seal.form.fill(validSeal), container)
            case _               => errorHandler.redirectToErrorPage
          }
      )
  }

  def displaySealSummary(containerId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(summaryPage(addSealYesNoForm(containerId).withSubmissionErrors, containerId, seals(containerId)))
  }

  def submitSummaryAction(containerId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    FormAction.bindFromRequest() match {
      case Remove(values) => confirmRemoveSeal(containerId, sealId(values))
      case _              => addSealAnswer(containerId)
    }
  }

  def displaySealRemove(containerId: String, sealId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    checkIfSealExists(containerId, sealId) {
      Future.successful(Ok(removePage(removeSealYesNoForm, containerId, sealId)))
    }
  }

  def submitSealRemove(containerId: String, sealId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    checkIfSealExists(containerId, sealId) {
      removeSealAnswer(containerId, sealId)
    }
  }

  private def checkIfSealExists(containerId: String, sealId: String)(action: => Future[Result])(implicit request: JourneyRequest[_]): Future[Result] =
    request.cacheModel
      .containerBy(containerId)
      .flatMap(_.seals.find(_.id == sealId))
      .fold(Future.successful(Redirect(SealController.displaySealSummary(containerId))))(_ => action)

  private def addSealYesNoForm(containerId: String)(implicit request: JourneyRequest[AnyContent]): Form[YesNoAnswer] = {
    val errorKey = if (seals(containerId).isEmpty) "declaration.seal.add.answer.empty" else "declaration.seal.another.answer.empty"
    YesNoAnswer.form(errorKey = errorKey)
  }

  private def removeSealYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.seal.remove.answer.empty")

  private def sealId(values: Seq[String]): String = values.headOption.getOrElse("")

  private def seals(containerId: String)(implicit request: JourneyRequest[AnyContent]): Seq[Seal] =
    request.cacheModel.containerBy(containerId).map(container => container.seals).getOrElse(Seq.empty)

  private def addSealAnswer(containerId: String)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    addSealYesNoForm(containerId)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(summaryPage(formWithErrors, containerId, seals(containerId)))),
        _.answer match {
          case YesNoAnswers.yes => Future.successful(navigator.continueTo(SealController.displayAddSeal(containerId)))
          case YesNoAnswers.no  => Future.successful(navigator.continueTo(ContainerController.displayContainerSummary))
        }
      )

  private def removeSealAnswer(containerId: String, sealId: String)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    removeSealYesNoForm
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(removePage(formWithErrors, containerId, sealId))),
        _.answer match {
          case YesNoAnswers.yes => removeSeal(containerId, sealId)
          case YesNoAnswers.no  => Future.successful(navigator.continueTo(SealController.displaySealSummary(containerId)))
        }
      )

  private def confirmRemoveSeal(containerId: String, sealId: String)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    Future.successful(navigator.continueTo(SealController.displaySealRemove(containerId, sealId)))

  private def removeSeal(containerId: String, sealId: String)(implicit request: JourneyRequest[AnyContent]): Future[Result] = {
    val result = request.cacheModel
      .containerBy(containerId)
      .map(c => c.copy(seals = c.seals.filterNot(_.id == sealId))) match {
      case Some(container) => updateCache(container, container.seals)
      case _               => Future.successful(None)
    }

    result.map(_ => navigator.continueTo(SealController.displaySealSummary(containerId)))
  }

  private def saveSeal(boundForm: Form[Seal], container: Container)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    saveAndContinue(boundForm, container.seals, isMandatory = true, Seal.sealsAllowed, "id", "declaration.seal")
      .fold(
        formWithErrors => Future.successful(BadRequest(addPage(formWithErrors, container.id))),
        seals =>
          if (seals == container.seals)
            Future.successful(navigator.continueTo(SealController.displaySealSummary(container.id)))
          else
            updateCache(container, seals).map { _ =>
              navigator.continueTo(SealController.displaySealSummary(container.id))
            }
      )

  private def updateCache(container: Container, seals: Seq[Seal])(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] = {
    val declarationMeta = request.cacheModel.declarationMeta
    val (updatedSeals, updatedMeta) = handleSequencing(seals, declarationMeta)

    updateDeclarationFromRequest(
      _.addOrUpdateContainer(container.copy(seals = updatedSeals))
        .copy(declarationMeta = updatedMeta)
    )
  }
}
