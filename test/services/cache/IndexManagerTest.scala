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

import java.util.concurrent.TimeUnit

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, WordSpec}
import reactivemongo.api.CollectionMetaCommands
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{CollectionIndexesManager, Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONInteger, BSONNull, BSONValue}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext, Future}

class IndexManagerTest extends WordSpec with MockitoSugar with BeforeAndAfterEach {

  private val indexManagerSupplier = mock[CollectionMetaCommands]
  private val underlyingIndexManager = mock[CollectionIndexesManager]

  override def afterEach(): Unit = {
    reset(indexManagerSupplier, underlyingIndexManager)
  }

  "IndexManager" should {
    val existingIndex1 = index("key1", "index1", "option1" -> BSONInteger(1))
    val existingIndex2 = index("key2", "index2", "option2" -> BSONInteger(2))
    val currentIndex1 = index("key1", "index1", "option1" -> BSONInteger(1))
    val currentIndex2 = index("key2", "index2", "option2" -> BSONInteger(2))

    "Drop surplus indexes" in {
      given(indexManagerSupplier.indexesManager(any[ExecutionContext])) willReturn underlyingIndexManager
      given(underlyingIndexManager.list()) willReturn Future.successful(List(existingIndex1, existingIndex2))
      given(underlyingIndexManager.drop(anyString)) willReturn Future.successful(1)

      await(newIndexManager(Seq(currentIndex2)).dropSurplusIndexes())

      verify(underlyingIndexManager).drop("index1")
      verify(underlyingIndexManager, never).drop("index2")
    }

    "Not drop _id_ index" in {
      given(indexManagerSupplier.indexesManager(any[ExecutionContext])) willReturn underlyingIndexManager
      given(underlyingIndexManager.list()) willReturn Future.successful(List(index("key", "_id_")))
      given(underlyingIndexManager.drop(anyString)) willReturn Future.successful(1)

      await(newIndexManager(Seq(currentIndex2)).dropSurplusIndexes())

      verify(underlyingIndexManager, never).drop("_id_")
    }

    "Create new indexes" in {
      given(indexManagerSupplier.indexesManager(any[ExecutionContext])) willReturn underlyingIndexManager
      given(underlyingIndexManager.list()) willReturn Future.successful(List(existingIndex1))
      given(underlyingIndexManager.create(any[Index])) willReturn Future.successful[WriteResult](mock[WriteResult])

      await(newIndexManager(Seq(currentIndex1, currentIndex2)).createNewIndexes())

      verify(underlyingIndexManager, never).create(existingIndex1)
      verify(underlyingIndexManager).create(existingIndex2)
    }

    "Update existing indexes" when {
      "nothing has changed" in {
        given(indexManagerSupplier.indexesManager(any[ExecutionContext])) willReturn underlyingIndexManager
        given(underlyingIndexManager.list()) willReturn Future.successful(List(existingIndex1))
        given(underlyingIndexManager.drop(anyString)) willReturn Future.successful(1)
        given(underlyingIndexManager.create(any[Index])) willReturn Future.successful[WriteResult](mock[WriteResult])

        await(newIndexManager(Seq(currentIndex1)).updateExistingIndexes())

        verify(underlyingIndexManager, never).drop("index1")
        verify(underlyingIndexManager, never).create(existingIndex1)
      }

      "key has changed" in {
        given(indexManagerSupplier.indexesManager(any[ExecutionContext])) willReturn underlyingIndexManager
        given(underlyingIndexManager.list()) willReturn Future.successful(List(existingIndex1, existingIndex2))
        given(underlyingIndexManager.drop(anyString)) willReturn Future.successful(1)
        given(underlyingIndexManager.create(any[Index])) willReturn Future.successful[WriteResult](mock[WriteResult])

        val updatedIndex2 = currentIndex2.copy(key = Seq("updated" -> IndexType.Ascending))
        await(newIndexManager(Seq(currentIndex1, updatedIndex2)).updateExistingIndexes())

        verify(underlyingIndexManager, never).drop("index1")
        verify(underlyingIndexManager, never).create(existingIndex1)
        verify(underlyingIndexManager).drop("index2")
        verify(underlyingIndexManager).create(updatedIndex2)
      }

      "'unique' has changed" in {
        given(indexManagerSupplier.indexesManager(any[ExecutionContext])) willReturn underlyingIndexManager
        given(underlyingIndexManager.list()) willReturn Future.successful(List(existingIndex1, existingIndex2))
        given(underlyingIndexManager.drop(anyString)) willReturn Future.successful(1)
        given(underlyingIndexManager.create(any[Index])) willReturn Future.successful[WriteResult](mock[WriteResult])

        val updatedIndex2 = currentIndex2.copy(unique = true)
        await(newIndexManager(Seq(currentIndex1, updatedIndex2)).updateExistingIndexes())

        verify(underlyingIndexManager, never).drop("index1")
        verify(underlyingIndexManager, never).create(existingIndex1)
        verify(underlyingIndexManager).drop("index2")
        verify(underlyingIndexManager).create(updatedIndex2)
      }

      "'background' has changed" in {
        given(indexManagerSupplier.indexesManager(any[ExecutionContext])) willReturn underlyingIndexManager
        given(underlyingIndexManager.list()) willReturn Future.successful(List(existingIndex1, existingIndex2))
        given(underlyingIndexManager.drop(anyString)) willReturn Future.successful(1)
        given(underlyingIndexManager.create(any[Index])) willReturn Future.successful[WriteResult](mock[WriteResult])

        val updatedIndex2 = currentIndex2.copy(background = true)
        await(newIndexManager(Seq(currentIndex1, updatedIndex2)).updateExistingIndexes())

        verify(underlyingIndexManager, never).drop("index1")
        verify(underlyingIndexManager, never).create(existingIndex1)
        verify(underlyingIndexManager).drop("index2")
        verify(underlyingIndexManager).create(updatedIndex2)
      }

      "'options' has changed" in {
        given(indexManagerSupplier.indexesManager(any[ExecutionContext])) willReturn underlyingIndexManager
        given(underlyingIndexManager.list()) willReturn Future.successful(List(existingIndex1, existingIndex2))
        given(underlyingIndexManager.drop(anyString)) willReturn Future.successful(1)
        given(underlyingIndexManager.create(any[Index])) willReturn Future.successful[WriteResult](mock[WriteResult])

        val updatedIndex2 = currentIndex2.copy(options = BSONDocument(Set("option" -> BSONNull)))
        await(newIndexManager(Seq(currentIndex1, updatedIndex2)).updateExistingIndexes())

        verify(underlyingIndexManager, never).drop("index1")
        verify(underlyingIndexManager, never).create(existingIndex1)
        verify(underlyingIndexManager).drop("index2")
        verify(underlyingIndexManager).create(updatedIndex2)
      }
    }

    "ensure indexes" when {
      "nothing has changed" in {
        given(indexManagerSupplier.indexesManager(any[ExecutionContext])) willReturn underlyingIndexManager
        given(underlyingIndexManager.list()) willReturn Future.successful(List(existingIndex1, existingIndex2))

        await(newIndexManager(Seq(currentIndex1, currentIndex2)).ensureIndexes())

        verify(underlyingIndexManager, never).drop(anyString())
        verify(underlyingIndexManager, never).create(any[Index])
      }

      "new index" in {
        given(indexManagerSupplier.indexesManager(any[ExecutionContext])) willReturn underlyingIndexManager
        given(underlyingIndexManager.list()) willReturn Future.successful(List(existingIndex1))
        given(underlyingIndexManager.create(any[Index])) willReturn Future.successful[WriteResult](mock[WriteResult])

        await(newIndexManager(Seq(currentIndex1, currentIndex2)).ensureIndexes())

        verify(underlyingIndexManager, never).drop(anyString())
        verify(underlyingIndexManager).create(currentIndex2)
      }

      "removed index" in {
        given(indexManagerSupplier.indexesManager(any[ExecutionContext])) willReturn underlyingIndexManager
        given(underlyingIndexManager.list()) willReturn Future.successful(List(existingIndex1, existingIndex2))
        given(underlyingIndexManager.drop(anyString)) willReturn Future.successful(1)

        await(newIndexManager(Seq(currentIndex1)).ensureIndexes())

        verify(underlyingIndexManager).drop("index2")
        verify(underlyingIndexManager, never).create(any[Index])
      }

      "updated index" in {
        given(indexManagerSupplier.indexesManager(any[ExecutionContext])) willReturn underlyingIndexManager
        given(underlyingIndexManager.list()) willReturn Future.successful(List(existingIndex1, existingIndex2))
        given(underlyingIndexManager.drop(anyString)) willReturn Future.successful(1)
        given(underlyingIndexManager.create(any[Index])) willReturn Future.successful[WriteResult](mock[WriteResult])

        val updatedIndex1 = currentIndex1.copy(unique = true)
        await(newIndexManager(Seq(updatedIndex1, currentIndex2)).ensureIndexes())

        verify(underlyingIndexManager).drop("index1")
        verify(underlyingIndexManager).create(updatedIndex1)
        verify(underlyingIndexManager, never).drop("index2")
        verify(underlyingIndexManager, never).create(currentIndex2)
      }
    }
  }

  private def await[T](future: Future[T]): T = Await.result(future, FiniteDuration(5, TimeUnit.SECONDS))

  private def index(key: String, name: String, options: (String, BSONValue)*) = {
    Index(key = Seq(key -> IndexType.Descending), name = Some(name), options = BSONDocument(options))
  }

  private def newIndexManager(currentIndexes: Seq[Index]): IndexManager = new IndexManager {
    override val collection: CollectionMetaCommands = indexManagerSupplier
    override def indexes: Seq[Index] = currentIndexes
  }
}
