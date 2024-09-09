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

package controllers.amendments

import com.google.inject.Inject
import config.featureFlags.DeclarationAmendmentsConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.general.routes.RootController
import controllers.summary.routes.SummaryController
import models.declaration.submissions.EnhancedStatus.ERRORS
import models.declaration.submissions.Submission
import models.requests.SessionHelper.declarationUuid
import play.api.Logging
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class AmendDeclarationController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  mcc: MessagesControllerComponents,
  connector: CustomsDeclareExportsConnector,
  cacheService: ExportsCacheService,
  declarationAmendmentsConfig: DeclarationAmendmentsConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with Logging {

  def initAmendment(parentId: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    if (!declarationAmendmentsConfig.isEnabled) Future.successful(Redirect(RootController.displayPage))
    else {
      cacheService.get(parentId).flatMap {
        case Some(srcDec) =>
          connector.findOrCreateDraftForAmendment(parentId, ERRORS, request.user.eori, srcDec).map { id =>
            Redirect(SummaryController.displayPage).addingToSession(declarationUuid -> id)
          }

        case None =>
          logger.warn(s"Could not retrieve from cache, for eori ${request.user.eori}, the declaration with id $parentId")
          Future.successful(Results.Redirect(RootController.displayPage))
      }
    }
  }
}

object AmendDeclarationController {

  def initAmendment(submission: Submission): Call =
    submission.latestDecId
      .map(parentId => routes.AmendDeclarationController.initAmendment(parentId))
      .getOrElse {
        val message = s"(Amendment) 'latestDecId' and/or 'latestEnhancedStatus' undefined for Submission(${submission.uuid})??"
        throw new IllegalStateException(message)
      }
}
