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

package services.cache

import java.time.LocalDateTime.now

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExportsCacheService @Inject()(journeyCacheModelRepo: ExportsCacheModelRepository)(implicit ec: ExecutionContext) {

  def get(sessionId: String): Future[Option[ExportsCacheModel]] = journeyCacheModelRepo.get(sessionId)

  def update(sessionId: String, model: ExportsCacheModel): Future[Option[ExportsCacheModel]] =
    journeyCacheModelRepo.upsert(sessionId, model.copy(updatedDateTime = now()))

  @deprecated
  def getItemByIdAndSession(itemId: String, sessionId: String): Future[Option[ExportItem]] =
    get(sessionId).map {
      case Some(model) => model.itemBy(itemId)
      case _           => None
    }

}
