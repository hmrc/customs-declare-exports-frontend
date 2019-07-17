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

package base

import java.time.LocalDateTime

import models.declaration.Parties
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.mockito.MockitoSugar
import services.cache.{ExportItem, ExportsCacheModel, ExportsCacheService}

import scala.concurrent.Future

trait MockExportsCacheService extends MockitoSugar {

  val mockExportsCacheService = mock[ExportsCacheService]

  def withNewCaching(dataToReturn: ExportsCacheModel): Unit = {

    when(mockExportsCacheService.getItemByIdAndSession(anyString, anyString))
      .thenReturn((Future.successful(dataToReturn.items.headOption)))

    when(
      mockExportsCacheService
        .update(anyString, any[ExportsCacheModel])
    ).thenReturn(Future.successful(Some(dataToReturn)))

    when(
      mockExportsCacheService
        .get(anyString)
    ).thenReturn(Future.successful(Some(dataToReturn)))
  }

  def createModelWithItem(
    existingSessionId: String,
    item: Option[ExportItem] = Some(ExportItem(id = "1234"))
  ): ExportsCacheModel = {
    val cacheExportItems = item.fold(Set.empty[ExportItem])(Set(_))
    createModelWithItems(existingSessionId, cacheExportItems)
  }

  def createModelWithItems(sessionId: String, items: Set[ExportItem]): ExportsCacheModel =
    ExportsCacheModel(
      sessionId = sessionId,
      draftId = "",
      createdDateTime = LocalDateTime.now(),
      updatedDateTime = LocalDateTime.now(),
      choice = "SMP",
      items = items,
      parties = Parties()
    )

  def createModelWithNoItems(): ExportsCacheModel = createModelWithItems("", Set.empty)

  protected def theCacheModelUpdated: ExportsCacheModel = {
    val captor = ArgumentCaptor.forClass(classOf[ExportsCacheModel])
    verify(mockExportsCacheService).update(anyString, captor.capture())
    captor.getValue
  }

  protected def verifyTheCacheIsUnchanged(): Unit =
    verify(mockExportsCacheService, never()).update(anyString, any[ExportsCacheModel])
}
