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

package controllers.declaration

import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import controllers.util.{FormAction, Remove}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.{form, YesNoAnswers}
import forms.declaration.{ContainerAdd, ContainerYesNo}
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.Container.maxNumberOfItems
import models.declaration.{Container, TransportData}
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.{transport_container_add, transport_container_add_first, transport_container_remove, transport_container_summary}

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class TransportContainerController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  addFirstPage: transport_container_add_first,
  addPage: transport_container_add,
  summaryPage: transport_container_summary,
  removePage: transport_container_remove
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayAddContainer(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (request.cacheModel.hasContainers) {
      Ok(addPage(mode, ContainerAdd.form()))
    } else {
      Ok(addFirstPage(mode, ContainerYesNo.form()))
    }
  }

  def submitAddContainer(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    if (request.cacheModel.hasContainers) {
      val boundForm = ContainerAdd.form().bindFromRequest()
      saveAdditionalContainer(mode, boundForm, maxNumberOfItems, request.cacheModel.containers)
    } else {

      ContainerYesNo.form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[ContainerYesNo]) => Future.successful(BadRequest(addFirstPage(mode, formWithErrors))),
          validForm =>
            validForm.id match {
              case Some(containerId) => saveFirstContainer(mode, containerId)
              case _                 => successful(navigator.continueTo(routes.SummaryController.displayPage(Mode.Normal)))
          }
        )
    }
  }

  def displayContainerSummary(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.containers match {
      case containers if containers.nonEmpty => Ok(summaryPage(mode, YesNoAnswer.form(), containers, allowSeals))
      case _ =>
        navigator.continueTo(routes.TransportContainerController.displayAddContainer(mode))
    }
  }

  def submitSummaryAction(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    FormAction.bindFromRequest() match {
      case Remove(values) =>
        successful(navigator.continueTo(routes.TransportContainerController.displayContainerRemove(mode, containerId(values))))
      case _ => addContainerAnswer(mode)
    }
  }

  def displayContainerRemove(mode: Mode, containerId: String): Action[AnyContent] =
    (authenticate andThen journeyType) { implicit request =>
      request.cacheModel.containerBy(containerId) match {
        case Some(container) => Ok(removePage(mode, YesNoAnswer.form(), container))
        case _               => navigator.continueTo(routes.TransportContainerController.displayContainerSummary(mode))
      }
    }

  def submitContainerRemove(mode: Mode, containerId: String): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      removeContainerAnswer(mode, containerId)
    }

  private def saveFirstContainer(mode: Mode, containerId: String)(implicit request: JourneyRequest[AnyContent]) = {
    val container = Container(containerId, Seq.empty)
    updateCache(Seq(container)).map(_ => redirectAfterAdd(mode, containerId))
  }

  private def saveAdditionalContainer(mode: Mode, boundForm: Form[ContainerAdd], elementLimit: Int, cache: Seq[Container])(
    implicit request: JourneyRequest[AnyContent]
  ) =
    prepare(boundForm, elementLimit, cache) fold (
      formWithErrors => successful(BadRequest(addPage(mode, formWithErrors))),
      updatedCache =>
        if (updatedCache != cache)
          updateCache(updatedCache)
            .map(_ => redirectAfterAdd(mode, updatedCache.last.id))
        else
          successful(
            navigator
              .continueTo(routes.TransportContainerController.displayContainerSummary(mode))
        )
    )

  private def prepare(boundForm: Form[ContainerAdd], elementLimit: Int, cache: Seq[Container]): Either[Form[ContainerAdd], Seq[Container]] = {
    val newContainer = boundForm.value.flatMap(_.id).map(Container(_, Seq.empty))
    newContainer match {
      case Some(container) =>
        duplication(container.id, cache) ++ limitOfElems(elementLimit, cache) match {
          case Seq()  => Right(cache :+ container)
          case errors => Left(boundForm.copy(errors = errors))
        }
      case _ => Left(boundForm)
    }
  }

  private def duplication(id: String, cachedData: Seq[Container]): Seq[FormError] =
    if (cachedData.exists(_.id == id)) Seq(FormError("", "supplementary.duplication")) else Seq.empty

  private def limitOfElems[A](limit: Int, cachedData: Seq[Container]): Seq[FormError] =
    if (cachedData.length >= limit) Seq(FormError("", "supplementary.limit")) else Seq.empty

  private def addContainerAnswer(mode: Mode)(implicit request: JourneyRequest[AnyContent]) =
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) => successful(BadRequest(summaryPage(mode, formWithErrors, request.cacheModel.containers, allowSeals))),
        formData =>
          formData.answer match {
            case YesNoAnswers.yes =>
              successful(navigator.continueTo(routes.TransportContainerController.displayAddContainer(mode)))
            case YesNoAnswers.no =>
              successful(navigator.continueTo(routes.SummaryController.displayPage(Mode.Normal)))
        }
      )

  private def removeContainerAnswer(mode: Mode, containerId: String)(implicit request: JourneyRequest[AnyContent]) =
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) =>
          successful(BadRequest(removePage(mode, formWithErrors, request.cacheModel.containers.filter(_.id == containerId).head))),
        formData => {
          formData.answer match {
            case YesNoAnswers.yes =>
              removeContainer(containerId, mode)
            case YesNoAnswers.no =>
              successful(navigator.continueTo(routes.TransportContainerController.displayContainerSummary(mode)))
          }
        }
      )

  private def removeContainer(containerId: String, mode: Mode)(implicit request: JourneyRequest[AnyContent]) =
    updateCache(request.cacheModel.containers.filterNot(_.id == containerId))
      .map(_ => navigator.continueTo(routes.TransportContainerController.displayContainerSummary(mode)))

  private def containerId(values: Seq[String]): String = values.headOption.getOrElse("")

  private def updateCache(updatedContainers: Seq[Container])(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(
      model => model.copy(transportData = Some(model.transportData.getOrElse(new TransportData).copy(containers = updatedContainers)))
    )

  private def redirectAfterAdd(mode: Mode, containerId: String)(implicit request: JourneyRequest[AnyContent]) =
    if (allowSeals)
      navigator.continueTo(routes.SealController.displaySealSummary(mode, containerId))
    else
      navigator.continueTo(routes.TransportContainerController.displayContainerSummary(mode))

  private def allowSeals(implicit request: JourneyRequest[AnyContent]) =
    request.isType(DeclarationType.STANDARD) || request.isType(DeclarationType.SUPPLEMENTARY) || request.isType(DeclarationType.SIMPLIFIED)

}
