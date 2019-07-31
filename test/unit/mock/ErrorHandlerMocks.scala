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
