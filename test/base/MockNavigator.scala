package base

import controllers.navigation.Navigator
import models.requests.JourneyRequest
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.mvc.{AnyContent, Call, Result}

trait MockNavigator extends MockitoSugar with BeforeAndAfterEach { self: MockitoSugar with Suite =>

  protected val navigator: Navigator = mock[Navigator]
  protected val aRedirectToTheNextPage: Result = mock[Result]

  override protected def beforeEach(): Unit = {
    given(navigator.continueTo(any[Call])(any[JourneyRequest[AnyContent]])).willReturn(aRedirectToTheNextPage)
  }

  protected def thePageNavigatedTo: Call = {
    val captor:ArgumentCaptor[Call] = ArgumentCaptor.forClass(classOf[Call])
    Mockito.verify(navigator).continueTo(captor.capture())(any())
    captor.getValue
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(navigator)
  }
}
