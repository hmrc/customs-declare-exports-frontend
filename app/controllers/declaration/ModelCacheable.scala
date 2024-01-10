/*
 * Copyright 2023 HM Revenue & Customs
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

import models.ExportsDeclaration
import models.requests.JourneyRequest
import services.audit.{AuditService, AuditTypes}
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait ModelCacheable {

  def exportsCacheService: ExportsCacheService

  def updateDeclaration(declaration: ExportsDeclaration, eori: String)(implicit hc: HeaderCarrier): Future[ExportsDeclaration] =
    exportsCacheService.update(declaration, eori)

  def updateDeclarationFromRequest(
    updateDeclaration: ExportsDeclaration => ExportsDeclaration
  )(implicit hc: HeaderCarrier, request: JourneyRequest[_], auditService: AuditService, ec: ExecutionContext): Future[ExportsDeclaration] =
    exportsCacheService.update(updateDeclaration(request.cacheModel), request.eori).map { updatedCache =>
      auditService.auditAllPagesUserInput(AuditTypes.SaveDraftValue, updatedCache)
      updatedCache
    }
}
