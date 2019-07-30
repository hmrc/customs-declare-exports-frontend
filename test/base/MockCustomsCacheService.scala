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

import forms.Choice
import org.mockito.{ArgumentMatchers, Mockito}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import services.CustomsCacheService
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

trait MockCustomsCacheService extends MockitoSugar with BeforeAndAfterEach { self: Suite =>

  val mockCustomsCacheService = mock[CustomsCacheService]

  def withCaching[T](form: Option[Form[T]]): OngoingStubbing[Future[CacheMap]] = {
    when(mockCustomsCacheService.fetchAndGetEntry[Form[T]](any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(form))

    when(mockCustomsCacheService.cache[T](any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(CacheMap("id1", Map.empty)))
  }

  def withCaching[T](dataToReturn: Option[T], id: String): OngoingStubbing[Future[CacheMap]] = {
    when(
      mockCustomsCacheService
        .fetchAndGetEntry[T](any(), ArgumentMatchers.eq(id))(any(), any(), any())
    ).thenReturn(Future.successful(dataToReturn))

    when(mockCustomsCacheService.cache[T](any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(CacheMap(id, Map.empty)))
  }

  def withJourneyType(choice: Choice): OngoingStubbing[Future[Option[Choice]]] =
    when(mockCustomsCacheService.fetchAndGetEntry[Choice](any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(Some(choice)))

  override protected def afterEach(): Unit = {
    Mockito.reset(mockCustomsCacheService)
    super.afterEach()
  }
}
