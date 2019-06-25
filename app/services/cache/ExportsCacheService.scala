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
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExportsCacheService @Inject()(journeyCacheModelRepo: ExportsCacheModelRepository)(implicit ec: ExecutionContext) {
  private val logger = Logger(this.getClass)

  def get(sessionId: String): Future[Either[String, ExportsCacheModel]] = journeyCacheModelRepo.getWithEither(sessionId)

  def getBySessionIdAndFormId(sessionId: String): Future[Option[ExportsCacheModel]] = journeyCacheModelRepo.get(sessionId)

  def update(sessionId: String, model: ExportsCacheModel): Future[Either[String, ExportsCacheModel]] = {
    journeyCacheModelRepo.upsert(sessionId, model.copy(updatedDateTime = now())).map {
      case Some(retrievedModel) => Right(retrievedModel)
      case None => Left("unable to sss")
    }
  }.recover {
    case exception: Throwable =>
      logger.error(s"There is a problem saving the cacheModel with exception: ${exception.getMessage}")
      Left(exception.getMessage)
  }

  def save(model: ExportsCacheModel): Future[Either[String, Unit]] = {
    journeyCacheModelRepo.save(model).map {
      case false => Left("Failed saving cacheModel")
      case true => Right(())
    }.recover {
      case exception: Throwable =>
        logger.error(s"There is a problem saving the cacheModel with exception: ${exception.getMessage}")
        Left(exception.getMessage)
    }
  }
}

