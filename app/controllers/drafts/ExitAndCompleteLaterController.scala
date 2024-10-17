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

package controllers.drafts

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.ModelCacheable
import models.declaration.DeclarationStatus.DRAFT
import models.requests.SessionHelper.{declarationUuid, errorFixModeSessionKey}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.drafts.exit_and_complete_later

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExitAndCompleteLaterController @Inject() (
  authenticate: AuthAction,
  appConfig: AppConfig,
  mcc: MessagesControllerComponents,
  override val exportsCacheService: ExportsCacheService,
  exitAndCompleteLater: exit_and_complete_later,
  journeyType: JourneyAction
)(implicit ec: ExecutionContext, auditService: AuditService) extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val updatedDateTime = request.cacheModel.declarationMeta.updatedDateTime
    val expiry = updatedDateTime.plusSeconds(appConfig.draftTimeToLive.toSeconds).toEpochMilli.toString

    val result = Ok(exitAndCompleteLater(request.declarationId, expiry)).removingFromSession(declarationUuid, errorFixModeSessionKey)

    val meta = request.cacheModel.declarationMeta
    if (meta.status == DRAFT && meta.summaryWasVisited.contains(true)) Future.successful(result)
    else updateDeclarationFromRequest(declaration =>
      declaration.copy(declarationMeta = declaration.declarationMeta.copy(summaryWasVisited = Some(true)))
    ).map(_ => result)
  }
}
