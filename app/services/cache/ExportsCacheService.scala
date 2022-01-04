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

package services.cache

import java.time.Instant

import connectors.CustomsDeclareExportsConnector
import connectors.exchange.ExportsDeclarationExchange
import javax.inject.{Inject, Singleton}
import models.ExportsDeclaration
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExportsCacheService @Inject()(connector: CustomsDeclareExportsConnector)(implicit ec: ExecutionContext) {

  def create(declaration: ExportsDeclarationExchange)(implicit hc: HeaderCarrier): Future[ExportsDeclaration] =
    connector.createDeclaration(declaration)

  def update(declaration: ExportsDeclaration)(implicit hc: HeaderCarrier): Future[Option[ExportsDeclaration]] = {
    val declarationWithUpdatedTimestamp = declaration.copy(updatedDateTime = Instant.now())
    connector.updateDeclaration(declarationWithUpdatedTimestamp).map(Some(_))
  }

  def get(id: String)(implicit hc: HeaderCarrier): Future[Option[ExportsDeclaration]] = connector.findDeclaration(id)

}
