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
import forms.Choice.AllowedChoiceValues
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.{form, YesNoAnswers}
import forms.declaration.TransportInformationContainer
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.TransportInformationContainerData.maxNumberOfItems
import models.declaration.{Container, TransportInformationContainerData}
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.{transport_container_add, transport_container_remove, transport_container_summary}

import scala.concurrent.{ExecutionContext, Future}

class TransportContainerController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  addPage: transport_container_add,
  summaryPage: transport_container_summary,
  removePage: transport_container_remove
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayAddContainer(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(addPage(mode, TransportInformationContainer.form()))
  }

  def submitAddContainer(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      val boundForm = TransportInformationContainer.form().bindFromRequest()
      saveContainer(mode, boundForm, maxNumberOfItems, request.cacheModel.containers)
  }

  def displayContainerSummary(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.containers match {
      case containers if containers.nonEmpty => Ok(summaryPage(mode, YesNoAnswer.form(), containers, allowSeals))
      case _ =>
        navigator.continueTo(controllers.declaration.routes.TransportContainerController.displayAddContainer(mode))
    }
  }

  def submitSummaryAction(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      FormAction.bindFromRequest() match {
        case Remove(values) =>
          Future.successful(
            navigator.continueTo(routes.TransportContainerController.displayContainerRemove(mode, containerId(values)))
          )
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

  private def saveContainer(
    mode: Mode,
    boundForm: Form[TransportInformationContainer],
    elementLimit: Int,
    cache: Seq[Container]
  )(implicit request: JourneyRequest[AnyContent]) =
    prepare(boundForm, elementLimit, cache) fold (
      formWithErrors => Future.successful(BadRequest(addPage(mode, formWithErrors))),
      updatedCache =>
        if (updatedCache != cache)
          updateCache(updatedCache)
            .map(_ => redirectAfterAdd(mode, updatedCache.last.id))
        else
          Future.successful(
            navigator
              .continueTo(controllers.declaration.routes.TransportContainerController.displayContainerSummary(mode))
        )
    )

  private def prepare(
    boundForm: Form[TransportInformationContainer],
    elementLimit: Int,
    cache: Seq[Container]
  ): Either[Form[TransportInformationContainer], Seq[Container]] = {
    val newContainer = boundForm.value.map(formValue => Container(formValue.id, Seq.empty))
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
        (formWithErrors: Form[YesNoAnswer]) =>
          Future.successful(BadRequest(summaryPage(mode, formWithErrors, request.cacheModel.containers, allowSeals))),
        formData =>
          formData.answer match {
            case YesNoAnswers.yes =>
              Future.successful(navigator.continueTo(routes.TransportContainerController.displayAddContainer(mode)))
            case YesNoAnswers.no =>
              Future
                .successful(navigator.continueTo(controllers.declaration.routes.SummaryController.displayPage(mode)))
        }
      )

  private def removeContainerAnswer(mode: Mode, containerId: String)(implicit request: JourneyRequest[AnyContent]) =
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) =>
          Future.successful(
            BadRequest(removePage(mode, formWithErrors, request.cacheModel.containers.filter(_.id == containerId).head))
        ),
        formData => {
          formData.answer match {
            case YesNoAnswers.yes =>
              removeContainer(containerId, mode)
            case YesNoAnswers.no =>
              Future.successful(navigator.continueTo(routes.TransportContainerController.displayContainerSummary(mode)))
          }
        }
      )

  private def removeContainer(containerId: String, mode: Mode)(implicit request: JourneyRequest[AnyContent]) =
    updateCache(request.cacheModel.containers.filterNot(_.id == containerId))
      .map(_ => navigator.continueTo(routes.TransportContainerController.displayContainerSummary(mode)))

  private def containerId(values: Seq[String]): String = values.headOption.getOrElse("")

  private def updateCache(
    updatedContainers: Seq[Container]
  )(implicit r: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(
      model => model.copy(containerData = Some(TransportInformationContainerData(updatedContainers)))
    )

  private def redirectAfterAdd(mode: Mode, containerId: String)(implicit request: JourneyRequest[AnyContent]) =
    if (allowSeals)
      navigator.continueTo(controllers.declaration.routes.SealController.displaySealSummary(mode, containerId))
    else
      navigator.continueTo(controllers.declaration.routes.TransportContainerController.displayContainerSummary(mode))

  private def allowSeals(implicit request: JourneyRequest[AnyContent]) =
    request.choice.value == AllowedChoiceValues.StandardDec

}
