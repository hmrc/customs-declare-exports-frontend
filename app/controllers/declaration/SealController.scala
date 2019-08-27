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
  navigator: Navigator,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  sealPage: seal
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val declaration = request.cacheModel
    Ok(sealPage(mode, form(), declaration.seals, declaration.transportDetails.exists(_.container)))
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val actionTypeOpt = FormAction.bindFromRequest()
    val boundForm = form().bindFromRequest()

    val data = request.cacheModel
    val seals = data.seals
    val hasContainers: Boolean = data.transportDetails.fold(false)(_.container)
    actionTypeOpt match {
      case Add                             => addSeal(mode, boundForm, sealsAllowed, seals)
      case Remove(ids)                     => removeSeal(mode, boundForm, seals, hasContainers, ids)
      case SaveAndContinue | SaveAndReturn => saveSeal(mode, boundForm, sealsAllowed, seals)
      case _                               => errorHandler.displayErrorPage()
    }
  }

  private def saveSeal(mode: Mode, boundForm: Form[Seal], elementLimit: Int, cachedSeals: Seq[Seal])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    saveAndContinue(boundForm, cachedSeals, isMandatory = false, elementLimit).fold(
      formWithErrors => Future.successful(badRequest(mode, formWithErrors, cachedSeals)),
      updatedCache =>
        if (updatedCache != cachedSeals) {
          updateCache(updatedCache).map { _ =>
            navigator.continueTo(routes.SummaryController.displayPage(Mode.Normal))
          }
        } else Future.successful(navigator.continueTo(routes.SummaryController.displayPage(Mode.Normal)))
    )

  private def badRequest(mode: Mode, formWithErrors: Form[Seal], cachedSeals: Seq[Seal])(implicit request: JourneyRequest[_]) = {
    val d = request.cacheModel.transportDetails
    val s = request.cacheModel.seals
    BadRequest(sealPage(mode, formWithErrors, s, d.fold(false)(_.container)))
  }

  private def removeSeal(
    mode: Mode,
    userInput: Form[Seal],
    cachedSeals: Seq[Seal],
    hasContainers: Boolean,
    ids: Seq[String]
  )(implicit request: JourneyRequest[_]): Future[Result] = {
    val updatedSeals = remove(cachedSeals, { seal: Seal =>
      ids.contains(seal.id)
    })
    updateCache(updatedSeals).map { _ =>
      Ok(sealPage(mode, userInput.discardingErrors, updatedSeals, hasContainers))
    }
  }

  private def updateCache(formData: Seq[Seal])(implicit req: JourneyRequest[_]) =
    updateExportsDeclarationSyncDirect(model => model.copy(seals = formData))

  private def addSeal(mode: Mode, boundForm: Form[Seal], elementLimit: Int, seals: Seq[Seal])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    add(boundForm, seals, elementLimit)
      .fold(
        formWithErrors => Future.successful(badRequest(mode, formWithErrors, seals)),
        updatedCache =>
          updateCache(updatedCache).map { _ =>
            navigator.continueTo(routes.SealController.displayPage(mode))
        }
      )
}
