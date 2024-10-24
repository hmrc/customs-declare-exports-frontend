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

package base

import models.ExportsDeclaration
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.{BeforeAndAfterEach, Suite}
import services.cache.{ExportsCacheService, ExportsDeclarationBuilder}

import scala.concurrent.Future
import scala.jdk.CollectionConverters.IteratorHasAsScala

trait MockExportCacheService extends ExportsDeclarationBuilder with BeforeAndAfterEach {
  self: Suite =>

  val mockExportsCacheService: ExportsCacheService = mock[ExportsCacheService]

  val existingDeclarationId = "declarationUuid"

  def withNewCaching(dataToReturn: ExportsDeclaration): Unit = {
    when(mockExportsCacheService.update(any[ExportsDeclaration], any[String])(any()))
      .thenAnswer(withTheFirstArgument)

    when(mockExportsCacheService.create(any[ExportsDeclaration], any[String])(any()))
      .thenReturn(Future.successful(dataToReturn.copy(id = existingDeclarationId)))

    when(mockExportsCacheService.get(anyString)(any()))
      .thenReturn(Future.successful(Some(dataToReturn)))
  }

  private def withTheFirstArgument[T]: Answer[Future[T]] =
    (invocation: InvocationOnMock) => Future.successful(invocation.getArgument(0))

  def withCreateResponse(declaration: ExportsDeclaration): Unit =
    when(mockExportsCacheService.create(any[ExportsDeclaration], any[String])(any()))
      .thenReturn(Future.successful(declaration))

  def withNoDeclaration(): Unit =
    when(mockExportsCacheService.get(any())(any())).thenReturn(Future.successful(None))

  protected def theCacheModelUpdated: ExportsDeclaration = {
    val captor = ArgumentCaptor.forClass(classOf[ExportsDeclaration])
    verify(mockExportsCacheService).update(captor.capture(), any[String])(any())
    captor.getValue
  }

  protected def theCacheModelUpdated(invocations: Int = 1): List[ExportsDeclaration] = {
    val captor = ArgumentCaptor.forClass(classOf[ExportsDeclaration])
    verify(mockExportsCacheService, times(invocations)).update(captor.capture(), any[String])(any())
    captor.getAllValues.iterator.asScala.toList
  }

  protected def theCacheModelCreated: ExportsDeclaration = {
    val captor = ArgumentCaptor.forClass(classOf[ExportsDeclaration])
    verify(mockExportsCacheService).create(captor.capture(), any[String])(any())
    captor.getValue
  }

  protected def verifyTheCacheIsUnchanged(): Unit = {
    verify(mockExportsCacheService, never).update(any[ExportsDeclaration], any[String])(any())
    verify(mockExportsCacheService, never).create(any[ExportsDeclaration], any[String])(any())
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(mockExportsCacheService)
    super.afterEach()
  }
}
