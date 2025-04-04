/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.helpers.SequenceIdHelper.handleSequencing
import controllers.helpers.{FormAction, Remove}
import controllers.navigation.Navigator
import controllers.section6.routes.{ContainerController, SealController}
import controllers.summary.routes.SectionSummaryController
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section6.ContainerAdd.form
import forms.section6.{ContainerAdd, ContainerFirst}
import models.DeclarationMeta.sequenceIdPlaceholder
import models.ExportsDeclaration
import models.declaration.Container
import models.declaration.Container.maxNumberOfItems
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section6.{container_add, container_add_first, container_remove, container_summary}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ContainerController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  addFirstPage: container_add_first,
  addPage: container_add,
  summaryPage: container_summary,
  removePage: container_remove
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayAddContainer(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (request.cacheModel.hasContainers) Ok(addPage(form.withSubmissionErrors))
    else Ok(addFirstPage(ContainerFirst.form.withSubmissionErrors))
  }

  def submitAddContainer(): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      if (request.cacheModel.hasContainers) {
        val boundForm = form.bindFromRequest()
        saveAdditionalContainer(boundForm, maxNumberOfItems, request.cacheModel.containers)
      } else
        ContainerFirst.form
          .bindFromRequest()
          .fold(formWithErrors => Future.successful(BadRequest(addFirstPage(formWithErrors))), containerId => saveFirstContainer(containerId.id))
    }

  def displayContainerSummary(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.containers match {
      case containers if containers.nonEmpty => Ok(summaryPage(addAnotherContainerYesNoForm.withSubmissionErrors, containers))

      case _ => navigator.continueTo(ContainerController.displayAddContainer)
    }
  }

  def submitSummaryAction(): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      FormAction.bindFromRequest() match {
        case Remove(values) =>
          val result = navigator.continueTo(ContainerController.displayContainerRemove(containerId(values)))
          Future.successful(result)

        case _ => addContainerAnswer()
      }
    }

  def displayContainerRemove(containerId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.containerBy(containerId) match {
      case Some(container) => Ok(removePage(removeContainerYesNoForm, container))
      case _               => navigator.continueTo(ContainerController.displayContainerSummary)
    }
  }

  def submitContainerRemove(containerId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    removeContainerAnswer(containerId)
  }

  private def saveFirstContainer(containerId: Option[String])(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    containerId match {
      case Some(id) => updateCache(Seq(Container(id = id, seals = Seq.empty))).map(_ => redirectAfterAdd(id))
      case None =>
        updateCache(Seq.empty).flatMap(dec => updateDeclaration(dec.updateReadyForSubmission(true), request.eori)) map { _ =>
          navigator.continueTo(SectionSummaryController.displayPage(6))
        }
    }

  private def saveAdditionalContainer(boundForm: Form[ContainerAdd], elementLimit: Int, containersInCache: Seq[Container])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    prepare(boundForm, elementLimit, containersInCache).fold(
      formWithErrors => Future.successful(BadRequest(addPage(formWithErrors))),
      containers =>
        if (containers != containersInCache) updateCache(containers).map(_ => redirectAfterAdd(containers.last.id))
        else Future.successful(navigator.continueTo(ContainerController.displayContainerSummary))
    )

  private def prepare(
    boundForm: Form[ContainerAdd],
    elementLimit: Int,
    containersInCache: Seq[Container]
  ): Either[Form[ContainerAdd], Seq[Container]] = {
    val newContainer = boundForm.value.flatMap(_.id).map(Container(sequenceIdPlaceholder, _, Seq.empty))
    newContainer match {
      case Some(container) =>
        duplication(container.id, containersInCache) ++ limitOfElems(elementLimit, containersInCache) match {
          case Seq()  => Right(containersInCache :+ container)
          case errors => Left(boundForm.copy(errors = errors))
        }
      case _ => Left(boundForm)
    }
  }

  private def addAnotherContainerYesNoForm: Form[YesNoAnswer] =
    YesNoAnswer.form(errorKey = "declaration.transportInformation.container.another.empty")

  private def removeContainerYesNoForm: Form[YesNoAnswer] =
    YesNoAnswer.form(errorKey = "declaration.transportInformation.container.remove.empty")

  private def duplication(id: String, containersInCache: Seq[Container]): Seq[FormError] =
    if (containersInCache.exists(_.id == id)) Seq(FormError("id", "declaration.transportInformation.error.duplicate")) else Seq.empty

  private def limitOfElems[A](limit: Int, cachedData: Seq[Container]): Seq[FormError] =
    if (cachedData.length >= limit) Seq(FormError("", "supplementary.limit")) else Seq.empty

  private def addContainerAnswer()(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    addAnotherContainerYesNoForm
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(summaryPage(formWithErrors, request.cacheModel.containers))),
        _.answer match {
          case YesNoAnswers.yes =>
            Future.successful(navigator.continueTo(ContainerController.displayAddContainer))

          case YesNoAnswers.no =>
            updateDeclarationFromRequest(_.updateReadyForSubmission(true)) map { _ =>
              navigator.continueTo(SectionSummaryController.displayPage(6))
            }
        }
      )

  private def removeContainerAnswer(containerId: String)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    removeContainerYesNoForm
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(removePage(formWithErrors, request.cacheModel.containers.filter(_.id == containerId).head))),
        _.answer match {
          case YesNoAnswers.yes => removeContainer(containerId)
          case YesNoAnswers.no  => Future.successful(navigator.continueTo(ContainerController.displayContainerSummary))
        }
      )

  private def removeContainer(containerId: String)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateCache(request.cacheModel.containers.filterNot(_.id == containerId))
      .map(_ => navigator.continueTo(ContainerController.displayContainerSummary))

  private def containerId(values: Seq[String]): String = values.headOption.getOrElse("")

  private def updateCache(containers: Seq[Container])(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] = {
    val declarationMeta = request.cacheModel.declarationMeta
    val (updatedContainers, updatedMeta) = handleSequencing(containers, declarationMeta)

    updateDeclarationFromRequest(
      _.updateContainers(updatedContainers)
        .copy(declarationMeta = updatedMeta)
    )
  }

  private def redirectAfterAdd(containerId: String)(implicit request: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(SealController.displaySealSummary(containerId))
}
