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

package base

import controllers.navigation.Navigator
import models.Mode
import models.requests.JourneyRequest
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.mvc.{AnyContent, Call, ResponseHeader, Result}
import uk.gov.hmrc.http.HeaderCarrier

trait MockNavigator extends MockitoSugar with BeforeAndAfterEach { self: MockitoSugar with Suite =>

  protected val navigator: Navigator = mock[Navigator]
  protected val aRedirectToTheNextPage: Result = mock[Result]
  protected val redirectFactoryToTheNextPage: Mode => Call = mock[Mode => Call]
  protected val hc: HeaderCarrier = HeaderCarrier()

  override protected def beforeEach(): Unit = {
    given(navigator.continueTo(any[Mode], any[Mode => Call]())(any[JourneyRequest[AnyContent]]))
      .willReturn(aRedirectToTheNextPage)
    given(aRedirectToTheNextPage.header).willReturn(ResponseHeader(Status.SEE_OTHER))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(navigator)
  }

  protected def initMockNavigatorForMultipleCallsInTheSameTest(): Unit = {
    Mockito.reset(navigator)
    given(navigator.continueTo(any[Mode], any[Mode => Call]())(any[JourneyRequest[AnyContent]]))
      .willReturn(aRedirectToTheNextPage)
    given(aRedirectToTheNextPage.header).willReturn(ResponseHeader(Status.SEE_OTHER))
  }

  protected def thePageNavigatedTo: Call = {
    val modeCaptor: ArgumentCaptor[Mode] = ArgumentCaptor.forClass(classOf[Mode])
    val callCaptor: ArgumentCaptor[Mode => Call] = ArgumentCaptor.forClass(classOf[Mode => Call])
    Mockito.verify(navigator).continueTo(modeCaptor.capture(), callCaptor.capture())(any())
    callCaptor.getValue.apply(modeCaptor.getValue)
  }
}
