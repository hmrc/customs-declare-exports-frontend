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

package controllers

import config.featureFlags.SfusConfig
import controllers.actions.AuthAction
import models.requests.{AuthenticatedRequest, ExportsSessionKeys}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.{AuditService, AuditTypes, EventData}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject

class FileUploadController @Inject()(sfusConfig: SfusConfig, authenticate: AuthAction, mcc: MessagesControllerComponents, auditService: AuditService)
    extends FrontendController(mcc) {

  def startFileUpload(mrn: String = ""): Action[AnyContent] = authenticate { implicit request =>
    lazy val sfusLink = s"${sfusConfig.sfusUploadLink}/$mrn"

    def auditData(eori: String, lrn: Option[String], mrn: String, docUploadUrl: String) =
      Map(
        EventData.eori.toString -> eori,
        EventData.lrn.toString -> lrn.getOrElse(""),
        EventData.mrn.toString -> mrn,
        EventData.url.toString -> docUploadUrl
      )

    auditService.audit(AuditTypes.UploadDocumentLink, auditData(request.user.eori, extractLrn, mrn, sfusLink))
    Redirect(Call("GET", sfusLink))
  }

  private def extractLrn(implicit request: AuthenticatedRequest[_]): Option[String] =
    request.session.data.get(ExportsSessionKeys.submissionLrn)
}
