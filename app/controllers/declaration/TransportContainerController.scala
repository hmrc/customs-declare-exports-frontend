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
import controllers.helpers.{FormAction, Remove}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.{ContainerAdd, ContainerFirst}
import models.declaration.Container
import models.declaration.Container.maxNumberOfItems
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportContainerController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  addFirstPage: transport_container_add_first,
  addPage: transport_container_add,
  summaryPage: transport_container_summary,
  removePage: transport_container_remove
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayAddContainer(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (request.cacheModel.hasContainers) {
      Ok(addPage(mode, ContainerAdd.form().withSubmissionErrors()))
    } else {
      Ok(addFirstPage(mode, ContainerFirst.form().withSubmissionErrors()))
    }
  }

  def submitAddContainer(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    if (request.cacheModel.hasContainers) {
      val boundForm = ContainerAdd.form().bindFromRequest()
      saveAdditionalContainer(mode, boundForm, maxNumberOfItems, request.cacheModel.containers)
    } else {

      ContainerFirst.form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[ContainerFirst]) => Future.successful(BadRequest(addFirstPage(mode, formWithErrors))),
          validForm => saveFirstContainer(mode, validForm.id)
        )
    }
  }

  def displayContainerSummary(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.containers match {
      case containers if containers.nonEmpty => Ok(summaryPage(mode, addAnotherContainerYesNoForm.withSubmissionErrors(), containers))
      case _ =>
        navigator.continueTo(mode, routes.TransportContainerController.displayAddContainer)
    }
  }

  def submitSummaryAction(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    FormAction.bindFromRequest() match {
      case Remove(values) =>
        Future.successful(navigator.continueTo(mode, routes.TransportContainerController.displayContainerRemove(_, containerId(values))))
      case _ => addContainerAnswer(mode)
    }
  }

  def displayContainerRemove(mode: Mode, containerId: String): Action[AnyContent] =
    (authenticate andThen journeyType) { implicit request =>
      request.cacheModel.containerBy(containerId) match {
        case Some(container) => Ok(removePage(mode, removeContainerYesNoForm, container))
        case _               => navigator.continueTo(mode, routes.TransportContainerController.displayContainerSummary)
      }
    }

  def submitContainerRemove(mode: Mode, containerId: String): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      removeContainerAnswer(mode, containerId)
    }

  private def saveFirstContainer(mode: Mode, containerId: Option[String])(implicit request: JourneyRequest[AnyContent]) =
    containerId match {
      case Some(id) => updateCache(Seq(Container(id, Seq.empty))).map(_ => redirectAfterAdd(mode, id))
      case None     => updateCache(Seq.empty).map(_ => navigator.continueTo(mode, routes.SummaryController.displayPage))
    }

  private def saveAdditionalContainer(mode: Mode, boundForm: Form[ContainerAdd], elementLimit: Int, cache: Seq[Container])(
    implicit request: JourneyRequest[AnyContent]
  ) =
    prepare(boundForm, elementLimit, cache) fold (
      formWithErrors => Future.successful(BadRequest(addPage(mode, formWithErrors))),
      updatedCache =>
        if (updatedCache != cache)
          updateCache(updatedCache)
            .map(_ => redirectAfterAdd(mode, updatedCache.last.id))
        else
          Future.successful(
            navigator
              .continueTo(mode, routes.TransportContainerController.displayContainerSummary)
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

  private def addAnotherContainerYesNoForm: Form[YesNoAnswer] =
    YesNoAnswer.form(errorKey = "declaration.transportInformation.container.another.empty")
  private def removeContainerYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.transportInformation.container.remove.empty")

  private def duplication(id: String, cachedData: Seq[Container]): Seq[FormError] =
    if (cachedData.exists(_.id == id)) Seq(FormError("id", "declaration.transportInformation.error.duplicate")) else Seq.empty

  private def limitOfElems[A](limit: Int, cachedData: Seq[Container]): Seq[FormError] =
    if (cachedData.length >= limit) Seq(FormError("", "supplementary.limit")) else Seq.empty

  private def addContainerAnswer(mode: Mode)(implicit request: JourneyRequest[AnyContent]) =
    addAnotherContainerYesNoForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) => Future.successful(BadRequest(summaryPage(mode, formWithErrors, request.cacheModel.containers))),
        formData =>
          formData.answer match {
            case YesNoAnswers.yes =>
              Future.successful(navigator.continueTo(mode, routes.TransportContainerController.displayAddContainer))
            case YesNoAnswers.no =>
              Future.successful(navigator.continueTo(Mode.Normal, routes.SummaryController.displayPage))
        }
      )

  private def removeContainerAnswer(mode: Mode, containerId: String)(implicit request: JourneyRequest[AnyContent]) =
    removeContainerYesNoForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) =>
          Future.successful(BadRequest(removePage(mode, formWithErrors, request.cacheModel.containers.filter(_.id == containerId).head))),
        formData => {
          formData.answer match {
            case YesNoAnswers.yes =>
              removeContainer(containerId, mode)
            case YesNoAnswers.no =>
              Future.successful(navigator.continueTo(Mode.Normal, routes.TransportContainerController.displayContainerSummary))
          }
        }
      )

  private def removeContainer(containerId: String, mode: Mode)(implicit request: JourneyRequest[AnyContent]) =
    updateCache(request.cacheModel.containers.filterNot(_.id == containerId))
      .map(_ => navigator.continueTo(mode, routes.TransportContainerController.displayContainerSummary))

  private def containerId(values: Seq[String]): String = values.headOption.getOrElse("")

  private def updateCache(updatedContainers: Seq[Container])(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(_.updateContainers(updatedContainers))

  private def redirectAfterAdd(mode: Mode, containerId: String)(implicit request: JourneyRequest[AnyContent]) =
    navigator.continueTo(mode, routes.SealController.displaySealSummary(_, containerId))
}
