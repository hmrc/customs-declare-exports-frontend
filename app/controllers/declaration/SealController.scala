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
import controllers.util._
import forms.declaration.Seal
import forms.declaration.Seal._
import handlers.ErrorHandler
import javax.inject.Inject
import models.Mode
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
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val declaration = request.cacheModel
    Ok(sealPage(form(), declaration.seals, declaration.transportDetails.exists(_.container)))
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val actionTypeOpt = FormAction.bindFromRequest()
    val boundForm = form().bindFromRequest()

    val data = request.cacheModel
    val seals = data.seals
    val hasContainers: Boolean = data.transportDetails.fold(false)(_.container)
    actionTypeOpt match {
      case Some(Add)             => addSeal(boundForm, sealsAllowed, seals)
      case Some(Remove(ids))     => removeSeal(boundForm, seals, hasContainers, ids)
      case Some(SaveAndContinue) => saveSeal(boundForm, sealsAllowed, seals)
      case _                     => errorHandler.displayErrorPage()
    }
  }

  private def saveSeal(boundForm: Form[Seal], elementLimit: Int, cachedSeals: Seq[Seal])(
    implicit request: JourneyRequest[_]
  ): Future[Result] =
    saveAndContinue(boundForm, cachedSeals, isMandatory = false, elementLimit).fold(
      formWithErrors => badRequest(formWithErrors, cachedSeals),
      updatedCache =>
        if (updatedCache != cachedSeals) {
          updateCache(updatedCache).map { _ =>
            Redirect(routes.SummaryController.displayPage(Mode.NormalMode))
          }
        } else Future.successful(Redirect(routes.SummaryController.displayPage(Mode.NormalMode)))
    )

  private def badRequest(formWithErrors: Form[Seal], cachedSeals: Seq[Seal])(implicit request: JourneyRequest[_]) = {
    val declaration = exportsCacheService.get(request.declarationId)
    declaration.map(_.flatMap(_.transportDetails)).flatMap { data =>
      declaration.map(_.map(_.seals)).map { seals =>
        BadRequest(sealPage(formWithErrors, seals.getOrElse(Seq.empty), data.fold(false)(_.container)))
      }
    }
  }

  private def removeSeal(userInput: Form[Seal], cachedSeals: Seq[Seal], hasContainers: Boolean, ids: Seq[String])(
    implicit request: JourneyRequest[_]
  ): Future[Result] = {
    val updatedSeals = remove(cachedSeals, { seal: Seal =>
      ids.contains(seal.id)
    })
    updateCache(updatedSeals).map { _ =>
      Ok(sealPage(userInput.discardingErrors, updatedSeals, hasContainers))
    }
  }

  private def updateCache(formData: Seq[Seal])(implicit req: JourneyRequest[_]) =
    updateExportsDeclarationSyncDirect(
      model => model.copy(seals = formData)
    )

  private def addSeal(boundForm: Form[Seal], elementLimit: Int, seals: Seq[Seal])(
    implicit request: JourneyRequest[_]
  ): Future[Result] =
    add(boundForm, seals, elementLimit)
      .fold(
        formWithErrors => badRequest(formWithErrors, seals),
        updatedCache =>
          updateCache(updatedCache).map { _ =>
            Redirect(routes.SealController.displayForm())
        }
      )
}
