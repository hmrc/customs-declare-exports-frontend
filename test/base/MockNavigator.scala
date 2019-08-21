package base

import controllers.navigation.Navigator
import models.requests.JourneyRequest
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.{BDDMockito, Mockito}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.mvc.Result

trait MockNavigator extends MockitoSugar with BeforeAndAfterEach { self: MockitoSugar with Suite =>

  protected val navigator: Navigator = mock[Navigator]
  protected val draftConfirmationResult: Result = mock[Result]

  override protected def beforeEach(): Unit = {
    given(navigator.goToDraftConfirmation()(any[JourneyRequest[_]])).willReturn(draftConfirmationResult)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(navigator)
  }
}
