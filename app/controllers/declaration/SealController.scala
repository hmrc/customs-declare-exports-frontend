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
import controllers.util.MultipleItemsHelper.{add, remove, saveAndContinue}
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.declaration.Seal
import forms.declaration.Seal._
import handlers.ErrorHandler
import javax.inject.Inject
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.seal

import scala.concurrent.{ExecutionContext, Future}

class SealController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  sealPage: seal
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val declaration = request.cacheModel
    Ok(sealPage(form, declaration.seals, declaration.transportDetails.exists(_.container)))
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val actionTypeOpt = FormAction.bindFromRequest()
    val boundForm = form().bindFromRequest()

    exportsCacheService
      .get(journeySessionId)
      .flatMap { data: Option[ExportsDeclaration] =>
        val seals = data.map(_.seals)
        val hasContainers: Boolean = data.flatMap(_.transportDetails).fold(false)(_.container)
        actionTypeOpt match {
          case Some(Add)             => addSeal(boundForm, sealsAllowed, seals.getOrElse(Seq.empty))
          case Some(Remove(ids))     => removeSeal(boundForm, seals.getOrElse(Seq.empty), hasContainers, ids)
          case Some(SaveAndContinue) => saveSeal(boundForm, sealsAllowed, seals.getOrElse(Seq.empty))
          case _                     => errorHandler.displayErrorPage()
        }
      }
  }

  private def saveSeal(boundForm: Form[Seal], elementLimit: Int, cachedSeals: Seq[Seal])(
    implicit request: JourneyRequest[_]
  ): Future[Result] =
    saveAndContinue(boundForm, cachedSeals, false, elementLimit).fold(
      formWithErrors => badRequest(formWithErrors, cachedSeals),
      updatedCache =>
        if (updatedCache != cachedSeals) {
          updateCache(journeySessionId, updatedCache).map { _ =>
            Redirect(routes.SummaryController.displayPage())
          }
        } else Future.successful(Redirect(routes.SummaryController.displayPage()))
    )

  private def badRequest(formWithErrors: Form[Seal], cachedSeals: Seq[Seal])(implicit request: JourneyRequest[_]) = {
    val declaration = exportsCacheService.get(journeySessionId)
    declaration.map(_.flatMap(_.transportDetails)).flatMap { data =>
      declaration.map(_.map(_.seals)).map { seals =>
        BadRequest(sealPage(formWithErrors, seals.getOrElse(Seq.empty), data.fold(false)(_.container)))
      }
    }
  }

  private def removeSeal(userInput: Form[Seal], cachedSeals: Seq[Seal], hasContainers: Boolean, ids: Seq[String])(
    implicit request: JourneyRequest[_]
  ): Future[Result] = {
    val updatedSeals = remove(ids.headOption, cachedSeals)
    updateCache(journeySessionId, updatedSeals).map { _ =>
      Ok(sealPage(userInput.discardingErrors, cachedSeals, hasContainers))
    }
  }

  private def updateCache(sessionId: String, formData: Seq[Seal])(implicit req: JourneyRequest[_]) =
    getAndUpdateExportCacheModel(
      sessionId,
      model =>
        exportsCacheService
          .update(sessionId, model.copy(seals = formData))
    )

  private def addSeal(boundForm: Form[Seal], elementLimit: Int, seals: Seq[Seal])(
    implicit request: JourneyRequest[_]
  ): Future[Result] =
    add(boundForm, seals, elementLimit)
      .fold(
        formWithErrors => badRequest(formWithErrors, seals),
        updatedCache =>
          updateCache(journeySessionId, updatedCache).map { _ =>
            Redirect(routes.SealController.displayForm())
        }
      )
}
