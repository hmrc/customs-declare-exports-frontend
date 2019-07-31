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

import play.api.Logger
import reactivemongo.api.CollectionMetaCommands
import reactivemongo.api.indexes.Index

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait IndexManager {

  val collection: CollectionMetaCommands
  def indexes: Seq[Index]

  def dropSurplusIndexes(): Future[Unit] = for {
    existing <- collection.indexesManager.list()
    _ <- dropSurplusIndexes(existing)
  } yield (): Unit

  def dropSurplusIndexes(existing: List[Index]): Future[Unit] = {
    Future.sequence(
      existing.filter(e => !indexes.exists(_.name == e.name)).filterNot(_.name.contains("_id_")).map { idx =>
      Logger.info(s"Dropping index [${idx.name}]")
      collection.indexesManager.drop(idx.name.get)
    }).map(_ => (): Unit)
  }

  def createNewIndexes(): Future[Unit] = for {
    existing <- collection.indexesManager.list()
    _ <- createNewIndexes(existing)
  } yield (): Unit

  def createNewIndexes(existing: List[Index]): Future[Unit] = {
    Future.sequence(
      indexes
      .filter(idx => !existing.exists(_.name == idx.name))
      .map { idx =>
        Logger.info(s"Creating index [${idx.name}]")
        collection.indexesManager.create(idx)
      }
    ).map(_ => (): Unit)
  }

  def updateExistingIndexes(): Future[Unit] = for {
    existing <- collection.indexesManager.list()
    _ <- updateExistingIndexes(existing)
  } yield (): Unit

  def updateExistingIndexes(existing: List[Index]): Future[Unit] = {
    Future.sequence(
      indexes.map(idx => idx -> existing.find(_.name == idx.name)).map {
        case (updatedIndex, Some(existingIndex)) =>
          val checks = Set(
            updatedIndex.background == existingIndex.background,
            updatedIndex.dropDups == existingIndex.dropDups,
            updatedIndex.key == existingIndex.key,
            updatedIndex.unique == existingIndex.unique,
            updatedIndex.options == existingIndex.options
          )
          if (checks.exists(!_)) {
            Logger.info(s"Updating index [${updatedIndex.name}]")
            for {
              _ <- collection.indexesManager.drop(existingIndex.name.get)
              _ <- collection.indexesManager.create(updatedIndex)
            } yield (): Unit
          } else {
            Future.successful((): Unit)
          }

        case _ => Future.successful((): Unit)
      }).map(_ => (): Unit)
  }

  def ensureIndexes(): Future[Unit] = for {
    existing <- collection.indexesManager.list()
    _ <- dropSurplusIndexes(existing)
    _ <- createNewIndexes(existing)
    _ <- updateExistingIndexes(existing)
  } yield (): Unit


}
