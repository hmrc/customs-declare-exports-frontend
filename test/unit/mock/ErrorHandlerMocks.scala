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

package unit.mock

import handlers.ErrorHandler
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Results.BadRequest
import play.twirl.api.Html

import scala.concurrent.Future

trait ErrorHandlerMocks extends BeforeAndAfterEach { self: MockitoSugar with Suite =>

  val mockErrorHandler: ErrorHandler = mock[ErrorHandler]

  def setupErrorHandler(): Unit = {
    when(mockErrorHandler.standardErrorTemplate(anyString, anyString, anyString)(any()))
      .thenReturn(Html.apply(""))

    when(mockErrorHandler.displayErrorPage()(any())).thenReturn(Future.successful(BadRequest(Html.apply(""))))
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(mockErrorHandler)
    super.afterEach()
  }
}
