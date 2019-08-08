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

import models.ExportsDeclaration
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{never, verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import services.cache.{ExportsCacheService, ExportsDeclarationBuilder}

import scala.concurrent.Future

trait MockExportsCacheService extends MockitoSugar with ExportsDeclarationBuilder with BeforeAndAfterEach {
  self: Suite =>

  val mockExportsCacheService = mock[ExportsCacheService]

  def withNewCaching(dataToReturn: ExportsDeclaration): Unit = {

    when(mockExportsCacheService.getItemByIdAndSession(anyString, anyString))
      .thenReturn(Future.successful(dataToReturn.items.headOption))

    when(mockExportsCacheService.update(anyString, any[ExportsDeclaration]))
      .thenReturn(Future.successful(Some(dataToReturn)))

    when(mockExportsCacheService.get(anyString))
      .thenReturn(Future.successful(Some(dataToReturn)))
  }

  protected def theCacheModelUpdated: ExportsDeclaration = {
    val captor = ArgumentCaptor.forClass(classOf[ExportsDeclaration])
    verify(mockExportsCacheService).update(anyString, captor.capture())
    captor.getValue
  }

  protected def verifyTheCacheIsUnchanged(): Unit =
    verify(mockExportsCacheService, never()).update(anyString, any[ExportsDeclaration])

  override protected def afterEach(): Unit = {
    Mockito.reset(mockExportsCacheService)
    super.afterEach()
  }
}
