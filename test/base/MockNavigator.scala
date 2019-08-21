package base

import controllers.navigation.Navigator
import org.mockito.Mockito
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatest.mockito.MockitoSugar

trait MockNavigator extends MockitoSugar with BeforeAndAfterEach { self: MockitoSugar with Suite =>

  protected val navigator: Navigator = mock[Navigator]

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(navigator)
  }
}
